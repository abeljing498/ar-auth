package com.lyentech.bdc.md.auth.service;

import com.lyentech.bdc.md.auth.model.entity.MdUserOrg;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyentech.bdc.md.auth.model.param.OrgUserParam;

public interface MdUserOrgService extends IService<MdUserOrg> {
    /**
     * 新增用户组织关系
     * @param orgUserParam
     */
    void addOrgUser(OrgUserParam orgUserParam) throws Exception;
}