package com.lyentech.bdc.md.auth.util;

/**
 * @author 260442
 */
public class CharacterUntil {
    /**
     * 邮箱进行转换
     * @param email
     * @return
     */
    public static String emailToChange(String email) {
        // 检查字符串长度是否至少为2，以避免StringIndexOutOfBoundsException
        if (email.length() >= 2) {
            char secondChar = email.charAt(1); // 获取第二个字符（索引为1）
            if (Character.isDigit(secondChar)) {
                char firstChar = Character.toUpperCase(email.charAt(0));
                // 将大写字符与字符串的剩余部分拼接
                String result = firstChar + email.substring(1);
                return result;
            } else {
                return email;
            }
        } else {
            System.out.println("字符串长度不足，无法判断第二个字符是否为数字。");
        }

        return email;
    }

    public static void main(String[] args) {
        String result= emailToChange("ar12122");
        System.out.println(result);
    }
}
