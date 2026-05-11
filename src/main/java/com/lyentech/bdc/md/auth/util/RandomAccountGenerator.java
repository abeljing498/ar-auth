package com.lyentech.bdc.md.auth.util;

import java.util.Random;

/**
 * @author yuyi
 */
public class RandomAccountGenerator {

    /**
     * 获取生成的随机数
     * @param length 输入字节长度
     * @return
     */
    public static String getRandomAccount(int length){
        String val = "";

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            //输出字母还是数字
            String charOrNUm = random.nextInt(2) % 2 == 0 ? "char" : "num";

            //字符串
            if ("char".equalsIgnoreCase(charOrNUm)) {
                int choice = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char) (choice + random.nextInt(26));
            } else if ("num".equalsIgnoreCase(charOrNUm)) {
                val += String.valueOf(random.nextInt(7));
            }

        }
        return val;
    }

    /**
     * 获得一个随机密码
     *
     * @param length 密码长度
     * @return 返回密码
     */
    public static String getRandomPassword(int length) {
        StringBuffer password = new StringBuffer();
        for (int i = 0; i < length; i++) {
            //assic 对应表48-57 {0-9} A-Z {65-90} a-z {97-122} 特殊字符{32-127} 32-Space 127-DEL
            //随机密码生成器
            int mm = (int) Math.round(33 + Math.random() * 93);
            char pw = (char) mm == 92 ? 48 : (char) mm;
            password.append(pw);
        }
        return password.toString();
    }
}
