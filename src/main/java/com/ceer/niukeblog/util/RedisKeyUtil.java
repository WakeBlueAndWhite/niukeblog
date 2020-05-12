package com.ceer.niukeblog.util;

/**
 * @ClassName RedisKeyUtil
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/4 15:27
 * @Version 1.0
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";

    /**
     * @Description: // 某个实体的赞
     * // like:entity:entityType:entityId -> set(userId)
     * @param:
     * @return:
     * @date: 2020/5/4 15:29
     */
    public static String getEntityLikeKey(Integer entityType, Integer entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * @Description: // 某个用户的赞
     * // like:user:userId -> int
     * @param:
     * @return:
     * @date: 2020/5/4 15:29
     */
    public static String getUserLikeKey(Integer userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * @Description: // 某个用户关注的实体
     * // followee:userId:entityType -> zset(entityId,now)
     * @param:
     * @return:
     * @date: 2020/5/4 21:26
     */
    public static String getFolloweeKey(Integer userId, Integer entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * @Description: // 某个实体拥有的粉丝
     * // follower:entityType:userId -> zset(entityId,now)
     * @param:
     * @return:
     * @date: 2020/5/4 21:26
     */
    public static String getFollowerKey(Integer entityType, Integer userId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + userId;
    }

    /**
     * @Description: 验证码
     * @param:
     * @return:
     * @date: 2020/5/5 17:09
     */
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * @Description: 登录许可
     * @param:
     * @return:
     * @date: 2020/5/5 18:44
     */
    public static String getTicket(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * @Description: 用户
     * @param:
     * @return:
     * @date: 2020/5/5 19:33
     */
    public static String getUserkey(Integer userId){
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * @Description: 单日UV
     * @param:
     * @return:
     * @date: 2020/5/9 17:17
     */
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * @Description: 区间UV
     * @param:
     * @return:
     * @date: 2020/5/9 17:17
     */
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * @Description: 单日活跃用户
     * @param:
     * @return:
     * @date: 2020/5/9 17:18
     */
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * @Description: 区间活跃用户
     * @param:
     * @return:
     * @date: 2020/5/9 17:18
     */
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * @Description: 帖子分数
     * @param:
     * @return:
     * @date: 2020/5/9 22:42
     */
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
