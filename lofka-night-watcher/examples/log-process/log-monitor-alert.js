// 使用默认的open和close（也就是啥也不做）


FILTERS = {
    "big_data_alarms": {
        "filter_func": function (log_data) {
            return log_data.startsWith("cvnavi/bigdata/online");
        },
        "notice_info": USER_GROUP["big_data"].map(function (user) {
            return {
                "user_id": user_id,
                "method": NOTIFY_CODE.mail
            }
        })
    }
};

/**
 * 用户ID表
 */
USER_ID_MAP = {
    "zhangsan": "ef40000cced",
    "lisi": "efd1032a80"
};

/**
 * 用户组定义
 */
USER_GROUP = {
    "big_data": [
        "zhangsan",
        "lisi"
    ],
    "backend": [
        "zhangsan"
    ]
};

/**
 * 通知编码
 */
NOTIFY_CODE = {
    "tel": "10001",
    "sms": "10002",
    "mail": "10003",
    "dingding": "11001",
    "wechat": "11002"
};

/**
 * 数据处理
 * @param str_data 日志JSON文本数据
 */
function processing_data(str_data) {
    var log_data = JSON.stringify(str_data);
    FILTERS.forEach(function (f) {
        if (f.filter_func(log_data)) {
            f.notice_info.forEach(function (notice) {
                notify_people(notice.user_id, log_data, notice.method);
            })
        }
    });
}


/**
 * 调用内部通信的API通知相应的
 * @param user_id 用户ID
 * @param log_data 日志数据
 * @param notify_method 通知方法
 */
function notify_people(user_id, log_data, notify_method) {
    var post_data = {
        "method": notify_method,
        "data": JSON.stringify(log_data, null, 2),
        "user_id": user_id
    };
    $.post(
        "http://172.16.9.179:8080/api/notify/",
        post_data
        , function (resp_str) {
            var response_data = JSON.parse(resp_str);
            if (response_data["status"] !== 200) {
                // 注意这里不能对这条日志联动
                // 否则可能自激振荡
                console.err("Error while notifying: " + JSON.stringify(post_data))
            } else {
                console.log("Notify successfully:" + JSON.stringify(post_data))
            }
        }
    )
}