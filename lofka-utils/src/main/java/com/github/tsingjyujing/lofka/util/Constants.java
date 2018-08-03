package com.github.tsingjyujing.lofka.util;

/**
 * Constants
 */
public class Constants {
    public static final String INTERFACE_PUSH_SINGLE = "lofka/service/push";
    public static final String INTERFACE_PUSH_BATCH = "lofka/service/push/batch";
    public static final String INTERFACE_PUSH_SINGLE_ZIP = "lofka/service/push/zip";
    public static final String INTERFACE_PUSH_BATCH_ZIP = "lofka/service/push/batch/zip";

    public static final String PROTOCAL = "http://";

    /**
     * Auto repair URL settings
     *
     * @param urlSetting
     * @return
     */
    public static String urlProcessing(String urlSetting, String apiRoute) {
        if (!urlSetting.startsWith(PROTOCAL)) {
            urlSetting = PROTOCAL + urlSetting;
        }
        if (!urlSetting.endsWith("/" + apiRoute)) {
            if (!urlSetting.endsWith("/")) {
                urlSetting += "/";
            }
            urlSetting += apiRoute;
        }
        return urlSetting;
    }
}
