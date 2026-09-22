package org.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.BizException;
import org.example.backend.dto.SiteNavReqDto;
import org.example.backend.dto.SiteNavQueryDto;
import org.example.backend.dto.vo.SiteNavVo;
import org.example.backend.entity.SiteNav;
import org.example.backend.entity.SysUser;
import org.example.backend.mapper.SiteNavMapper;
import org.example.backend.service.ISiteNavService;
import org.example.backend.service.ISysUserService;
import org.example.backend.utils.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 站点导航业务实现类（整合 Redis 旁路缓存 Cache-Aside）
 */
@Slf4j
@Service
public class SiteNavServiceImpl extends ServiceImpl<SiteNavMapper, SiteNav> implements ISiteNavService {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /** 导航全量列表 Redis 缓存 Key */
    public static final String CACHE_KEY_NAV_LIST_ALL = "site_nav:list:all";

    @Override
    public Long addNav(SiteNavReqDto dto) {
        String name = dto.getName().trim();

        // 1. 查重：检查是否已有同名且未删除的导航菜单
        long count = this.count(new LambdaQueryWrapper<SiteNav>()
                .eq(SiteNav::getName, name));
        if (count > 0) {
            throw new BizException("该导航菜单名称已存在，请勿重复添加");
        }

        // 2. 构造实体对象（无需手动 set id、createTime、createBy，由雪花算法和 MetaObjectHandler 全自动处理！）
        SiteNav nav = SiteNav.builder()
                .name(name)
                .path(dto.getPath().trim())
                .icon(StrUtil.isNotBlank(dto.getIcon()) ? dto.getIcon().trim() : "")
                .sort(dto.getSort() != null ? dto.getSort() : 0)
                .isBlank(dto.getIsBlank() != null ? dto.getIsBlank() : 0)
                .build();

        this.save(nav);
        // 【缓存失效】：新增数据后，安全清除全量缓存（自动适配事务 afterCommit）
        CacheUtils.deleteAfterCommit(CACHE_KEY_NAV_LIST_ALL);
        return nav.getId();
    }

    @Override
    public void updateNav(Long id, SiteNavReqDto dto) {
        SiteNav nav = this.getById(id);
        if (nav == null) {
            throw new BizException("待修改的导航菜单不存在");
        }

        String name = dto.getName().trim();
        // 查重：排除当前 ID，检查是否有其它菜单同名
        long count = this.count(new LambdaQueryWrapper<SiteNav>()
                .eq(SiteNav::getName, name)
                .ne(SiteNav::getId, id));
        if (count > 0) {
            throw new BizException("该导航菜单名称已被其它项目占用");
        }

        nav.setName(name);
        nav.setPath(dto.getPath().trim());
        nav.setIcon(StrUtil.isNotBlank(dto.getIcon()) ? dto.getIcon().trim() : "");
        if (dto.getSort() != null) {
            nav.setSort(dto.getSort());
        }
        if (dto.getIsBlank() != null) {
            nav.setIsBlank(dto.getIsBlank());
        }

        // updateById 会全自动触发 MetaObjectHandler.updateFill 刷新 updateTime
        this.updateById(nav);
        // 【缓存失效】：修改数据后，安全清除全量缓存（自动适配事务 afterCommit）
        CacheUtils.deleteAfterCommit(CACHE_KEY_NAV_LIST_ALL);
    }

