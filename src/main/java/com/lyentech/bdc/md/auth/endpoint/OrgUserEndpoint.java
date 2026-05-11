package com.lyentech.bdc.md.auth.endpoint;

import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.common.constant.MdAuthReturnTypeConstant;
import com.lyentech.bdc.md.auth.model.param.*;
import com.lyentech.bdc.md.auth.model.vo.PushAppOrderMsgDto;
import com.lyentech.bdc.md.auth.service.OrgUserService;
import com.lyentech.bdc.md.auth.service.SendMessageToWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;


/**
 * @author Yaoyulong
 * @since 2022-05-30
 */
@RestController
@RequestMapping("/orgUser")
@Slf4j
public class OrgUserEndpoint {

    @Autowired
    OrgUserService orgUserService;
    @Autowired
    private SendMessageToWebSocketService sendMessageToWebSocketService;

    /**
     * 添加员工
     *
     * @param orgUserParam
     * @return
     */
//    @PreAuthorize("#oauth2.hasScope('profile')")
    @PostMapping("/add")
    public ResultEntity add(@RequestBody OrgUserParam orgUserParam) throws Exception {
        return ResultEntity.success(orgUserService.add(orgUserParam));
    }

    @PostMapping("/addHumanResourceOrg")
    public ResultEntity addHumanResourceOrg(@RequestBody OrgUserParam orgUserParam) throws Exception {
        return ResultEntity.success(orgUserService.addHumanResourceOrg(orgUserParam));
    }

    @PostMapping("/addUserByEmail")
    public ResultEntity addUserByEmail(@RequestBody OrgUserParam orgUserParam) throws Exception {
        orgUserService.addUserByEmail(orgUserParam);
        return ResultEntity.success();
    }

    @PostMapping("/addByEmail")
    public ResultEntity addByEmail(@RequestBody OrgUserParam orgUserParam) throws Exception {
        orgUserService.addSearchUserByEmail(orgUserParam);

        return ResultEntity.success();
    }

    /**
     * 分页获取租户下所有员工信息以及模糊查找
     *
     * @param tenantId 租户Id
     * @return
     */
    @GetMapping("/getList")
    public ResultEntity getUser(@RequestParam(defaultValue = "1") Long pageNum,
                                @RequestParam(defaultValue = "10") Long pageSize,
                                @RequestParam(defaultValue = "") String keyword,
                                @RequestParam(defaultValue = "") Long orgId,
                                @RequestParam(defaultValue = "") String account,
                                @RequestParam Long tenantId,
                                @RequestParam String appId,
                                @RequestParam(required = false) Integer userState) throws UnsupportedEncodingException {
        keyword = !keyword.isEmpty() ? URLDecoder.decode(keyword, "UTF-8") : keyword;
        return ResultEntity.success(orgUserService.getUserList(pageNum, pageSize, tenantId, orgId, account, keyword, appId, userState));
    }

    @GetMapping("/getUserAppRoleList")
    public ResultEntity getUserAppRoleList(@RequestParam(defaultValue = "1") Long pageNum,
                                           @RequestParam(defaultValue = "10") Long pageSize,
                                           @RequestParam(defaultValue = "") String keyword) throws UnsupportedEncodingException {
        keyword = !keyword.isEmpty() ? URLDecoder.decode(keyword, "UTF-8") : keyword;
        return ResultEntity.success(orgUserService.getUserAppRoleList(pageNum, pageSize, keyword));
    }

