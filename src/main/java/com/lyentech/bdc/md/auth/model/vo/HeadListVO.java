package com.lyentech.bdc.md.auth.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author YuYi
 * @create 2022/7/1
 * @create 11:29
 */
@Data
public class HeadListVO implements Serializable {
    private static final long serialVersionUID = 2001455843107079128L;
    private List<AppHeadVO> headList;
    private String name;
    private String ssoAppId;
    private List<String> loginTypeList;
    private List<TenantVO> tenantVOList;
    private String homePageUrl;
    private String appRemark;
    private Boolean isOutWeb;
}
