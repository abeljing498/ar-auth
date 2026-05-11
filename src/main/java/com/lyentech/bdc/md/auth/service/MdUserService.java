package com.lyentech.bdc.md.auth.service;

import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.param.*;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author guolanren
 */
public interface MdUserService extends UserDetailsService {

    MdUser register(MdJoinParam joinParam);

    MdUser registerByEmail(MdJoinParam joinParam);
    MdUser registerByAccount(MdJoinParam joinParam);

    /**
     * 修改账户基本信息
     *
     * @param userParam：phone；nickname；avatar：根据 id，为 null 则不进行更改
     */
    void update(MdUserParam userParam);

    /**
     * 修改密码
     *
     * @param resetPassword 手机号；旧密码；新密码
     */
    void resetPassword(MdResetPassword resetPassword);

    /**
     * 验证码方式修改秘密
     * @param resetPassword 手机号；验证码；新密码
     */
    void codeResetPassword(MdResetPassword resetPassword);

    /**
     * 根据邮箱号查询用户信息
     * @param email
     * @return
     */
    MdUser getByEmail(String email);

    /**
     * 根据手机号查询用户信息
     * @param phone
     * @return
     */
    MdUser getByPhone(String phone);

    MdUser getById(Integer id);

    /**
     * 管理员重新设置用户密码
     * @param managerResetPasswordParam
     */
    String managerResetPassword(ManagerResetPasswordParam managerResetPasswordParam);

    void bindUser(MdUserBindParam userBindParam);

    /**
     * 管理员修改密码
     * @param resetPassword
     * @return
     */
    String resetPwByManager(MdResetPassword resetPassword);

    /**
     *
     * @param resetPassword
     * @return
     */
    String resetPwByPhone(MdResetPassword resetPassword);

    Integer updateById(MdUser mdUser);

    MdUser getEmployeeId(String empId);
}
