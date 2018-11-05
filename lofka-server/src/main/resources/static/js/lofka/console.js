/**
 *
 * @param message 消息
 * @param foreground 前台色
 * @returns {string}
 */
function mark_color(message, foreground) {
    return "\x1B[1;3;3" + foreground + "m" + message + "\x1B[0m";
}

// Lofka 控制台颜色
LofkaColors = {
    "black": (message) => mark_color(message, 0),
    "red": (message) => mark_color(message, 1),
    "green": (message) => mark_color(message, 2),
    "yellow": (message) => mark_color(message, 3),
    "blue": (message) => mark_color(message, 4),
    "purple": (message) => mark_color(message, 5),
    "cerulean": (message) => mark_color(message, 6),
    "white": (message) => mark_color(message, 7),
};


LEVEL_COLOR = {
    "TRACE": LofkaColors.cerulean,
    "DEBUG": LofkaColors.blue,
    "INFO": LofkaColors.green,
    "WARN": LofkaColors.yellow,
    "ERROR": LofkaColors.red,
    "FATAL": LofkaColors.red,
};

/**
 *
 * @param text
 * @param limit_ahead
 * @param limit_after
 * @param padding
 * @param shrink_str
 */
function str_size_limit(
    text = "",
    limit_ahead = 3,
    limit_after = 7,
    padding = false,
    shrink_str = "..."
) {
    const max_size_limit = limit_ahead + limit_after + shrink_str.length;
    const raw_size = text.length;
    if (raw_size === max_size_limit) {
        return text
    } else if (raw_size < max_size_limit) {
        if (padding) {
            const padding_size = max_size_limit - raw_size;
            return " ".repeat(padding_size) + text
        } else {
            return text
        }
    } else {
        return text.slice(0, limit_ahead) + shrink_str + text.slice(text.length - limit_after, text.length)
    }
}

/**
 * 从字典中链式取值的算法
 * @param x 字典
 * @param k 键值
 */
function get_linked_key_in_dict(x, k) {
    if (k.indexOf(".") < 0) {
        return x[k]
    } else {
        const ks = k.split(".")
        return get_linked_key_in_dict(x, ks.slice(1, ks.length));
    }
}

/**
 *  格式化机器信息，如果主机名和IP一样就只显示IP，否则在括号中显示主机名称
 * @param host_info
 * @returns {*}
 */
function host_info_format(host_info) {
    if (host_info["name"] === host_info["ip"]) {
        return host_info["ip"];
    } else {
        return `${host_info["ip"]}(${host_info["name"]})`;
    }
}

/**
 * 日志格式化
 * @param message
 * @returns {string}
 */
function message_formatter_raw(message) {
    return JSON.stringify(message, null, 2);
}

/**
 * 时间戳格式化工具
 * @param timestamp 毫秒时间
 * @returns {string}
 */
function format_datetime(timestamp) {
    return new Date(timestamp).toLocaleString();
}

/**
 * 获取值或者返回默认值
 * @param dict
 * @param key
 * @param default_value
 * @returns {*}
 */
function get_or_default(dict, key, default_value) {
    if (key in dict) {
        return dict[key]
    } else {
        return default_value
    }
}

/**
 * 格式化可抛对象
 * @param throwable
 * @returns {string}
 */
function throwable_info_formatter(throwable) {
    const stack_info = throwable["stack_trace"].map(stack => {
        `    at ${
            LofkaColors.red(get_or_default(stack, "method", "NO METHOD"))
            } of ${
            LofkaColors.red(get_or_default(stack, "class", "NO CLASS"))
            }	(${
            LofkaColors.cerulean(get_or_default(stack, "filename", "NO FILE"))
            }#${
            LofkaColors.blue(str(int(get_or_default(stack, "line", "-1"))))
            })`
    }).join("\n");
    const exception_msg = get_or_default(throwable, "message", "NO MESSAGE");
    return `  Exception message: ${LofkaColors.red(exception_msg)}\n${stack_info}`;
}

