package com.lyentech.bdc.md.auth.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.exception.BusinessException;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.kr.starter.common.KrEmailTypeEnum;
import com.lyentech.bdc.kr.starter.constant.KrSmsType;
import com.lyentech.bdc.kr.starter.service.KrInfoPush;
import com.lyentech.bdc.md.auth.common.constant.MdAuthReturnTypeConstant;
import com.lyentech.bdc.md.auth.common.constant.MdResutConstant;
import com.lyentech.bdc.md.auth.common.constant.UserOperationType;
import com.lyentech.bdc.md.auth.common.exception.MdAppAuthorizationException;
import com.lyentech.bdc.md.auth.common.exception.MdVerificationCodeException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginErrorLockException;
import com.lyentech.bdc.md.auth.dao.*;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.model.entity.MdUserAppRole;
import com.lyentech.bdc.md.auth.model.entity.MdUserOperateLog;
import com.lyentech.bdc.md.auth.model.entity.MdUserOperationLog;
import com.lyentech.bdc.md.auth.model.param.*;
import com.lyentech.bdc.md.auth.model.vo.PushAppOrderMsgDto;
import com.lyentech.bdc.md.auth.service.MdUserOperationLogService;
import com.lyentech.bdc.md.auth.service.MdUserService;
import com.lyentech.bdc.md.auth.service.SendMessageToWebSocketService;
import com.lyentech.bdc.md.auth.util.PinYinMultiCharactersUtils;
import com.lyentech.bdc.md.auth.util.RandomAccountGenerator;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lyentech.bdc.md.auth.common.constant.MdPasswordConstant.USER_PW;
import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_PASSWORD_BY_MANAGER_CHANGE_PATH;
import static com.lyentech.bdc.md.auth.util.PinYinMultiCharactersUtils.getMultiCharactersPinYin;
import static com.lyentech.bdc.md.auth.util.PinYinUtils.chineseToPinYinS;
import static com.lyentech.bdc.md.auth.util.PinYinUtils.getChinesePinyinFromName;

/**
 * @author guolanren
 */
@Service
public class MdUserServiceImpl implements MdUserService {


    private static final Logger logger = LoggerFactory.getLogger(MdUserServiceImpl.class);

    public static final String PASSWORD_ERROR = "PASSWORD:ERROR:";

    @Resource
    private MdUserMapper userMapper;

    private RandomValueStringGenerator randomValueStringGenerator = new RandomValueStringGenerator(11);


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Resource
    private MdUserAppRoleMapper mdUserAppRoleMapper;

    @Resource
    MdResetPasswordLogMapper mdResetPasswordLogMapper;

    @Resource
    private MdUserOrgMapper mdUserOrgMapper;
    @Resource
    private MdAppMapper appMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    private KrInfoPush krInfoPush;
    @Autowired
    private SendMessageToWebSocketService sendMessageToWebSocketService;
    @Autowired
    MdUserOperationLogService mdUserOperationLogService;

