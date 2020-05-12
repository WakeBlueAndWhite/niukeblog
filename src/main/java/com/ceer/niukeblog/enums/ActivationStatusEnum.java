package com.ceer.niukeblog.enums;
/**
 *@ClassName ActivationStatusEnum
 *@Description 激活状态枚举
 *@Author ceer
 *@Date 2020/4/30 14:12
 *@Version 1.0
 */
public enum ActivationStatusEnum {
    /**
     *  激活成功
     */
    ACTIVATION_SUCCESS(0),
    /**
     * 重复激活
     */
    ACTIVATION_REPEAT(1),
    /**
     * 激活失败
     */
    ACTIVATION_FAILURE(2),
    ;

    private Integer type;

    ActivationStatusEnum(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
