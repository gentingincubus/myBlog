package org.example.backend.controller;

import org.example.backend.dto.BasicResponse;
import org.example.backend.service.ISiteNavService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据库事务（@Transactional）实战演练控制器
 */
@RestController
@RequestMapping("/api/lab/tx")
public class TxLabController {

    @Autowired
    private ISiteNavService siteNavService;

    /**
     * 对比实验 A：无事务测试
     */
    @PostMapping("/no-tx")
    public BasicResponse<String> testNoTx(@RequestParam(defaultValue = "true") boolean makeError) {
        siteNavService.batchAddWithoutTx(makeError);
        return BasicResponse.success("批量添加成功（无事务）");
    }

    /**
     * 对比实验 B：声明式事务测试
     */
    @PostMapping("/with-tx")
    public BasicResponse<String> testWithTx(@RequestParam(defaultValue = "true") boolean makeError) {
        siteNavService.batchAddWithTx(makeError);
        return BasicResponse.success("批量添加成功（有事务保障）");
    }

    /**
     * 一键清理演练脏数据
     */
    @PostMapping("/clean")
    public BasicResponse<String> cleanTestData() {
        siteNavService.cleanTxTestData();
        return BasicResponse.success("已成功清理事务测试数据！");
    }
}
