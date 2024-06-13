package com.bbs.cloud.admin.activity.controller;

import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.param.OperatorActivityParam;
import com.bbs.cloud.admin.activity.service.ActivityService;
import com.bbs.cloud.admin.common.result.HttpResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @PostMapping("create")
    public HttpResult createActivity(@RequestBody CreateActivityParam createActivityParam){
        return activityService.createActivity(createActivityParam);
    }

    @PostMapping("start")
    public HttpResult startActivity(@RequestBody OperatorActivityParam operatorActivityParam){
        return activityService.startActivity(operatorActivityParam);
    }

    @PostMapping("end")
    public HttpResult endActivity(@RequestBody OperatorActivityParam operatorActivityParam){
        return activityService.endActivity(operatorActivityParam);
    }
}
