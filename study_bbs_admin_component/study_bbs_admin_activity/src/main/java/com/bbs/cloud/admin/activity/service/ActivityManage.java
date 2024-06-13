package com.bbs.cloud.admin.activity.service;

import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.param.OperatorActivityParam;
import com.bbs.cloud.admin.common.result.HttpResult;

public interface ActivityManage {

    HttpResult createActivity(CreateActivityParam createActivityParam);

    HttpResult startActivity(OperatorActivityParam operatorActivityParam);

    HttpResult endActivity(OperatorActivityParam operatorActivityParam);

    Integer getActivityType();
}
