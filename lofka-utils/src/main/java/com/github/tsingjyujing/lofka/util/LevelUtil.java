package com.github.tsingjyujing.lofka.util;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 等级判断信息
 */
public class LevelUtil {
    private static LevelUtil ourInstance = new LevelUtil();

    public static LevelUtil getInstance() {
        return ourInstance;
    }

    private final Map<String, Integer> levelSetting = Maps.newHashMap();

    private LevelUtil() {
        levelSetting.put("trace", 1);
        levelSetting.put("debug", 2);
        levelSetting.put("info", 3);
        levelSetting.put("warn", 4);
        levelSetting.put("error", 5);
        levelSetting.put("fatal", 6);
    }

    /**
     * 返回对应的等级的数字
     *
     * @param levelInfo 等级信息，不区分大小写
     * @return 相应的等级信息
     */
    public int queryLevel(String levelInfo) {
        final String lowerInfo = levelInfo.toLowerCase();
        if (levelSetting.containsKey(lowerInfo)) {
            return levelSetting.get(lowerInfo);
        } else {
            for (Map.Entry<String, Integer> entry : levelSetting.entrySet()) {
                if (lowerInfo.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
            return 0;
        }
    }
}
