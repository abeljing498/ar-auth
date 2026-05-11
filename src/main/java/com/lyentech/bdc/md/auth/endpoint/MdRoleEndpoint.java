package com.lyentech.bdc.md.auth.endpoint;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.dao.TenantRoleMapper;
import com.lyentech.bdc.md.auth.model.entity.TenantRole;
import com.lyentech.bdc.md.auth.model.param.RoleParam;
import com.lyentech.bdc.md.auth.model.vo.PushMessageDto;
import com.lyentech.bdc.md.auth.service.AuthService;
import com.lyentech.bdc.md.auth.service.MdRoleService;
import com.lyentech.bdc.md.auth.service.SendMessageToWebSocketService;
import com.lyentech.bdc.md.auth.service.TenantRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 角色
 */
@RestController
@RequestMapping("/role")
public class MdRoleEndpoint {
    @Autowired
    MdRoleService mdRoleService;
    @Autowired
    AuthService authService;
    @Autowired
    TenantRoleService tenantRoleService;
    @Autowired
    private SendMessageToWebSocketService sendMessageToWebSocketService;
    @Resource
    private TenantRoleMapper tenantRoleMapper;

    /**
     * 新增角色
     *
     * @param roleParam
     * @return
     */
    @PreAuthorize("#oauth2.hasScope('profile')")
    @PostMapping("/add")
    public ResultEntity addRole(@RequestBody RoleParam roleParam) {
        Long id = mdRoleService.add(roleParam);
        PushMessageDto pushMessageDto = new PushMessageDto();
        pushMessageDto.setRoleId(Long.toString(id));
        pushMessageDto.setTenantId(String.valueOf(roleParam.getTenantId()));
        pushMessageDto.setStatus("ADD");
        pushMessageDto.setAppKey(roleParam.getAppId());
        sendMessageToWebSocketService.sendMessage(pushMessageDto);
        Map<String, Object> map = new HashMap<>();
        map.put("roleId", id);
        return ResultEntity.success(map);
    }

    /**
     * 修改角色
     *
     * @param roleParam
     * @return
     */
    @PreAuthorize("#oauth2.hasScope('profile')")
    @PostMapping("/updateRole")
    public ResultEntity updateRole(@RequestBody RoleParam roleParam) {
        mdRoleService.update(roleParam);
        TenantRole tenantRole = tenantRoleMapper.selectOne(Wrappers.<TenantRole>lambdaQuery().eq(TenantRole::getRoleId, roleParam.getId()));
        Boolean permissionChange = false;

        if (roleParam.getPermissionChange() == null) {
            //为了兼容没有接入新版SDK的应用
            permissionChange = true;
        } else {
            permissionChange = roleParam.getPermissionChange();
        }
        if (permissionChange) {
            PushMessageDto pushMessageDto = new PushMessageDto();
            pushMessageDto.setRoleId(String.valueOf(roleParam.getId()));
            pushMessageDto.setTenantId(String.valueOf(tenantRole.getTenantId()));
            pushMessageDto.setStatus("ADD");
            pushMessageDto.setAppKey(tenantRole.getAppId());
            sendMessageToWebSocketService.sendMessage(pushMessageDto);
        }

        return ResultEntity.success();
    }

    /**
     * 删除角色
     *
     * @param id
     * @return
     */
    @PostMapping("/deleteRole")
    public ResultEntity deleteRole(@RequestParam Long id) throws Exception {
        mdRoleService.delete(id);
        return ResultEntity.success();
    }

    @PostMapping("/deleteAppRole")
    public ResultEntity deleteAppRole(@RequestBody RoleParam roleParam) throws Exception {
        mdRoleService.deleteAppRole(roleParam);
        return ResultEntity.success();
    }

    /**
     * 分页查找租户下角色列表
     *
     * @return
     */
    @GetMapping("/getRoleList")
    public ResultEntity selectRoleList(@RequestParam String appId,
                                       @RequestParam Long tenantId,
                                       @RequestParam(defaultValue = "1") Long pageNum,
                                       @RequestParam(defaultValue = "10") Long pageSize,
                                       @RequestParam(required = false) String name) throws UnsupportedEncodingException {
        name = StringUtils.isEmpty(name) ? null : URLDecoder.decode(name, "UTF-8");
        return ResultEntity.success(tenantRoleService.selectRoleList(appId, tenantId, pageNum, pageSize, name));
    }

    /**
     * 查找租户下所有角色
     *
     * @param tenantId
     * @return
     */
    @GetMapping("/getAllRoles")
    public ResultEntity getRoleList(@RequestParam Long tenantId,
                                    @RequestParam String appId) {
        return ResultEntity.success(tenantRoleService.getRoleList(tenantId, appId));
    }

    /**
     * 获取详情
     *
     * @param id
     * @return
     */
    @GetMapping("/getDetail")
    public ResultEntity getRoleDetail(Long id) {
        return ResultEntity.success(mdRoleService.getRoleDetail(id));
    }

    @GetMapping("/getAllAuthNew")
    public ResultEntity getAuth(String appId) {
        return ResultEntity.success(authService.getAllAuth(appId));
    }

    @GetMapping("/getAllAuth")
    public ResultEntity getAuthOld(String appId) {
        return ResultEntity.success(authService.getAllAuthOld(appId));
    }

    /**
     * 改变角色状态
     *
     * @param id
     * @return
     */
    @GetMapping("/status")
    public ResultEntity status(@RequestParam Long id,
                               @RequestParam Boolean status) {
        mdRoleService.changeStatus(id, status);
        return ResultEntity.success();
    }

    @PostMapping("/changeStatus")
    public ResultEntity changeStatus(@RequestBody RoleParam roleParam) {
        mdRoleService.changeRoleStatus(roleParam);
        return ResultEntity.success();
    }

    @GetMapping("/authFromManager")
    public ResultEntity authFromManager(@RequestParam Long authId,
                                        @RequestParam String appId,
                                        @RequestParam String type,
                                        @RequestParam String isNotTips,
                                        @RequestParam(required = false) String userId) {
        mdRoleService.authChange(authId, appId, type, isNotTips, userId);
        return ResultEntity.success();
    }
}
