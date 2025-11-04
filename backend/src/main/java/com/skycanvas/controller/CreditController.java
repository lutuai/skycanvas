package com.skycanvas.controller;

import com.skycanvas.common.PageResult;
import com.skycanvas.common.Result;
import com.skycanvas.entity.CreditLog;
import com.skycanvas.service.CreditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 积分控制器
 */
@Slf4j
@RestController
@RequestMapping("/credit")
public class CreditController {

    @Autowired
    private CreditService creditService;

    /**
     * 获取积分余额
     */
    @GetMapping("/balance")
    public Result<Integer> getBalance(@RequestAttribute("userId") Long userId) {
        Integer balance = creditService.getBalance(userId);
        return Result.success(balance);
    }

    /**
     * 获取积分记录
     */
    @GetMapping("/logs")
    public Result<PageResult<CreditLog>> getLogs(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            @RequestAttribute("userId") Long userId) {
        
        PageResult<CreditLog> result = creditService.getCreditLogs(userId, current, size);
        return Result.success(result);
    }
}

