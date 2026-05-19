package com.whtc.employee.controller;

import com.whtc.employee.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @GetMapping("/hello")
    public Result<String> hello() {
        log.info("测试接口被调用");
        return Result.success("员工信息管理系统后端服务运行正常！");
    }

    @GetMapping("/time")
    public Result<Map<String, Object>> time() {
        log.info("获取服务器时间");
        Map<String, Object> data = new HashMap<>();
        data.put("serverTime", LocalDateTime.now());
        data.put("message", "服务器时间");
        return Result.success(data);
    }
}
