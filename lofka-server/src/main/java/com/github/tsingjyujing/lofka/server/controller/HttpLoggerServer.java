package com.github.tsingjyujing.lofka.server.controller;

import com.github.tsingjyujing.lofka.basic.LoggerJson;
import com.github.tsingjyujing.lofka.server.queue.MessageQueueCluster;
import com.github.tsingjyujing.lofka.server.socket.LoggerSocketServerCluster;
import com.github.tsingjyujing.lofka.server.util.Constants;
import com.github.tsingjyujing.lofka.server.util.LofkaConfigUtil;
import com.github.tsingjyujing.lofka.server.util.SocketServerUtil;
import com.github.tsingjyujing.lofka.server.websocket.LoggerPushWebSocket;
import com.github.tsingjyujing.lofka.util.CompressUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Spring Boot 服务类，对外提供接口服务
 *
 * @author tsingjyujing@163.com
 */
@Controller
@EnableAutoConfiguration
public class HttpLoggerServer {
    private final static Gson GSON = new Gson();
    private final String successfullyWritten;
    private final MessageQueueCluster messageQueueCluster = MessageQueueCluster.getInstance();
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpLoggerServer.class);
    /**
     * 可接受的等级信息
     */
    private final Set<String> levelSet = Sets.newHashSet(
            "TRACE",
            "DEBUG",
            "INFO",
            "WARN",
            "ERROR",
            "FATAL"
    );
    private final String systemApplicationName = LofkaConfigUtil.getInstance().getApplicationName();

    /**
     * 最大转发次数
     */
    private static final int MAX_ROUTER_SIZE = 16;

    /**
     * 初始化成功返回消息
     */
    public HttpLoggerServer() {
        final Map<String, Object> returnValue = Maps.newHashMap();
        returnValue.put("status", 200);
        returnValue.put("server_app_name", systemApplicationName);
        returnValue.put("message", "post LOGGER json successfully.");
        successfullyWritten = GSON.toJson(returnValue);
    }

    /**
     * 将日志推入队列的接口
     *
     * @param request POST的BODY为JSON格式的日志
     * @return
     * @throws Exception
     */
    @RequestMapping(
            value = Constants.INTERFACE_PUSH_SINGLE,
            method = RequestMethod.POST,// GET 仅仅建议在调试的时候使用
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE}
    )
    @ResponseBody
    public String pushLog(
            HttpServletRequest request
    ) throws Exception {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        request.getInputStream()
                )
        );
        final Map result = new Gson().fromJson(reader, HashMap.class);
        messageQueueCluster.pushQueue(GSON.toJson(messageProcessor(result, request)));
        return successfullyWritten;
    }

    /**
     * 将日志推入队列的接口
     *
     * @param request POST的BODY为JSON格式的日志
     * @return
     * @throws Exception
     */
    @RequestMapping(
            value = Constants.INTERFACE_PUSH_SINGLE_ZIP,
            method = RequestMethod.POST,// GET 仅仅建议在调试的时候使用
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE}
    )
    @ResponseBody
    public String pushCompressedLog(
            HttpServletRequest request
    ) throws Exception {
        final Map result = new Gson().fromJson(
                CompressUtil.decompressUTF8String(
                        request.getInputStream()
                ),
                HashMap.class
        );
        messageQueueCluster.pushQueue(
                GSON.toJson(
                        messageProcessor(
                                result,
                                request
                        )
                )
        );
        return successfullyWritten;
    }


    /**
     * 将日志推入队列的接口
     *
     * @param request POST的BODY为JSON格式的日志，应该输入一个LIST，里面是多个MAP
     * @return
     * @throws Exception
     */
    @RequestMapping(
            value = Constants.INTERFACE_PUSH_BATCH,
            method = RequestMethod.POST,// GET 仅仅建议在调试的时候使用
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE}
    )
    @ResponseBody
    public String batchPushLog(
            HttpServletRequest request
    ) throws Exception {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        final List results = new Gson().fromJson(reader, ArrayList.class);
        final int sumCount = results.size();
        if (sumCount <= 0) {
            throw new RuntimeException("No data in list.");
        }
        int succeeded = multiMessageProcessor(results, request);
        if (succeeded <= 0) {
            throw new RuntimeException("No data saved, all failed.");
        }
        final Map<String, Object> returnValue = Maps.newHashMap();
        returnValue.put("status", 200);
        returnValue.put("server_app_name", systemApplicationName);
        returnValue.put("message", new Document("successed", succeeded).append("count", sumCount));
        return GSON.toJson(returnValue);
    }

    /**
     * 将日志推入队列的接口
     *
     * @param request POST的BODY为JSON格式的日志，应该输入一个LIST，里面是多个MAP
     * @return
     * @throws Exception
     */
    @RequestMapping(
            value = Constants.INTERFACE_PUSH_BATCH_ZIP,
            method = RequestMethod.POST,// GET 仅仅建议在调试的时候使用
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE}
    )
    @ResponseBody
    public String batchPushCompressedLog(
            HttpServletRequest request
    ) throws Exception {
        final List results = new Gson().fromJson(CompressUtil.decompressUTF8String(request.getInputStream()), ArrayList.class);
        final int sumCount = results.size();
        if (sumCount <= 0) {
            throw new RuntimeException("No data in list.");
        }
        int succeeded = multiMessageProcessor(results, request);
        if (succeeded <= 0) {
            throw new RuntimeException("No data saved, all failed.");
        }
        final Map<String, Object> returnValue = Maps.newHashMap();
        returnValue.put("status", 200);
        returnValue.put("server_app_name", systemApplicationName);
        returnValue.put("message", new Document("successed", succeeded).append("count", sumCount));
        return GSON.toJson(returnValue);
    }

    /**
     * 多消息处理器
     *
     * @param results 多个消息
     * @param request
     * @return
     */
    private int multiMessageProcessor(Iterable results, HttpServletRequest request) {
        int succeeded = 0;
        for (Object result : results) {
            if (result instanceof Map) {
                try {
                    messageQueueCluster.pushQueue(GSON.toJson(messageProcessor((Map) result, request)));
                    succeeded++;
                } catch (Exception ex) {
                    LOGGER.info("Error while saving data", ex);
                }
            } else {
                LOGGER.info("Object in list is not a Map.");
            }
        }
        return succeeded;
    }

    /**
     * 消息处理器
     *
     * @param result  消息数据
     * @param request 请求信息
     * @return
     */
    private Map messageProcessor(Map result, HttpServletRequest request) {
        if (result.containsKey(LoggerJson.TAG_LEVEL)) {
            final Object level = result.get(LoggerJson.TAG_LEVEL);
            if (!levelSet.contains(level)) {
                LOGGER.warn("Level {} not in level set.", level);
            }
        }
        if (!result.containsKey(LoggerJson.TAG_MESSAGE)) {
            throw new IllegalArgumentException("Field 'message' not found.");
        }
        if (!result.containsKey(LoggerJson.TAG_TIMESTAMP)) {
            result.put(LoggerJson.TAG_TIMESTAMP, (double) System.currentTimeMillis());
        }
        // 处理Host信息，在最后加上这一层的IP
        if (!result.containsKey(LoggerJson.TAG_HOST)) {
            result.put(LoggerJson.TAG_HOST, SocketServerUtil.analysisHostInfo(request));
        }

        if (result.containsKey(LoggerJson.TAG_REDIRECT_LINK) && (result.get(LoggerJson.TAG_REDIRECT_LINK) instanceof List)) {
            List<String> ipTable = Lists.newArrayList((List<String>) result.get(LoggerJson.TAG_REDIRECT_LINK));
            if (ipTable.size() > MAX_ROUTER_SIZE) {
                throw new RuntimeException(String.format("Stack size of routers too large (>%d): %s", MAX_ROUTER_SIZE, GSON.toJson(ipTable)));
            }
            ipTable.add(SocketServerUtil.getIPAddress(request));
            result.put(LoggerJson.TAG_REDIRECT_LINK, ipTable);
        } else {
            result.put(LoggerJson.TAG_REDIRECT_LINK, Lists.newArrayList(SocketServerUtil.getIPAddress(request)));
        }

        if (!result.containsKey(LoggerJson.TAG_APP_NAME)) {
            if (!"".equals(systemApplicationName)) {
                result.put(LoggerJson.TAG_APP_NAME, systemApplicationName);
            } else {
                result.put(LoggerJson.TAG_APP_NAME, "no_app_name");
            }
        } else {
            if (!"".equals(systemApplicationName)) {
                result.put(LoggerJson.TAG_APP_NAME, systemApplicationName + "/" + result.get(LoggerJson.TAG_APP_NAME).toString());
            }
        }

        final Object message = result.get(LoggerJson.TAG_MESSAGE);

        try {
            if (message instanceof String) {
                final Document doc = Document.parse(String.valueOf(message));
                result.put(LoggerJson.TAG_MESSAGE, doc);
            }
        } catch (Exception ex) {
            //pass
        }
        return result;
    }


    /**
     * 将日志推入队列的接口(不作任何检验和处理，不是很安全)
     *
     * @param messageBody 消息正文
     * @return
     * @throws Exception
     */
    @Deprecated
    @RequestMapping(
            value = Constants.INTERFACE_PUSH_UNSAFE,
            method = RequestMethod.POST,// GET 仅仅建议在调试的时候使用
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE}
    )
    @ResponseBody
    public String unsafePushLog(
            @RequestBody String messageBody
    ) throws Exception {
        messageQueueCluster.pushQueue(messageBody);
        return successfullyWritten;
    }

    /**
     * 将日志推入Kafka相应的topic的接口
     * 非LoggerJson格式的文本日志都使用这个接口推送
     * <p>
     * 格式也是JSON，必需字段是topic和message（可以是文本或者JSON）
     *
     * @param request POST的BODY为JSON格式的日志
     * @return
     * @throws Exception
     */
    @Deprecated
    @RequestMapping(
            value = Constants.INTERFACE_KAFKA_PUSH,
            method = RequestMethod.POST,// GET 仅仅建议在调试的时候使用
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE}
    )
    @ResponseBody
    public String pushToTopic(
            HttpServletRequest request
    ) throws Exception {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        final Map result = new Gson().fromJson(reader, HashMap.class);
        if (!(result.containsKey("topic") && result.containsKey("message"))) {
            throw new IllegalArgumentException("Field 'message' not found.");
        }
        // 解析出HOST信息一并放入
        result.put(LoggerJson.TAG_HOST, SocketServerUtil.analysisHostInfo(request));
        MessageQueueCluster.getInstance().getKafkaConnector().pushMessageToTopicAsync(
                result.get("topic").toString(),
                GSON.toJson(result)
        );
        return successfullyWritten;
    }


    /**
     * 监控Socket服务器的状态
     *
     * @return
     */
    @RequestMapping(
            value = Constants.INTERFACE_SERVER_MONITOR,
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE}
    )
    @ResponseBody
    public String monitorSocketServerStatus() {
        final Document doc = new Document();
        doc.append("logger_socket_server", LoggerSocketServerCluster.getInstance().getAllStatus());
        doc.append("web_socket", new Document("client_count", LoggerPushWebSocket.getOnlineCount()));
        return GSON.toJson(doc);
    }

    /**
     * 我一句话不说也不好
     *
     * @return
     */
    @RequestMapping(
            value = "/",
            method = RequestMethod.GET,
            produces = {
                    MediaType.TEXT_HTML_VALUE
            }
    )
    public ModelAndView homePageTest() {
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("systemApplicationName", "".equals(systemApplicationName) ? "[未设置名称]" : systemApplicationName);
        return modelAndView;
    }

    /**
     * 用于显示的控制台
     *
     * @return
     */
    @RequestMapping(
            value = "/lofka/console",
            method = RequestMethod.GET,
            produces = {
                    MediaType.TEXT_HTML_VALUE
            }
    )
    public ModelAndView consolePage() {
        final ModelAndView modelAndView = new ModelAndView("console");
        return modelAndView;
    }


}