    @Override
    public MdUser loadUserByUsername(String account) throws UsernameNotFoundException {
        MdUser mdUser = userMapper.searchByPhone(account);
        if (mdUser == null) {
            mdUser = userMapper.searchByAccount(account);
            if (mdUser == null) {
                throw new UsernameNotFoundException("用户不存在！");
            }
        }
        return mdUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdUser register(MdJoinParam joinParam) {
        String phone = joinParam.getPhone();
        // 查询手机号对应的用户（已包括禁用的用户）
        MdUser user = userMapper.searchByPhoneIncludeDeleted(phone);
        // 用户已存在
        if (user != null) {
            userMapper.register(user.getId(),
                    joinParam.getNickname(),
                    passwordEncoder.encode(joinParam.getPassword()));
            return user;
        } else {
            // 创建用户
            user = new MdUser();
            BeanUtils.copyProperties(joinParam, user);

//        user.setPhone(joinParam.getPhone());
//        user.setNickname("MD_" + randomValueStringGenerator.generate());

            userMapper.insert(user);
            return user;
        }

    }

    /**
     * 通过邮箱号新增用户
     *
     * @param joinParam
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdUser registerByEmail(MdJoinParam joinParam) {
        MdUser user = new MdUser();
        user.setNickname(joinParam.getNickname());
        String abbreviation = null;
        try {
            abbreviation = chineseToPinYinS(joinParam.getNickname());
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        StringBuilder pinyinBuilder = new StringBuilder();
        String userPinyin = null;
        if (PinYinMultiCharactersUtils.isMultiChineseWord(joinParam.getNickname())) {
            userPinyin = pinyinBuilder.append(getChinesePinyinFromName(joinParam.getNickname())).append(",")
                    .append(abbreviation).append(",")
                    .append(getMultiCharactersPinYin(joinParam.getNickname())).toString();
        } else {
            userPinyin = pinyinBuilder.append(getChinesePinyinFromName(joinParam.getNickname())).append(",")
                    .append(abbreviation).toString();
        }
        //系统是否设置默认密码
        String appInitialPassword = appMapper.getAppInitialPassword(joinParam.getAppId());
        if (!com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(appInitialPassword)) {
            user.setPassword(passwordEncoder.encode(appInitialPassword));
        }
        user.setPinyin(userPinyin);
        user.setEmail(joinParam.getEmail());
        user.setEmployeeId(joinParam.getEmployeeId());
        userMapper.insert(user);
        if (StringUtils.isNotEmpty(joinParam.getEmail())) {
            return userMapper.searchByEmail(joinParam.getEmail());
        } else {
            return userMapper.selectOne(Wrappers.<MdUser>lambdaQuery()
                    .eq(MdUser::getEmployeeId, joinParam.getEmployeeId()).last("limit 1"));
        }

    }

    @Override
    public MdUser registerByAccount(MdJoinParam joinParam) {
        MdUser user = new MdUser();
        user.setNickname(joinParam.getNickname());
        String abbreviation = null;
        try {
            abbreviation = chineseToPinYinS(joinParam.getNickname());
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        StringBuilder pinyinBuilder = new StringBuilder();
        String userPinyin = null;
        if (PinYinMultiCharactersUtils.isMultiChineseWord(joinParam.getNickname())) {
            userPinyin = pinyinBuilder.append(getChinesePinyinFromName(joinParam.getNickname())).append(",")
                    .append(abbreviation).append(",")
                    .append(getMultiCharactersPinYin(joinParam.getNickname())).toString();
        } else {
            userPinyin = pinyinBuilder.append(getChinesePinyinFromName(joinParam.getNickname())).append(",")
                    .append(abbreviation).toString();
        }
        //系统是否设置默认密码
        String appInitialPassword = appMapper.getAppInitialPassword(joinParam.getAppId());
        if (!com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(appInitialPassword)) {
            user.setPassword(passwordEncoder.encode(appInitialPassword));
        }
        user.setPinyin(userPinyin);
        user.setAccount(joinParam.getAccount());
        MdUser mdUser = userMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getAccount, joinParam.getAccount()));
        if (mdUser == null) {
            userMapper.insert(user);
            mdUser = userMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getAccount, joinParam.getAccount()));
        }

        return mdUser;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MdUserParam userParam) {
        Long id = userParam.getId();
        if (id == null) {
            return;
        }

        String phone = userParam.getPhone();
        String nickname = userParam.getNickname();
        String avatar = userParam.getAvatar();
        if (nickname == null && avatar == null && phone == null) {
            return;
        }

        LambdaUpdateWrapper<MdUser> wrapper = Wrappers.lambdaUpdate();
        if (nickname != null) {
            wrapper.set(MdUser::getNickname, nickname);
        }
        if (avatar != null) {
            wrapper.set(MdUser::getAvatar, avatar);
        }
        if (phone != null) {
            wrapper.set(MdUser::getPhone, phone);
        }
        wrapper.eq(MdUser::getId, id);

        try {
            userMapper.update(null, wrapper);
        } catch (Exception e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new BusinessException("手机号[" + phone + "]已存在，请返回核对");
            } else {
                throw e;
            }
        }

    }

    @Override
    public void resetPassword(MdResetPassword resetPassword) {
        if (StringUtils.isBlank(resetPassword.getOldPassword())) {
            throw new IllegalParamException("原密码不能为空！");
        }
        if (StringUtils.isBlank(resetPassword.getNewPassword())) {
            throw new IllegalParamException("新密码不能为空！");
        }
        if (resetPassword.getNewPassword().equals(resetPassword.getOldPassword())) {
            throw new IllegalParamException("原密码和新密码不能一致,请修改！");
        }
        AtomicInteger errorNum = new AtomicInteger(0);
        MdUser mdUser = userMapper.searchByAccount(resetPassword.getAccount());
        if (ObjectUtils.isEmpty(mdUser)) {
            mdUser = userMapper.searchByPhone(resetPassword.getAccount());
        }
        StringBuilder stringBuilder = new StringBuilder();
        String errorKey = stringBuilder.append(PASSWORD_ERROR).append(mdUser.getId()).toString();
        String errorValue = stringRedisTemplate.boundValueOps(errorKey).get();
        if (errorValue != null) {
            errorNum = new AtomicInteger(Integer.parseInt(errorValue));
        }
        if (errorNum.longValue() >= 5) {
            //如果用户错误登录次数超过十次
            Long expire = stringRedisTemplate.getExpire(errorKey);
            //抛出账号锁定异常类
            throw new MdLoginErrorLockException("旧密码错误次数已到达上限，被锁定" + expire / 60 + "分钟" + expire % 60 + "秒");
        }
        boolean passwordMatches = passwordEncoder.matches(resetPassword.getOldPassword(), mdUser.getPassword());
        if (passwordMatches) {
            mdUser.setPassword(passwordEncoder.encode(resetPassword.getNewPassword()));
            int update = userMapper.updateById(mdUser);
            stringRedisTemplate.boundValueOps(USER_PW + ":" + mdUser.getId()).set(String.valueOf(mdUser.getId()), 90, TimeUnit.DAYS);
            stringRedisTemplate.delete(errorKey);
            //修改成功之后删除管理员初次修改密码记录
            Boolean hasKey = stringRedisTemplate.hasKey(USER_PASSWORD_BY_MANAGER_CHANGE_PATH + mdUser.getId().toString());
            if (hasKey) {
                stringRedisTemplate.delete(USER_PASSWORD_BY_MANAGER_CHANGE_PATH + mdUser.getId().toString());
            }
        } else {
            stringRedisTemplate.boundValueOps(errorKey).set(String.valueOf(errorNum.incrementAndGet()), 1800, TimeUnit.SECONDS);
            throw new MdAppAuthorizationException("旧密码错误，请校验后再尝试");
        }
    }


    @Override
    public void codeResetPassword(MdResetPassword resetPassword) {
        MdUser mdUser = userMapper.searchByPhone(resetPassword.getPhone());
        MdUserOperationLog mdUserOperationLog = new MdUserOperationLog();
        mdUserOperationLog.setOperateUserId(resetPassword.getOperateUserId());
        mdUserOperationLog.setOperateUserName(resetPassword.getOperateUserName());
        mdUserOperationLog.setOperationType(UserOperationType.CODE_PW.getCode());
        mdUserOperationLog.setAppId(resetPassword.getAppId());
        mdUserOperationLog.setTenantId(resetPassword.getTenantId());
        mdUserOperationLog.setUserId(mdUser.getId());
        mdUserOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdUserOperationLog.setCreateTime(new Date());
        mdUserOperationLog.setUserIp(resetPassword.getUserIp());
        mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
        if (StringUtils.isBlank(resetPassword.getPhone()) && StringUtils.isBlank(resetPassword.getNewPassword())) {
            mdUserOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
            throw new IllegalParamException("手机号和密码不能为空");
        }

        boolean passwordMatches = passwordEncoder.matches(resetPassword.getNewPassword(), mdUser.getPassword());
        if (passwordMatches) {
            mdUserOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
            throw new IllegalParamException("原密码和新密码不能一致,请修改！");
        }
        mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
        mdUser.setPassword(passwordEncoder.encode(resetPassword.getNewPassword()));
        userMapper.updateById(mdUser);
        stringRedisTemplate.boundValueOps(USER_PW + ":" + mdUser.getId()).set(String.valueOf(mdUser.getId()), 90, TimeUnit.DAYS);

    }

    @Override
    public MdUser getByEmail(String email) {
        return userMapper.searchByEmail(email);
    }

    @Override
    public MdUser getByPhone(String phone) {
        MdUser mdUser = userMapper.searchByPhone(phone);
        if (mdUser == null) {
            throw new UsernameNotFoundException("用户不存在！");
        }
        return mdUser;
    }

    @Override
    public MdUser getById(Integer id) {
        MdUser mdUser = userMapper.selectById(id);
        return mdUser;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String managerResetPassword(ManagerResetPasswordParam managerResetPasswordParam) {
        //验证管理员身份
        MdUserOperateLog mdUserOperateLog = new MdUserOperateLog();
        Integer count = mdUserAppRoleMapper.selectCount(Wrappers.<MdUserAppRole>lambdaQuery()
                .eq(MdUserAppRole::getUserId, managerResetPasswordParam.getManageId())
                .eq(MdUserAppRole::getAppId, managerResetPasswordParam.getAppKey()));
        Integer count1 = userMapper.selectCount(Wrappers.<MdUser>lambdaQuery()
                .eq(MdUser::getAccount, "Sbztglpt")
                .eq(MdUser::getId, managerResetPasswordParam.getRoleId()));
        if (count > 0) {
            mdUserOperateLog.setOperateType("updateByManager");
        } else if ((count <= 0) && (count1 > 0)) {
            mdUserOperateLog.setOperateType("updateByProject");
        } else if ((count <= 0) && (count1 <= 0)) {
            throw new IllegalParamException("无权限重置用户密码");
        }
        //验证用户身份
        Long orgCount = mdUserOrgMapper.existUserInAppKey(managerResetPasswordParam.getUserId(), managerResetPasswordParam.getAppKey());
        if (orgCount <= 0) {
            throw new IllegalParamException("当前项目中不存在该用户，不可修改密码");
        }

        //随机生成8位字符密码
        String pw = RandomAccountGenerator.getRandomPassword(8);
        String password = passwordEncoder.encode(pw);
        MdUser mdUser = userMapper.selectById(managerResetPasswordParam.getUserId());
        if (ObjectUtils.isEmpty(mdUser)) {
            throw new IllegalParamException("用户不存在");
        }
        //修改密码
        mdUser.setPassword(password);
        mdUser.setFirstLoginPW(false);
        //查看用户的密码是否到期
        String userId = stringRedisTemplate.boundValueOps(USER_PW + ":" + mdUser.getId()).get();
        if (!StringUtils.isEmpty(userId)) {
            stringRedisTemplate.delete(USER_PW + ":" + userId);
        }
        userMapper.updateById(mdUser);
        //添加修改日志
        mdUserOperateLog.setOperateContent("密码");
        mdUserOperateLog.setOperateInfo(JSON.toJSONString(mdUser));
        mdUserOperateLog.setUserId(mdUser.getId());
        mdUserOperateLog.setOperateUser(managerResetPasswordParam.getManageId());
        mdResetPasswordLogMapper.insert(mdUserOperateLog);
        return pw;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void bindUser(MdUserBindParam userBindParam) {
        if (ObjectUtils.isEmpty(userBindParam.getUserId())) {
            throw new IllegalParamException("成员id不能为空");
        }
        MdUser mdUser = userMapper.selectOne(Wrappers.<MdUser>lambdaQuery()
                .eq(MdUser::getPhone, userBindParam.getPhone())
                .ne(MdUser::getId, userBindParam.getUserId()));
        MdUser mdUser1 = new MdUser();
        if (ObjectUtils.isNotEmpty(mdUser)) {
            throw new IllegalParamException("该手机号已被绑定！请联系管理员处理");
        } else {
            mdUser1.setPhone(userBindParam.getPhone());
            mdUser1.setId(userBindParam.getUserId());
            userMapper.updateById(mdUser1);
        }
        MdUserOperateLog mdUserOperateLog = new MdUserOperateLog();
        mdUserOperateLog.setOperateContent("手机号码");
        mdUserOperateLog.setOperateInfo(JSON.toJSONString(mdUser1));
        mdUserOperateLog.setUserId(userBindParam.getUserId());
        mdUserOperateLog.setOperateUser(userBindParam.getUserId());
        mdUserOperateLog.setOperateType("bindPhone");
        mdResetPasswordLogMapper.insert(mdUserOperateLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetPwByManager(MdResetPassword resetPassword) {
        if (ObjectUtils.isEmpty(resetPassword.getUserId())) {
            throw new IllegalParamException("成员id不能为空");
        }
        //验证用户身份
        Long orgCount = mdUserOrgMapper.existUserInAppKey(resetPassword.getUserId().longValue(), resetPassword.getAppKey());
        if (orgCount <= 0) {
            throw new IllegalParamException("当前项目中不存在该用户，不可修改密码");
        }
        MdUser mdUser = userMapper.selectById(resetPassword.getUserId());
        if (ObjectUtils.isEmpty(mdUser)) {
            throw new IllegalParamException("用户不存在");
        }
        MdUserOperationLog mdUserOperationLog = new MdUserOperationLog();
        mdUserOperationLog.setOperateUserId(resetPassword.getOperateUserId());
        mdUserOperationLog.setOperateUserName(resetPassword.getOperateUserName());
        mdUserOperationLog.setOperationType(UserOperationType.MANGER_PW.getCode());
        mdUserOperationLog.setAppId(resetPassword.getAppId());
        mdUserOperationLog.setTenantId(resetPassword.getTenantId());
        mdUserOperationLog.setUserId(mdUser.getId());
        mdUserOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdUserOperationLog.setCreateTime(new Date());
        mdUserOperationLog.setUserIp(resetPassword.getUserIp());
        mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
        String appInitialPassword = appMapper.getAppInitialPassword(resetPassword.getAppKey());
        String password = "";
        if (!ObjectUtils.isEmpty(appInitialPassword)) {
            password = passwordEncoder.encode(appInitialPassword);
            mdUser.setPassword(password);
            //修改密码
            mdUser.setFirstLoginPW(false);
            //查看用户的密码是否到期
            String userId = stringRedisTemplate.boundValueOps(USER_PW + ":" + mdUser.getId()).get();
            if (!StringUtils.isEmpty(userId)) {
                stringRedisTemplate.delete(USER_PW + ":" + userId);
            }
            userMapper.updateById(mdUser);
            try {
                if (!StringUtils.isBlank(mdUser.getPhone())) {
                    logger.info("验证码通，知app:{},管理员修改密码默认密码为：{}", resetPassword.getAppKey(), appInitialPassword);
                    String param = blurPhone(mdUser.getPhone()) + "@@" + appInitialPassword;
                    krInfoPush.sendSmsCode(mdUser.getPhone(), param, KrSmsType.REST_PASSWORD_BY_MANAGER_NOTICE);
                }
                if (!StringUtils.isBlank(mdUser.getEmail())) {
                    logger.info("短信通知，app:{},管理员修改密码默认密码为：{}", resetPassword.getAppKey(), appInitialPassword);
                    String html = "<!DOCTYPE html>";
                    html += "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">";
                    html += "</head><body>";
                    html += "<div style=\"margin:30px auto;\">";
                    html += "<p><span style=\"font-weight:bold\">【系统通知】</span>您的初始密码已设置，请使用初始密码首次登录后重新修改用户密码进入系统，初始密码为 <span style=\"color:red\">" + appInitialPassword + "</span>,请务必妥善保管账户信息！</p>";
                    html += "</div>";
                    html += "</body></html>";
                    krInfoPush.sendGreeEmail(mdUser.getEmail().toLowerCase() + "@gree.com.cn", "", "用户账号变动提醒", html, KrEmailTypeEnum.HTML);
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
            MdUser mdManager = userMapper.selectOne(Wrappers.<MdUser>lambdaQuery()
                    .eq(MdUser::getId, resetPassword.getManagerId()));
            //验证管理员身份
            MdUserOperateLog mdUserOperateLog = new MdUserOperateLog();
            mdUserOperateLog.setOperateType("updateByManager");
            mdUserOperateLog.setOperateContent("管理员修改密码");
            mdUserOperateLog.setOperateInfo(JSON.toJSONString(mdManager));
            mdUserOperateLog.setUserId(resetPassword.getUserId().longValue());
            mdUserOperateLog.setOperateUser(resetPassword.getManagerId().longValue());
            mdResetPasswordLogMapper.insert(mdUserOperateLog);
            StringBuilder changeBuilder = new StringBuilder();
            String changePassword = changeBuilder.append(USER_PASSWORD_BY_MANAGER_CHANGE_PATH)
                    .append(mdUser.getId()).toString();
            stringRedisTemplate.boundValueOps(changePassword).set(appInitialPassword);
            //发送权限变更消息至下游客户端
            PushAppOrderMsgDto pushAppOrderMsgDto = new PushAppOrderMsgDto();
            pushAppOrderMsgDto.setAppKey(resetPassword.getAppKey());
            pushAppOrderMsgDto.setUserId(resetPassword.getUserId().toString());
            pushAppOrderMsgDto.setMessage(MdAuthReturnTypeConstant.MANAGER_CHANGE_PASSWORD);
            sendMessageToWebSocketService.pushAppOrderMessage(pushAppOrderMsgDto);
        }
        return appInitialPassword;
    }

    @Override
    public String resetPwByPhone(MdResetPassword resetPassword) {
        MdUserOperationLog mdUserOperationLog = new MdUserOperationLog();
        mdUserOperationLog.setOperateUserId(resetPassword.getOperateUserId());
        mdUserOperationLog.setOperateUserName(resetPassword.getOperateUserName());
        mdUserOperationLog.setOperationType(UserOperationType.CODE_PW.getCode());
        mdUserOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdUserOperationLog.setCreateTime(new Date());
        mdUserOperationLog.setUserIp(resetPassword.getUserIp());
        mdUserOperationLog.setNotes(resetPassword.getPhone());
        mdUserOperationLog.setAppId(resetPassword.getAppId());
        if (StringUtils.isBlank(resetPassword.getCode())) {
            mdUserOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
            throw new IllegalParamException("验证码不能为空");
        }
        if (StringUtils.isBlank(resetPassword.getPhone())) {
            mdUserOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
            throw new IllegalParamException("手机号不能为空");
        }
        Boolean isRight = krInfoPush.verifySmsCode(resetPassword.getPhone(), resetPassword.getCode(), KrSmsType.LOGIN);

        if (!isRight) {
            mdUserOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
            throw new MdVerificationCodeException("验证码输入不正确或已过期");
        }

        MdUser mdUser = userMapper.searchByPhone(resetPassword.getPhone());
        boolean passwordMatches = passwordEncoder.matches(resetPassword.getNewPassword(), mdUser.getPassword());
        if (passwordMatches) {
            throw new MdVerificationCodeException("原密码和新密码不能一致,请修改！");
        }
        StringBuilder stringBuilder = new StringBuilder();
        String errorKey = stringBuilder.append(PASSWORD_ERROR).append(mdUser.getId()).toString();
        mdUser.setPassword(passwordEncoder.encode(resetPassword.getNewPassword()));
        int update = userMapper.updateById(mdUser);
        stringRedisTemplate.boundValueOps(USER_PW + ":" + mdUser.getId()).set(String.valueOf(mdUser.getId()), 90, TimeUnit.DAYS);
        stringRedisTemplate.delete(errorKey);
        //修改成功之后删除管理员初次修改密码记录
        Boolean hasKey = stringRedisTemplate.hasKey(USER_PASSWORD_BY_MANAGER_CHANGE_PATH + mdUser.getId().toString());
        if (hasKey) {
            stringRedisTemplate.delete(USER_PASSWORD_BY_MANAGER_CHANGE_PATH + mdUser.getId().toString());
        }
        mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
        return null;
    }

    @Override
    public Integer updateById(MdUser mdUser) {
        Integer flag = userMapper.updateById(mdUser);
        return flag;
    }

    @Override
    public MdUser getEmployeeId(String empId) {
        return userMapper.selectOne(Wrappers.<MdUser>lambdaQuery()
                .eq(MdUser::getEmployeeId, empId).last("limit 1"));
    }

    public static final String blurPhone(String phone) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(phone) || (phone.length() != 11)) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}
