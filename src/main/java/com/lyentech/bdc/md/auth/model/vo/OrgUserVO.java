package com.lyentech.bdc.md.auth.model.vo;

import com.alibaba.cloud.commons.lang.StringUtils;
import lombok.Data;
import java.io.Serializable;


/**
 * @author YuYi
 * @create 2023/6/26
 * @create 16:05
 */
@Data
public class OrgUserVO implements Serializable {
    private static final long serialVersionUID = 1549817564128351331L;

    private Long userId;

    private String userName;

    private String phone;

    private String email;

    private String userPinyin;

    private String userPinyins;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    private String account;

    /**
     * 手机号格式校验正则
     */
    public static final String PHONE_REGEX = "^1(3\\d|4[5-9]|5[0-35-9]|6[2567]|7[0-8]|8\\d|9[0-35-9])\\d{8}$";

    /**
     * 手机号脱敏筛选正则
     */
    public static final String PHONE_BLUR_REGEX = "(\\d{3})\\d{4}(\\d{4})";

    /**
     * 手机号脱敏替换正则
     */
    public static final String PHONE_BLUR_REPLACE_REGEX = "$1****$2";

    /**
     * 手机号格式校验
     * @param phone
     * @return
     */
    public static final boolean checkPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return false;
        }
        return phone.matches(PHONE_REGEX);
    }


    public String getPhone() {
        boolean checkFlag = checkPhone(phone);
        if (!checkFlag) {
            return phone;
        }
        return phone.replaceAll(PHONE_BLUR_REGEX, PHONE_BLUR_REPLACE_REGEX);
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
