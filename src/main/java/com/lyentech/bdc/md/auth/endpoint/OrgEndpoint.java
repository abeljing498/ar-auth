package com.lyentech.bdc.md.auth.endpoint;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.dao.MdUserOperationLogMapper;
import com.lyentech.bdc.md.auth.model.entity.MdOrgOperationLog;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.param.OrgOrderParam;
import com.lyentech.bdc.md.auth.model.param.OrgParam;
import com.lyentech.bdc.md.auth.model.param.QueryOrgNameParam;
import com.lyentech.bdc.md.auth.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
@RestController
@RequestMapping("/org")
public class OrgEndpoint {

    @Autowired
    private OrgService orgService;

    /**
     * 添加组织
     *
     * @param auth2Authentication
     * @param orgParam
     * @return
     */
    @PreAuthorize("#oauth2.hasScope('profile')")
    @PostMapping("/add")
    public ResultEntity add(OAuth2Authentication auth2Authentication, @RequestBody OrgParam orgParam) {
        if (ObjectUtils.isEmpty(orgParam.getType()) || orgParam.getType() == 0) {
            orgService.add(orgParam);
            return ResultEntity.success();
        } else {
            return ResultEntity.success(orgService.add(orgParam));
        }
    }

    /**
     * 更新组织
     *
     * @param auth2Authentication
     * @param orgParam
     * @return
     */
    @PostMapping("/update")
    public ResultEntity update(OAuth2Authentication auth2Authentication, @RequestBody OrgParam orgParam) {
        orgService.update(orgParam);
        return ResultEntity.success();
    }

    @PostMapping("updateOrder")
    public ResultEntity updateOrder(@RequestBody List<OrgOrderParam> orderParamList) {
        orgService.updateOrder(orderParamList);
        return ResultEntity.success();
    }

    /**
     * 根据tenantId获取组织架构树
     *
     * @param tenantId
     * @return
     */
    @GetMapping("/getTree")
    public ResultEntity getTreeByTenantId(@RequestParam Long tenantId) {
        return ResultEntity.success(orgService.getTreeByTenantId(tenantId));
    }

    @GetMapping("/getTreeByUser")
    public ResultEntity getTreeByUser(@RequestParam Long tenantId, @RequestParam Long userId) {
        return ResultEntity.success(orgService.getTreeByUser(tenantId, userId));
    }

    /**
     * 删除组织
     *
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ResultEntity delete(@RequestParam Long id) {
        orgService.delete(id);
        return ResultEntity.success();
    }

    @PostMapping("/deleteOrg")
    @PreAuthorize("#oauth2.hasScope('profile')")
    public ResultEntity deleteOrg(@RequestBody OrgParam orgParam) {
        orgService.deleteOrg(orgParam);
        return ResultEntity.success();
    }

    /**
     * 根据id获取下级组织
     *
     * @param pageNum
     * @param pageSize
     * @param id
     * @return
     */
    @GetMapping("/getSubById")
    public ResultEntity getSubById(@RequestParam Long pageNum, @RequestParam Long pageSize, @RequestParam Long id, @RequestParam Long tenantId) {
        return ResultEntity.success(orgService.getSubById(pageNum, pageSize, id, tenantId));
    }

    /**
     * 根据id获取组织详情
     *
     * @param id
     * @return
     */
    @GetMapping("/getDetail")
    public ResultEntity getDetail(@RequestParam Long id) {
        return ResultEntity.success(orgService.getDetail(id));
    }

    @GetMapping("/getOrgName")
    public ResultEntity getOrgName(@RequestParam List<Long> ids, @RequestParam Long height) {
        QueryOrgNameParam nameParam = new QueryOrgNameParam();
        nameParam.setIds(ids);
        nameParam.setHeight(height);
        return ResultEntity.success(orgService.getOrgName(nameParam));
    }

    @GetMapping("/getOrgByCid")
    public ResultEntity getOrgByCid(@RequestParam String customId, @RequestParam Long tenantId) {
        return ResultEntity.success(orgService.getOrgByCid(customId, tenantId));
    }

    @GetMapping("/getUserNum")
    public ResultEntity getUserNum(@RequestParam Long id, Long tenantId) {
        return ResultEntity.success(orgService.getOrgUserNum(id, tenantId));
    }

    @GetMapping("/getUserStatusNum")
    public ResultEntity getUserStatusNum(@RequestParam(required = false) Long id, Long tenantId, @RequestParam(required = false) String appKey) {
        return ResultEntity.success(orgService.getUserStatusNum(id, tenantId, appKey));
    }

    @GetMapping("/meOrg")
    public ResultEntity getMeOrg(@RequestParam Long userId, @RequestParam Long tenantId) {
        return ResultEntity.success(orgService.getMeOrg(userId, tenantId));
    }

    @GetMapping("/treeOrgUsers")
    public ResultEntity treeOrgUsers(@RequestParam Long tenantId) {
        return ResultEntity.success(orgService.treeOrgUsers(tenantId));
    }

    @GetMapping("/treeOrgUsersByOrgId")
    public ResultEntity treeOrgUsersByOrgId(@RequestParam Long tenantId, @RequestParam Long orgId) {
        return ResultEntity.success(orgService.treeOrgUsersByOrgId(tenantId, orgId));
    }
}
