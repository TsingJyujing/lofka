/**
 * 校验函数
 * @param log 日志对象
 * @returns {boolean} 是否通过校验
 */
function filter(log) {return false;}

/**
 * Java 调用的函数
 * @param log_str 字符串形式的日志
 * @returns {boolean} 是否通过校验
 */
function invoke_filter(log_str) {
    return filter(JSON.parse(log_str));
}
