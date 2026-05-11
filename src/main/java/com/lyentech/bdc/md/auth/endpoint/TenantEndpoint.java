package com.lyentech.bdc.md.auth.endpoint;

import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.model.param.OrgParam;
import com.lyentech.bdc.md.auth.model.param.TenantParam;
import com.lyentech.bdc.md.auth.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * @Author :yan
 * @Date :Create in 2022/5/25
 * @Description :
 */

@RestController
@RequestMapping("/tenant")
public class TenantEndpoint {

    @Autowired
    private TenantService tenantService;


    @PostMapping("/add")
    public ResultEntity add(OAuth2Authentication auth2Authentication, @RequestBody TenantParam tenantParam) {
        return ResultEntity.success(tenantService.addTenant(tenantParam));
    }

    /**
     * 更新组织
     * @param auth2Authentication
     * @param tenantParam
     * @return
     */
    @PostMapping("/update")
    public ResultEntity update(OAuth2Authentication auth2Authentication, @RequestBody TenantParam tenantParam) {
        tenantService.updateTenant(tenantParam);
        return ResultEntity.success();
    }


    @GetMapping("/getOrgByTenant")
    public ResultEntity getOrg(@RequestParam Long tenantId) {

        return ResultEntity.success(tenantService.getOrgByTenant(tenantId));
    }

    @GetMapping("/getList")
    public ResultEntity getList(@RequestParam String appId,@RequestParam Long userId) {

        return ResultEntity.success(tenantService.getList(appId,userId));
    }

    @GetMapping("/getName")
    public ResultEntity getName(@RequestParam Long tenantId) {
        return ResultEntity.success(tenantService.getName(tenantId));
    }
}
