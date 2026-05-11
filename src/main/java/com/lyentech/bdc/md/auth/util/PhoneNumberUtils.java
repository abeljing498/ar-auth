package com.lyentech.bdc.md.auth.util;

public class PhoneNumberUtils {

    /**
     * 对手机号进行脱敏处理
     *
     * @param phoneNumber 原始手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return "";
        }

        // 检查手机号是否为11位
        if (phoneNumber.length() != 11) {
            return phoneNumber; // 如果不是11位，直接返回原始手机号
        }

        // 提取前三位和后四位，并用四个星号替换中间部分
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
    }
}