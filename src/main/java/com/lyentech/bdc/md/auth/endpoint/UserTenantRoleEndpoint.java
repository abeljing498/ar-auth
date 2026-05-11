package com.lyentech.bdc.md.auth.endpoint;


import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.model.param.RoleByAuthParam;
import com.lyentech.bdc.md.auth.model.param.RoleUserParam;
import com.lyentech.bdc.md.auth.model.param.UserTenantRoleParam;
import com.lyentech.bdc.md.auth.service.UserTenantRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yan
 * @since 2022-08-11
 */
@RestController
@RequestMapping("/userTenantRole")
public class UserTenantRoleEndpoint {
    @Autowired
    private UserTenantRoleService userTenantRoleService;

    /**
     * 用户下新增角色(新增用户租户角色关联关系)
     */
    @PostMapping("/add")
    public ResultEntity addUserRole(@RequestBody UserTenantRoleParam userTenantRoleParam){
        userTenantRoleService.addUserTenantRole(userTenantRoleParam);
        return ResultEntity.success();
    }
    /**
     * 查找角色下成员列表
     * @param pageNum
     * @param pageSize
     * @param roleId
     * @return
     */
    @GetMapping("/getUser")
    public ResultEntity selectUserByRoleId(@RequestParam(defaultValue = "1") Long pageNum,
                                           @RequestParam(defaultValue = "10") Long pageSize,
                                           @RequestParam Long roleId,
                                           @RequestParam String nickname) throws UnsupportedEncodingException {
        nickname = !nickname.isEmpty() ? URLDecoder.decode(nickname, "UTF-8") : nickname;
        return ResultEntity.success(userTenantRoleService.selectUserByRoleId(pageNum,pageSize,roleId,nickname));
    }

    /**
     * 角色下批量添加成员 (批量添加角色成员关系)
     * @param roleUserList
     * @return
     */
    @PostMapping("/addUser")
    public ResultEntity addUserList(@RequestBody List<RoleUserParam> roleUserList){
        userTenantRoleService.addUserList(roleUserList);
        return ResultEntity.success();
    }

    /**
     * 角色下添加人员（添加角色成员关系）
     * @param roleUserParam
     * @return
     */
    @PostMapping("/addUserRole")
    public ResultEntity addUserRole(@RequestBody RoleUserParam roleUserParam){
        userTenantRoleService.addUserRole(roleUserParam);
        return ResultEntity.success();
    }

    /**
     * 移除角色下用户
     * @param roleId
     * @return
     */
    @PostMapping("/deleteUser")
    public ResultEntity deleteUser(@RequestParam Long roleId,
                                   @RequestParam Long userId,
                                   @RequestParam Long tenantId){
        userTenantRoleService.deleteUserRole(roleId,userId, tenantId);
        return ResultEntity.success();
    }
    @PostMapping("/deleteRoleUser")
    public ResultEntity deleteRoleUser(@RequestBody RoleUserParam roleUserParam){

        userTenantRoleService.deleteRoleUser(roleUserParam);
        return ResultEntity.success();
    }

    /**
     * 删除用户下角色（）
     * @param userTenantRoleParam
     * @return
     */
    @PostMapping("/delete")
    public ResultEntity deleteUserRole(@RequestBody UserTenantRoleParam userTenantRoleParam){
        userTenantRoleService.deleteUserRole(userTenantRoleParam);
        return ResultEntity.success();
    }

    @PostMapping("/getUserNumByAuth")
    public ResultEntity getUserNumByAuth(@RequestBody RoleByAuthParam roleByAuthParam) {
        return ResultEntity.success(userTenantRoleService.getUserNumByAuth(roleByAuthParam));
    }
}