/**
 * 消息过滤器，可以对来源、等级、IP等进行复杂过滤
 * @param log_data 日志数据
 * @param filter_map 过滤器
 * @returns {boolean}
 */
function message_filter(log_data, filter_map) {
    try {
        filter_map.forEach(
            (value, key, map) => {
                if (!get_linked_key_in_dict(log_data, key) in value) {
                    return false;
                }
            }
        );
        return true;
    } catch (err) {
        return false;
    }
}

/**
 * 日志格式化工具
 * @param log_data
 * @returns {string}
 */
function message_formatter(log_data) {
    const time_formatted = format_datetime(log_data["timestamp"]);
    const host_formatted = ("host" in log_data) ? host_info_format(log_data["host"]) : "Unknown host";
    var log_message = log_data["message"];
    if (typeof(log_message) !== "string") {
        log_message = "\n" + JSON.stringify(log_message, null, 2);
    }

    let logger_output = `${time_formatted} [${
        LEVEL_COLOR[log_data["level"]](str_size_limit(
            log_data["level"],
            limit_ahead = 2,
            limit_after = 3,
            padding = true,
            shrink_str = "-"
        ))
        }] [${
        LofkaColors.purple(str_size_limit(log_data["thread"], limit_ahead = 4, limit_after = 10, padding = true))
        }] ${
        LofkaColors.blue(str_size_limit(log_data["logger"], limit_ahead = 4, limit_after = 10, padding = true))
        } [${
        LofkaColors.yellow(log_data["app_name"])
        }://${
        LofkaColors.cerulean(host_formatted)
        }]\t:${
        LofkaColors.cerulean(log_message)
        }`;
    if ("throwable" in log_data) {
        logger_output += "\n" + throwable_info_formatter(log_data["throwable"]);
    }
    return logger_output;
}

/**
 * Nginx日志格式化工具
 * @param log_data
 * @returns {string}
 */
function nginx_message_formatter(log_data) {
    const time_formatted = format_datetime(log_data["timestamp"]);
    let http_status = "" + log_data["message"]["status"];
    if (http_status.length === 3) {
        if (http_status.startsWith("2")) {
            http_status = LofkaColors.green(log_data["message"]["status"])
        } else if (http_status.startsWith("3")) {
            http_status = LofkaColors.yellow(log_data["message"]["status"])
        } else if (http_status.startsWith("4")) {
            http_status = LofkaColors.red(log_data["message"]["status"])
        } else if (http_status.startsWith("5")) {
            http_status = LofkaColors.purple(log_data["message"]["status"])
        } else {
            http_status = LofkaColors.cerulean(log_data["message"]["status"])
        }
    } else {
        http_status = LofkaColors.cerulean(log_data["message"]["status"])
    }

    return `${
        time_formatted
        } [${
        LofkaColors.green(log_data["app_name"])
        }] [${
        LofkaColors.purple(
            str_size_limit(log_data["message"]["request_method"], limit_ahead = 5, limit_after = 5, padding = true)
        )
        } ${
        LofkaColors.white(log_data["message"]["server_protocol"])
        } ${
        http_status
        }] ${
        LofkaColors.cerulean(
            str_size_limit(log_data["message"]["upstream_addr"], limit_ahead = 5, limit_after = 10, padding = true)
        )
        } --> ${
        LofkaColors.blue(
            str_size_limit(log_data["message"]["remote_addr"], limit_ahead = 5, limit_after = 10, padding = true)
        )
        } ${
        LofkaColors.cerulean(log_data["message"]["host"])
        }\t${
        LofkaColors.blue(log_data["message"]["uri"])
        }`;
}


function auto_format_message(log_data) {
    try {
        if ("type" in log_data) {
            if (log_data["type"].toUpperCase() === "NGINX") {
                return nginx_message_formatter(log_data);
            }
        }
        return message_formatter(log_data);
    } catch (err) {
        console.error(err);
        console.error(log_data);
        // return message_formatter_raw(log_data);
        return "";
    }
}