    @Override
    public void deleteNav(Long id) {
        // removeById 自动触发 @TableLogic 逻辑删除（变为 UPDATE site_nav SET deleted = 1 WHERE id = ?）
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new BizException("待删除的导航菜单不存在或已被删除");
        }
        // 【缓存失效】：删除数据后，安全清除全量缓存（自动适配事务 afterCommit）
        CacheUtils.deleteAfterCommit(CACHE_KEY_NAV_LIST_ALL);
    }

    @Override
    public List<SiteNavVo> listNavs(org.example.backend.dto.SiteNavQueryDto queryDto) {
        // 判断是否是无筛选条件的全量查询
        boolean isAllQuery = queryDto == null || 
                (StrUtil.isBlank(queryDto.getName()) && StrUtil.isBlank(queryDto.getPath()));

        // 1. 【缓存读】：如果是全量无筛选查询，优先尝试从 Redis 中极速提取
        if (isAllQuery) {
            String cachedJson = redisTemplate.opsForValue().get(CACHE_KEY_NAV_LIST_ALL);
            if (StrUtil.isNotBlank(cachedJson)) {
                try {
                    log.info("🎯 命中 Redis 导航列表缓存，直接返回内存数据（零 SQL 执行）");
                    return objectMapper.readValue(cachedJson, new TypeReference<List<SiteNavVo>>() {});
                } catch (Exception e) {
                    log.error("Redis 缓存反序列化失败，安全降级走查库: {}", e.getMessage());
                }
            }
        }

        // 2. 动态条件查询：支持菜单名称、路由路径的模糊搜索
        LambdaQueryWrapper<SiteNav> wrapper = new LambdaQueryWrapper<>();
        if (queryDto != null) {
            wrapper.like(StrUtil.isNotBlank(queryDto.getName()), SiteNav::getName, StrUtil.trim(queryDto.getName()))
                   .like(StrUtil.isNotBlank(queryDto.getPath()), SiteNav::getPath, StrUtil.trim(queryDto.getPath()));
        }
        wrapper.orderByAsc(SiteNav::getSort)
               .orderByAsc(SiteNav::getCreateTime);

        List<SiteNav> navList = this.list(wrapper);

        if (CollUtil.isEmpty(navList)) {
            return Collections.emptyList();
        }

        // 3. 收集所有不为空的创建人 ID，并利用 Set 自动去重
        Set<Long> userIds = navList.stream()
                .map(SiteNav::getCreateBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 4. 批量一次性查出涉及的用户，转为 Map<Long, SysUser>（避免在循环里执行 N 次查库）
        Map<Long, SysUser> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<SysUser> userList = sysUserService.listByIds(userIds);
            userMap = userList.stream()
                    .collect(Collectors.toMap(SysUser::getId, Function.identity(), (u1, u2) -> u1));
        }

        // 5. 内存流水线组装：跨表装配创建人昵称 + 派生计算展示排序 (* 10)
        final Map<Long, SysUser> finalUserMap = userMap;
        List<SiteNavVo> voList = navList.stream().map(nav -> {
            // ① 基础同名字段自动复制 (Entity -> VO)
            SiteNavVo vo = BeanUtil.copyProperties(nav, SiteNavVo.class);

            // ② 跨表属性装配：根据 createBy 从 Map 快速提取用户昵称
            Long creatorId = nav.getCreateBy();
            if (creatorId != null && finalUserMap.containsKey(creatorId)) {
                SysUser user = finalUserMap.get(creatorId);
                vo.setCreatorName(user.getNickname() + " (" + user.getUsername() + ")");
            } else {
                vo.setCreatorName("系统内置");
            }

            // ③ 派生计算装配：展示排序 = 原始排序 * 10
            int rawSort = nav.getSort() != null ? nav.getSort() : 0;
            vo.setDisplaySort(rawSort * 10);

            return vo;
        }).collect(Collectors.toList());

        // 6. 【缓存写】：全量查询结果拼装完成后，异步写入 Redis，设置 30 分钟过期
        if (isAllQuery && CollUtil.isNotEmpty(voList)) {
            try {
                String json = objectMapper.writeValueAsString(voList);
                redisTemplate.opsForValue().set(CACHE_KEY_NAV_LIST_ALL, json, 30, TimeUnit.MINUTES);
                log.info("📦 导航全量列表已成功写入 Redis 缓存 (Key: {}, TTL: 30分钟)", CACHE_KEY_NAV_LIST_ALL);
            } catch (Exception e) {
                log.error("写入 Redis 缓存失败: {}", e.getMessage());
            }
        }

        return voList;
    }

    @Override
    public void batchAddWithoutTx(boolean makeError) {
        log.warn("⚠️ 【实验 A】开始执行【无事务】批量插入...");
        // 步骤 1：插入第一条导航数据
        SiteNav nav1 = SiteNav.builder()
                .name("【无事务测试】导航A")
                .path("/no-tx-a")
                .icon("⚠️")
                .sort(901)
                .isBlank(0)
                .build();
        this.save(nav1);
        log.info("✅ 步骤1完成：已将【无事务测试】导航A 插入数据库 (ID: {})", nav1.getId());

        // 步骤 2：故意模拟突发异常
        if (makeError) {
            log.error("💥 步骤2触发：业务中途崩溃！(抛出异常)");
            // 无论写不写，先把缓存删一下以便能查到数据库最新脏数据
            redisTemplate.delete(CACHE_KEY_NAV_LIST_ALL);
            throw new BizException("💥 业务逻辑中途崩溃（模拟 1 / 0 或第三方接口挂掉）！");
        }

        // 步骤 3：插入第二条导航数据
        SiteNav nav2 = SiteNav.builder()
                .name("【无事务测试】导航B")
                .path("/no-tx-b")
                .icon("⚠️")
                .sort(902)
                .isBlank(0)
                .build();
        this.save(nav2);
        log.info("✅ 步骤3完成：已将【无事务测试】导航B 插入数据库 (ID: {})", nav2.getId());

        redisTemplate.delete(CACHE_KEY_NAV_LIST_ALL);
    }

    /**
     * 【对比实验 B】：声明式事务模式
     * 关键注解：@Transactional(rollbackFor = Exception.class)
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @Override
    public void batchAddWithTx(boolean makeError) {
        log.warn("🛡️ 【实验 B】开始执行【声明式事务 @Transactional】批量插入...");
        // 步骤 1：插入第一条导航数据
        SiteNav nav1 = SiteNav.builder()
                .name("【事务回滚测试】导航A")
                .path("/tx-a")
                .icon("🛡️")
                .sort(903)
                .isBlank(0)
                .build();
        this.save(nav1);
        log.info("✅ 步骤1完成：已将【事务回滚测试】导航A 插入数据库 (ID: {})", nav1.getId());

        // 步骤 2：故意模拟突发异常
        if (makeError) {
            log.error("💥 步骤2触发：业务中途崩溃！(抛出异常，触发 Spring 回滚)");
            throw new BizException("💥 业务逻辑中途崩溃（模拟 1 / 0 或第三方接口挂掉）！");
        }

        // 步骤 3：插入第二条导航数据
        SiteNav nav2 = SiteNav.builder()
                .name("【事务回滚测试】导航B")
                .path("/tx-b")
                .icon("🛡️")
                .sort(904)
                .isBlank(0)
                .build();
        this.save(nav2);
        log.info("✅ 步骤3完成：已将【事务回滚测试】导航B 插入数据库 (ID: {})", nav2.getId());

        // 🌟【大厂级安全保障】：使用 CacheUtils 工具类，确保事务提交后才清理缓存
        CacheUtils.deleteAfterCommit(CACHE_KEY_NAV_LIST_ALL);
    }

    @Override
    public void cleanTxTestData() {
        // 💥 调用专属手写原生 SQL：彻底从数据库磁盘中物理抹除（DELETE FROM），零脏数据残留！
        int rows = baseMapper.physicalDeleteTestData();
        CacheUtils.deleteAfterCommit(CACHE_KEY_NAV_LIST_ALL);
        log.info("🧹 已彻底【物理删除】事务演练测试数据，共抹平 {} 行（磁盘无残留）", rows);
    }
}