    /**
     * 修改人员
     *
     * @param userParam 人员信息
     * @return
     */
//    @PreAuthorize("#oauth2.hasScope('profile')")
    @PostMapping("/update")
    public ResultEntity updateUser(@RequestBody OrgUserParam userParam) throws Exception {
        orgUserService.update(userParam);
        try {
            Boolean permissionChange = false;
            if (userParam.getPermissionChange() == null) {
                permissionChange = true;
            } else {
                permissionChange = userParam.getPermissionChange();
            }
            if (permissionChange) {
                PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
                pushAppOrderMsgDto.setAppKey(userParam.getAppId());
                pushAppOrderMsgDto.setUserId(userParam.getId().toString());
                pushAppOrderMsgDto.setTenantId(userParam.getTenantId().toString());
                pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.CHANGE_AUTH);
                sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return ResultEntity.success();
    }

    /**
     * 删除人员
     *
     * @param userParamList
     * @return
     */
    @PostMapping("/delete")
    public ResultEntity delete(@RequestBody List<DeleteUserParam> userParamList) {
        orgUserService.delete(userParamList);
        return ResultEntity.success();
    }

    /**
     * 通过用户Id获取人员详情
     *
     * @param id 人员Id
     * @return 人员详情
     */
    @GetMapping("/getInfo")
    public ResultEntity getUserDetails(@RequestParam Long id,
                                       @RequestParam Long orgId,
                                       @RequestParam Long tenantId,
                                       @RequestParam String appId
            , @RequestParam(required = false) String action) {
        return ResultEntity.success(orgUserService.getUserDetail(id, orgId, tenantId, appId, action));
    }

    /**
     * 通过电话号码精确查找人员信息
     *
     * @param phone    or email
     * @param tenantId
     * @param appId
     * @return
     */
    @GetMapping("/getUserByPhone")
    public ResultEntity getUserInfoByPhone(@RequestParam String phone,
                                           @RequestParam Long tenantId,
                                           @RequestParam String appId) {
        return ResultEntity.success(orgUserService.getUserInfo(phone, tenantId, appId));
    }

    @GetMapping("/getUserByAccount")
    public ResultEntity getUserByAccount(@RequestParam String account,
                                         @RequestParam Long tenantId,
                                         @RequestParam String appId) {
        return ResultEntity.success(orgUserService.getUserInfoByAccount(account));
    }

    /**
     * 删除单个用户组织关系
     *
     * @param deleteUserParam
     * @return
     */
    @PostMapping("/deleteOrgUser")
    public ResultEntity deleteOrgUser(@RequestBody DeleteUserParam deleteUserParam) {
        orgUserService.deleteOrgUser(deleteUserParam);
        return ResultEntity.success();
    }

    /**
     * 获取租户下所有人员
     *
     * @param tenantId
     * @return
     */
    @GetMapping("/getUserByTenantId")
    public ResultEntity getAllUser(@RequestParam Long tenantId,
                                   @RequestParam(defaultValue = "") Long orgId,
                                   @RequestParam(defaultValue = "") String keyword) throws UnsupportedEncodingException {
        keyword = !keyword.isEmpty() ? URLDecoder.decode(keyword, "UTF-8") : keyword;
        return ResultEntity.success(orgUserService.getUserByTenantId(tenantId, orgId, keyword));
    }

    @GetMapping("/getUser")
    public ResultEntity getUser(@RequestParam String phone) {
        return ResultEntity.success(orgUserService.userByPhone(phone));
    }

    @GetMapping("/getUserById")
    public ResultEntity getUserById(@RequestParam Long id) {
        return ResultEntity.success(orgUserService.getUserById(id));
    }

    @GetMapping("/getUserOrgAndRole")
    public ResultEntity getUserOrgAndRole(@RequestParam Long userId, @RequestParam Long tenantId, @RequestParam String appId, @RequestParam Long orgId) {
        return ResultEntity.success(orgUserService.getUserOrgAndRole(userId, tenantId, appId, orgId));
    }

    @PostMapping("/listUserInfo")
    public ResultEntity listUserDetail(@RequestBody ListUserInfoParam listUserInfoParam) {
        return ResultEntity.success(orgUserService.listUserDetails(listUserInfoParam.getKeys(), listUserInfoParam.getType(), listUserInfoParam.getTenantId(), listUserInfoParam.getHeight()));
    }

    @PostMapping("/importTeamUser")
    public ResultEntity importTeamUser(@RequestBody ImportTeamUserParam param) {
        return ResultEntity.success(orgUserService.importTeamUser(param));
    }

    /**
     * saveUserData
     *
     * @param param
     * @return
     */
    @PostMapping("/saveUserData")
    public ResultEntity saveUserData(@RequestBody ImportTeamUserParam param) {
        return ResultEntity.success(orgUserService.saveUserData(param));
    }

    @PostMapping("/saveFaqcToken")
    public ResultEntity saveFaqcToken(@RequestBody Map<String, Object> param) {
        return ResultEntity.success(orgUserService.saveFaqcToken(param));
    }

    @PostMapping("/outUserAdd")
    @PreAuthorize("#oauth2.hasScope('profile')")
    public ResultEntity outUserAdd(@RequestBody OutUserAddParam param) throws Exception {
        return ResultEntity.success(orgUserService.outUserAdd(param));
    }

    @PostMapping("/customerUserAdd")
    public ResultEntity customerUserAdd(@RequestBody OutUserAddParam param) throws Exception {
        return ResultEntity.success(orgUserService.customerUserAdd(param));
    }

    @GetMapping("/getDeptLeaderByUserEmail")
    @PreAuthorize("#oauth2.hasScope('profile')")
    public ResultEntity getDeptLeaderByUserEmail(@RequestParam String email, @RequestParam Long tenantId) throws UnsupportedEncodingException {
        email = !email.isEmpty() ? URLDecoder.decode(email, "UTF-8") : email;
        return ResultEntity.success(orgUserService.getDeptLeaderByUserEmail(email, tenantId));
    }

    @PostMapping("/exportUserLog")
    @PreAuthorize("#oauth2.hasScope('profile')")
    public ResultEntity exportUserLog(@RequestBody ExportUserParam param) {
        return ResultEntity.success(orgUserService.exportUserLog(param));
    }
}
