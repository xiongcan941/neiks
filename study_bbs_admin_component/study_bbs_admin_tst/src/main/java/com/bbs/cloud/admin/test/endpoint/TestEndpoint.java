package com.bbs.cloud.admin.test.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("endpoint")
public class TestEndpoint {

    @GetMapping("/feigh1")
    public String testFeigh() {
        throw new RuntimeException("异常.................");
        
    }
}
