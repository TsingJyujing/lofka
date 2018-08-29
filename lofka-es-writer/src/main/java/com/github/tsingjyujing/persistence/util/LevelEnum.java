package com.github.tsingjyujing.persistence.util;

/**
 * Created with IntelliJ IDEA.
 *
 * @author, zhuyh
 * @date, 2018/8/22
 */
public enum  LevelEnum {

    TRACE("TRACE",5 * 60),
    DEBUG("DEBUG", 10 * 60),
    INFO("INFO", 24 * 60 * 60),
    WARN("WARN", 31 * 24 * 60 * 60),
    ERROR("ERROR", 366 * 24 * 60 * 60),
    FATAL("FATAL", 732 * 24 * 60 * 60),
    DEFAULT("DEFAULT", 31 * 24 * 60 * 60),
    NGINX("NGINX", 190 * 24 * 60 * 60);
    
    private String name;
    private int expiredTime;


    LevelEnum(String name, int expiredTime) {
        this.name = name;
        this.expiredTime = expiredTime;
    }

    public static Integer getExpiredTime(String name){
        for(LevelEnum e:LevelEnum.values()){
            if(e.getName().equals(name)){
                return e.getExpiredTime();
            }
        }
        return LevelEnum.DEFAULT.getExpiredTime();
    }

    public String getName() {
        return name;
    }


    public int getExpiredTime() {
        return expiredTime;
    }

}
