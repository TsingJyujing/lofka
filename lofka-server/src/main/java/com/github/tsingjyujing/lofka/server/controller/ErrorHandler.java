package com.github.tsingjyujing.lofka.server.controller;


import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理页面
 */
@ControllerAdvice
class ErrorHandler {
    /**
     * 可以设置是否输出调试信息
     */
    private final static boolean IS_DEBUG = true;
    private final static Gson GSON = new Gson();
    private final static Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    /**
     * @param req HTTP请求
     * @param ex  输入的异常
     * @return
     */
    @ExceptionHandler(
            value = Exception.class
    )
    @ResponseBody
    public String defaultErrorHandler(HttpServletRequest req, Exception ex) {
        Map<String, Object> errorInfo = new HashMap<>(16);

        errorInfo.put("status", 100);
        errorInfo.put("message", ex.getMessage());
        errorInfo.put("data", new String[]{});

        if (IS_DEBUG) {
            errorInfo.put("timestamp", System.currentTimeMillis());
            errorInfo.put("url", req.getRequestURL());
            errorInfo.put("stack", ex.getStackTrace());
            errorInfo.put("method", req.getMethod());
        }
        final String returnData = GSON.toJson(errorInfo);
        LOGGER.info(String.format("Error page return to client: %s", returnData), ex);
        return returnData;
    }

}
