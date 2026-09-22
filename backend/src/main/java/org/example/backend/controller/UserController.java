package org.example.backend.controller;

import org.example.backend.dto.BasicResponse;
import org.example.backend.entity.SysUser;
import org.example.backend.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户接口控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 查询所有用户列表（测试数据库连通性）
     */
    @GetMapping("/list")
    public BasicResponse<List<SysUser>> getUserList() {
        // selectList(null) 表示无条件查询全部数据
        List<SysUser> users = sysUserMapper.selectList(null);
        return BasicResponse.success(users);
    }
}

