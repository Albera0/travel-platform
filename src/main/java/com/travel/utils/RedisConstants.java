package com.travel.utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_DESTINATION_TTL = 30L;
    public static final String CACHE_DESTINATION_KEY = "cache:destination:";

    public static final String LOCK_DESTINATION_KEY = "lock:destination:";
    public static final Long LOCK_DESTINATION_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String DESTINATION_LIKED_KEY = "destination:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String DESTINATION_GEO_KEY = "destination:geo:";
    public static final String USER_SIGN_KEY = "sign:";
}
