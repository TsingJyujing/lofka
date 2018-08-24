package com.github.tsingjyujing.lofka.util;

/**
 * Constants
 */
public class Constants {
    public static final String INTERFACE_PUSH_SINGLE = "lofka/service/push";
    public static final String INTERFACE_PUSH_BATCH = "lofka/service/push/batch";
    public static final String INTERFACE_PUSH_SINGLE_ZIP = "lofka/service/push/zip";
    public static final String INTERFACE_PUSH_BATCH_ZIP = "lofka/service/push/batch/zip";

    public static final String PROTOCOL = "http://";
    public static final String SLASH = "/";

    /**
     * Auto repair URL settings
     * @param urlSetting URL 配置
     * @param apiRoute API路由
     * @return
     */
    public static String urlProcessing(String urlSetting, String apiRoute) {
        if (urlSetting==null || "".equals(urlSetting)){
            throw new RuntimeException("Lofka URL setting can't be null or empty string");
        }
        if (!urlSetting.startsWith(PROTOCOL)) {
            urlSetting = PROTOCOL + urlSetting;
        }
        if (!urlSetting.endsWith(SLASH + apiRoute)) {
            if (!urlSetting.endsWith(SLASH)) {
                urlSetting += SLASH;
            }
            urlSetting += apiRoute;
        }
        return urlSetting;
    }
}
