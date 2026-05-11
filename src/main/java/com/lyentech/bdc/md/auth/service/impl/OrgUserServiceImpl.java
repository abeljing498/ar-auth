package com.lyentech.bdc.md.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Sets;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.common.constant.MdResutConstant;
import com.lyentech.bdc.md.auth.common.constant.MdUserStatusConstant;
import com.lyentech.bdc.md.auth.common.constant.UserOperationType;
import com.lyentech.bdc.md.auth.dao.*;
import com.lyentech.bdc.md.auth.model.entity.*;
import com.lyentech.bdc.md.auth.model.param.*;
import com.lyentech.bdc.md.auth.model.vo.*;
import com.lyentech.bdc.md.auth.service.*;
import com.lyentech.bdc.md.auth.util.CharacterUntil;
import com.lyentech.bdc.md.auth.util.PhoneNumberUtils;
import com.lyentech.bdc.md.auth.util.PinYinMultiCharactersUtils;
import com.lyentech.bdc.md.auth.util.RandomAccountGenerator;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.lyentech.bdc.md.auth.common.constant.MdAuthTokenConstant.FAQC_TOKEN_PATH;
import static com.lyentech.bdc.md.auth.util.PinYinMultiCharactersUtils.getMultiCharactersPinYin;
import static com.lyentech.bdc.md.auth.util.PinYinUtils.chineseToPinYinS;
import static com.lyentech.bdc.md.auth.util.PinYinUtils.getChinesePinyinFromName;


/**
 * 组织用户
 *
 * @author 260582
 */
@Service
@Slf4j
public class OrgUserServiceImpl implements OrgUserService {


    private static final String ID = "id";
    private static final String EMAIL = "email";
    private static final String PHONE = "phone";

    @Resource
    private MdUserOrgMapper mdUserOrgMapper;
    @Resource
    private MdUserMapper mdUserMapper;
    @Autowired
    private MdUserOrgService mdUserOrgService;
    @Resource
    private UserTenantRoleMapper userTenantRoleMapper;
    @Resource
    private UserTenantRoleService userTenantRoleService;
    @Resource
    private MdUserAppRoleMapper userAppRoleMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Resource
    private OrgMapper orgMapper;
    @Resource
    private OrgService orgService;
    @Resource
    private MdAppMapper mdAppMapper;
    @Resource
    MdBlackUserMapper mdBlackUserMapper;
    @Resource
    UserChannelRelatedMapper userChannelRelatedMapper;
    @Autowired
    MdUserChannelService mdUserChannelService;
    @Autowired
    MdCompanyUserMapper mdCompanyUserMapper;

    @Autowired
    MdUserOperationLogService mdUserOperationLogService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 组织下添加员工
     * 通过账号或手机号添加用户
     *
     * @param orgUserParam
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public PasswordVO add(OrgUserParam orgUserParam) throws Exception {
        log.info("新增用户参数--->{}", JSON.toJSON(orgUserParam).toString());
        if (ObjectUtils.isEmpty(orgUserParam.getNickname())) {
            throw new IllegalParamException("请输入姓名！");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getOrgIds())) {
            throw new IllegalParamException("组织不可为空");
        }
        if (StringUtils.isNotBlank(orgUserParam.getEmail())) {
            if (orgUserParam.getEmail().length() > 18) {
                throw new IllegalArgumentException("邮箱长度不能超过18位");
            }
            log.info("用户新增邮箱为：{}", orgUserParam.getEmail());
            orgUserParam.setEmail(CharacterUntil.emailToChange(orgUserParam.getEmail()));
        }
        if (ObjectUtils.isNotEmpty(orgUserParam.getPhone())) {
            if (!isValidPhone(orgUserParam.getPhone())) { // 调用手机号格式验证方法
                log.error("新增用户手机号格式不正确--->{}", JSON.toJSON(orgUserParam).toString());
                throw new IllegalArgumentException("手机号格式不正确"); // 或者返回错误对象
            }
        }
        PasswordVO passwordVO = new PasswordVO();
        MdUser mdUser = null;
        if (StringUtils.isNotBlank(orgUserParam.getPhone()) && StringUtils.isNotBlank(orgUserParam.getEmail())) {
            mdUser = mdUserMapper.searchByPhone(orgUserParam.getPhone());
            if (mdUser != null && StringUtils.isNotBlank(mdUser.getEmail())) {
                if (!mdUser.getEmail().equals(orgUserParam.getEmail())) {
                    throw new IllegalParamException("此手机号已绑定其他邮箱号！");
                }
            }
            //判断用户是否加入黑名单如果加入不让导入
            mdUser = mdUserMapper.searchByEmail(orgUserParam.getEmail());
            if (mdUser != null && StringUtils.isNotBlank(mdUser.getPhone())) {
                if (!mdUser.getPhone().equals(orgUserParam.getPhone())) {
                    throw new IllegalParamException("此邮箱号已被其他手机号绑定！");
                }
            }
        }
        //判断添加的成员是通过手机号添加还是账号添加
        if (ObjectUtils.isNotEmpty(orgUserParam.getPhone())) {
            log.info("新增用户手机不为空--->{}", JSON.toJSON(orgUserParam).toString());
            //如果添加用户电话不为空，使用电话进行创建
            passwordVO = addUserByPhone(orgUserParam);
        } else if (ObjectUtils.isNotEmpty(orgUserParam.getEmail())) {
            log.info("新增用户邮箱不为空--->{}", JSON.toJSON(orgUserParam).toString());
            //如果手机号为空，邮箱号不为空
            passwordVO = addByEmail(orgUserParam);
        } else if (ObjectUtils.isNotEmpty(orgUserParam.getAccount())) {
            //通过账号进行创建
            //判断账号名是否已经被注册
            passwordVO = addUserByAccount(orgUserParam);
        } else {
            if (!StringUtils.isBlank(orgUserParam.getEmployeeId())) {
                passwordVO = addEmployeeId(orgUserParam);
            }
        }
        MdUserOperationLog mdUserOperationLog = new MdUserOperationLog();
        mdUserOperationLog.setOperateUserId(orgUserParam.getOperateUserId());
        mdUserOperationLog.setOperateUserName(orgUserParam.getOperateUserName());
        mdUserOperationLog.setOperationType(UserOperationType.ADD.getCode());
        mdUserOperationLog.setAppId(orgUserParam.getAppId());
        mdUserOperationLog.setTenantId(orgUserParam.getTenantId());
        mdUserOperationLog.setUserId(passwordVO.getUserId());
        mdUserOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdUserOperationLog.setCreateTime(new Date());
        mdUserOperationLog.setNotes(JSON.toJSONString(orgUserParam));
        mdUserOperationLog.setUserIp(orgUserParam.getUserIp());
        mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
        log.info("新增用户返回--->{}", JSON.toJSON(passwordVO).toString());
        return passwordVO;
    }

    private PasswordVO addEmployeeId(OrgUserParam orgUserParam) throws Exception {
        if (StringUtils.isBlank(orgUserParam.getEmployeeId())) {
            throw new IllegalParamException("员工工号不能为空");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getNickname())) {
            throw new IllegalParamException("请输入姓名！");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getOrgIds())) {
            throw new IllegalParamException("组织不可为空");
        }
        PasswordVO passwordVO = new PasswordVO();
        MdUser userByEmployee = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getEmployeeId, orgUserParam.getEmployeeId()).last("limit 1"));
        if (ObjectUtils.isEmpty(userByEmployee)) {
            throw new IllegalParamException("该用户不存在，请联系管理员！");
        }

        MdBlackUser blackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, orgUserParam.getAppId())
                .eq(MdBlackUser::getUserId, userByEmployee.getId()).last(" limit 1"));
        if (ObjectUtils.isNotEmpty(blackUser)) {
            throw new IllegalParamException("此用户已再黑名单中，系统不允许登录，请联系管理员");
        }
        orgUserParam.setId(userByEmployee.getId());
        userByEmployee.setNickname(orgUserParam.getNickname());
        userByEmployee.setAccount(orgUserParam.getAccount());
        mdUserMapper.updateById(userByEmployee);
        mdUserOrgService.addOrgUser(orgUserParam);
        passwordVO.setUserId(userByEmployee.getId());
        return passwordVO;

    }

    /**
     * 通过邮箱号添加用户
     *
     * @param orgUserParam
     */
    public PasswordVO addByEmail(OrgUserParam orgUserParam) throws Exception {
        if (StringUtils.isBlank(orgUserParam.getEmail())) {
            throw new IllegalParamException("邮箱号不能为空");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getNickname())) {
            throw new IllegalParamException("请输入姓名！");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getOrgIds())) {
            throw new IllegalParamException("组织不可为空");
        }
        PasswordVO passwordVO = new PasswordVO();
        MdUser userByEmail = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getEmail, orgUserParam.getEmail()).last("limit 1"));
        //账号不存在
        if (ObjectUtils.isEmpty(userByEmail)) {
            //手机号存在
            if (!ObjectUtils.isEmpty(orgUserParam.getPhone())) {
                MdUser user = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getPhone, orgUserParam.getPhone()));
                if (ObjectUtils.isNotEmpty(user) && (user.getEmail().equals(orgUserParam.getEmail()) || ObjectUtils.isEmpty(user.getEmail()))) {
                    //手机号存在且手机号绑定的邮箱号和新添加邮箱号一致或者原账号邮箱号为空
                    user.setEmail(orgUserParam.getEmail());
                    user.setNickname(orgUserParam.getNickname());
                    orgUserParam.setId(user.getId());
                    mdUserMapper.updateById(user);
                    mdUserOrgService.addOrgUser(orgUserParam);
                    passwordVO.setUserId(user.getId());
                    return passwordVO;
                } else {
                    throw new IllegalParamException("该邮箱号已被注册，但与原绑定手机号不符，请修改或联系管理员！");
                }
            } else {
                //手机号未被注册，邮箱号未被注册
                checkoutUser(orgUserParam);
                passwordVO = addUser(orgUserParam);
                return passwordVO;
            }
        } else {
            MdBlackUser blackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, orgUserParam.getAppId())
                    .eq(MdBlackUser::getUserId, userByEmail.getId()).last(" limit 1"));
            if (ObjectUtils.isNotEmpty(blackUser)) {
                throw new IllegalParamException("此用户已存在于本系统黑名单，请勿重复添加");
            }
            orgUserParam.setId(userByEmail.getId());
            checkoutUser(orgUserParam);
            userByEmail.setNickname(orgUserParam.getNickname());
            userByEmail.setAccount(orgUserParam.getAccount());
            mdUserMapper.updateById(userByEmail);
            mdUserOrgService.addOrgUser(orgUserParam);
            passwordVO.setUserId(userByEmail.getId());
            return passwordVO;
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUserByEmail(OrgUserParam orgUserParam) throws Exception {
        //// TODO: 2023/3/27
        //此处是专门为了检索端设计的绑定流程，后续优化了通过邮箱号添加人员可将此处删去
        if (StringUtils.isBlank(orgUserParam.getPhone()) && StringUtils.isBlank(orgUserParam.getAccount())) {
            throw new IllegalParamException("电话号码与账户名不能同时为空");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getNickname())) {
            throw new IllegalParamException("请输入姓名！");
        }
        if (StringUtils.isBlank(orgUserParam.getEmail())) {
            throw new IllegalParamException("邮箱号不可为空");
        }
        MdUser userByEmail = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getEmail, orgUserParam.getEmail()));
        MdUser userByPhone = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getPhone, orgUserParam.getPhone()));
        RoleUserParam roleUserParam = new RoleUserParam();
        if (ObjectUtils.isEmpty(userByEmail)) {
            //邮箱号未被注册
            if (ObjectUtils.isEmpty(userByPhone)) {
                //手机号未被注册
                PasswordVO passwordVO = addUserByPhone(orgUserParam);
                roleUserParam.setUserId(passwordVO.getUserId());
                orgUserParam.setId(passwordVO.getUserId());
            } else {
                //手机号已经注册,覆盖
                if (StringUtils.isBlank(userByPhone.getEmail())) {
                    userByPhone.setEmail(orgUserParam.getEmail());
                    mdUserMapper.updateById(userByPhone);
                } else {
                    throw new IllegalParamException("该手机号已绑定其他邮箱，请检查！");
                }
                roleUserParam.setUserId(userByPhone.getId());
                orgUserParam.setId(userByPhone.getId());
                mdUserOrgService.addOrgUser(orgUserParam);
            }
        } else {
            //邮箱号已被注册
            if (ObjectUtils.isEmpty(userByPhone)) {
                //手机号未被注册
                if (StringUtils.isBlank(userByEmail.getPhone())) {
                    //userByEmail手机号为空，直接插入
                    userByEmail.setPhone(orgUserParam.getPhone());
                    mdUserMapper.updateById(userByEmail);
                } else {
                    //手机号不为null
                    throw new IllegalParamException("该手机号已绑定其他邮箱，请检查！");
                }
                roleUserParam.setUserId(userByEmail.getId());
                orgUserParam.setId(userByEmail.getId());
                mdUserOrgService.addOrgUser(orgUserParam);
            } else {
                //手机号已经被注册
                if (userByEmail.getId().equals(userByPhone.getId())) {
                    //插入角色
                    roleUserParam.setUserId(userByEmail.getId());
                    orgUserParam.setId(userByEmail.getId());
                    mdUserOrgService.addOrgUser(orgUserParam);
                } else {
                    throw new IllegalParamException("该手机号已绑定其他邮箱，请检查！");
                }
            }
        }
        roleUserParam.setTenantId(orgUserParam.getTenantId());
        roleUserParam.setRoleId(orgUserParam.getRoleList().iterator().next());
        roleUserParam.setAppId(orgUserParam.getAppId());
        //插入角色，进行原有角色判断
        Integer count = userTenantRoleMapper.selectCount(Wrappers.<UserTenantRole>lambdaQuery()
                .eq(UserTenantRole::getUserId, roleUserParam.getUserId())
                .eq(UserTenantRole::getTenantId, roleUserParam.getTenantId())
                .eq(UserTenantRole::getRoleId, roleUserParam.getRoleId())
                .eq(UserTenantRole::getAppId, roleUserParam.getAppId()));
        if (count == 0) {
            userTenantRoleService.addUserRole(roleUserParam);
        }
    }

    @Override
    public void addSearchUserByEmail(OrgUserParam orgUserParam) throws Exception {
        if (StringUtils.isBlank(orgUserParam.getEmail())) {
            throw new IllegalParamException("邮箱号不可为空");
        }
        MdUser userByEmail = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getEmail, orgUserParam.getEmail()));
        RoleUserParam roleUserParam = new RoleUserParam();
        roleUserParam.setUserId(userByEmail.getId());
        orgUserParam.setId(userByEmail.getId());
        mdUserOrgService.addOrgUser(orgUserParam);
        roleUserParam.setTenantId(orgUserParam.getTenantId());
        roleUserParam.setRoleId(orgUserParam.getRoleList().iterator().next());
        roleUserParam.setAppId(orgUserParam.getAppId());
        //插入角色，进行原有角色判断
        Integer count = userTenantRoleMapper.selectCount(Wrappers.<UserTenantRole>lambdaQuery()
                .eq(UserTenantRole::getUserId, roleUserParam.getUserId())
                .eq(UserTenantRole::getTenantId, roleUserParam.getTenantId())
                .eq(UserTenantRole::getRoleId, roleUserParam.getRoleId())
                .eq(UserTenantRole::getAppId, roleUserParam.getAppId()));
        if (count == 0) {
            userTenantRoleService.addUserRole(roleUserParam);
        }
    }

    @Override
    public MdUser customerUserAdd(OutUserAddParam param) throws Exception {
        OrgUserParam orgUserParam = param.getOrgUserParam();
        if (StringUtils.isBlank(orgUserParam.getPhone())) {
            throw new IllegalParamException("用户手机号不能为空！");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getOrgId())) {
            throw new IllegalParamException("用户组织不能为空！");
        }
        if (CollectionUtils.isEmpty(orgUserParam.getRoleList())) {
            throw new IllegalParamException("用户角色不能为空！");
        }
        log.info("联通调用用户保存接口{}", JSON.toJSON(param).toString());
        MdUser mdUser = null;
        if (orgUserParam.getId() == null) {
            mdUser = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getPhone, orgUserParam.getPhone()));
        } else {
            mdUser = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getId, orgUserParam.getId()));
        }
        Set<Long> listOrgId = new HashSet<>();
        listOrgId.add(orgUserParam.getOrgId());
        orgUserParam.setOrgIds(listOrgId);
        if (mdUser != null) {
            mdUser.setNickname(orgUserParam.getNickname());
            mdUser.setAccount("lt_" + orgUserParam.getUserName());
            mdUser.setPhone(orgUserParam.getPhone());
            mdUserMapper.updateById(mdUser);
            List<Long> userRolesId = userTenantRoleService.getRoleIds(orgUserParam.getAppId(), orgUserParam.getTenantId(), mdUser.getId());
            UserTenantRoleParam roleUserTenantParam = new UserTenantRoleParam();
            if (!CollectionUtils.isEmpty(userRolesId)) {
                log.info("联通调用用户{}存在角色{}", JSON.toJSON(param).toString(), JSON.toJSON(userRolesId));
                List<Long> mergedRoleIds = Stream.concat(userRolesId.stream(), orgUserParam.getRoleList().stream())
                        .distinct()
                        .collect(Collectors.toList());
                roleUserTenantParam.setRoleList(mergedRoleIds);
            } else {
                roleUserTenantParam.setRoleList(orgUserParam.getRoleList().stream().collect(Collectors.toList()));
            }
            orgUserParam.setId(mdUser.getId());
            roleUserTenantParam.setUserId(mdUser.getId());
            roleUserTenantParam.setTenantId(orgUserParam.getTenantId());
            roleUserTenantParam.setAppId(orgUserParam.getAppId());

            userTenantRoleService.addUserTenantRole(roleUserTenantParam);
            mdUserOrgService.addOrgUser(orgUserParam);
            if (param.getOrgUserChannelRelated() != null) {
                param.getOrgUserChannelRelated().setUserId(mdUser.getId());
            }

        } else {
            orgUserParam.setAccount("lt_" + orgUserParam.getUserName());
            PasswordVO passwordVO = addUser(orgUserParam);
            mdUser = new MdUser();
            mdUser.setId(passwordVO.getUserId());
            orgUserParam.setId(mdUser.getId());
            UserTenantRoleParam roleUserTenantParam = new UserTenantRoleParam();
            roleUserTenantParam.setUserId(mdUser.getId());
            roleUserTenantParam.setTenantId(orgUserParam.getTenantId());
            roleUserTenantParam.setAppId(orgUserParam.getAppId());
            roleUserTenantParam.setRoleList(orgUserParam.getRoleList().stream().collect(Collectors.toList()));
            userTenantRoleService.addUserTenantRole(roleUserTenantParam);
            if (param.getOrgUserChannelRelated() != null) {
                param.getOrgUserChannelRelated().setUserId(mdUser.getId());
            }
        }
        mdUserChannelService.relatedUserChannel(param.getOrgUserChannelRelated());
        return mdUser;
    }

    @Override
    public Object saveFaqcToken(Map<String, Object> param) {
        String token = param.get("token").toString();
        String userId = param.get("userId").toString();
        Integer tokenTimeOut = (Integer) param.get("tokenTimeOut");
        stringRedisTemplate.boundValueOps(FAQC_TOKEN_PATH + ":" + token).set(userId, tokenTimeOut, TimeUnit.SECONDS);
        return null;
    }

    @Override
    public PageResult<UserAppRolesVO> getUserAppRoleList(Long pageNum, Long pageSize, String keyword) {
        //查找租客下所有人员
        IPage<UserAppRolesVO> page = mdUserMapper.getUserAppRoleList(new Page<>(pageNum, pageSize), keyword);
        if (page.getRecords() != null) {
            for (UserAppRolesVO userAppRolesVO : page.getRecords()) {
                List<String> nameList = new ArrayList<>();
                List<RoleVO> mdRoleList = userTenantRoleService.getRoleListByTenantAndUserId(userAppRolesVO.getTenantId(), userAppRolesVO.getUserId(), userAppRolesVO.getAppId());
                if (mdRoleList != null) {
                    nameList = mdRoleList.stream()
                            .map(RoleVO::getName)
                            .collect(Collectors.toList());


                }
                Integer integer = userAppRoleMapper.selectCount(Wrappers.<MdUserAppRole>lambdaQuery()
                        .eq(MdUserAppRole::getAppId, userAppRolesVO.getAppId())
                        .eq(MdUserAppRole::getUserId, userAppRolesVO.getUserId()));
                if (integer > 0) {
                    nameList.add("admin");
                }
                userAppRolesVO.setRoleName(nameList);
            }

        } else {
            return PageResult.build(pageNum, pageSize, 0L, 0L, null);
        }
        return PageResult.build(pageNum, pageSize, page.getPages(), page.getTotal(), page.getRecords());
    }

    PasswordVO addUserByAccount(OrgUserParam orgUserParam) throws Exception {
        PasswordVO passwordVO = new PasswordVO();
        MdUser mdUser = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getAccount, orgUserParam.getAccount()));
        if (mdUser != null) {
            checkoutUser(orgUserParam);
            mdUser.setNickname(orgUserParam.getNickname());
            mdUserMapper.updateById(mdUser);
            orgUserParam.setId(mdUser.getId());
            //添加加入的组织管理
            mdUserOrgService.addOrgUser(orgUserParam);
            String pw = "当前账号已存在于用户资源池中并已于当前系统进行绑定，请用户使用原密码登录";
            passwordVO.setUserId(mdUser.getId());
            passwordVO.setPassword(pw);
//            throw new IllegalParamException("账号名称已重复");
        } else {
            //判断邮箱号是否为空，是否被注册
            if (StringUtils.isNotBlank(orgUserParam.getEmail())) {
                Integer count1 = mdUserMapper.selectCount(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getEmail, orgUserParam.getEmail()));
                if (count1 > 0) {
                    throw new IllegalParamException("该邮箱号已被注册，请重新输入");
                }
            }
            passwordVO = addUser(orgUserParam);
        }
        return passwordVO;
    }

    PasswordVO addUserByPhone(OrgUserParam orgUserParam) throws Exception {
        //通过电话查找是否电话已被注册
        PasswordVO passwordVO = new PasswordVO();
        MdUser mdUser = mdUserMapper.searchByPhone(orgUserParam.getPhone());
        if (mdUser == null) {
            //电话未被注册，对邮箱等其他字段进行查重判断
            if (StringUtils.isNotBlank(orgUserParam.getEmail())) {
                MdUser user = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getEmail, orgUserParam.getEmail()));
//                List<MdUser> userList = mdUserMapper.selectList(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getEmail, orgUserParam.getEmail()).select(MdUser::getPhone));
                if (ObjectUtils.isNotEmpty(user) && ((ObjectUtils.isEmpty(user.getPhone())) ? true : user.getPhone().equals(orgUserParam.getPhone()))) {
                    //手机号不存在，邮箱号存在，邮箱号绑定的手机号与输入的手机号一致或为空
                    user.setEmail(orgUserParam.getEmail());
                    user.setNickname(orgUserParam.getNickname());
                    user.setPhone(orgUserParam.getPhone());
                    orgUserParam.setId(user.getId());
                    mdUserMapper.updateById(user);
                    mdUserOrgService.addOrgUser(orgUserParam);
                    passwordVO.setUserId(user.getId());
                } else if (ObjectUtils.isEmpty(user)) {
                    checkoutUser(orgUserParam);
                    passwordVO = addUser(orgUserParam);
                } else {
                    throw new IllegalParamException("该邮箱号已与其他用户手机号绑定，请确认用户手机号后再重新添加！");
                }
            } else {
                passwordVO = addUser(orgUserParam);
            }
        } else {
            MdBlackUser blackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, orgUserParam.getAppId())
                    .eq(MdBlackUser::getUserId, mdUser.getId()).last(" limit 1"));
            if (ObjectUtils.isNotEmpty(blackUser)) {
                throw new IllegalParamException("此用户已存在于本系统黑名单，请勿重复添加");
            }
            passwordVO.setUserId(mdUser.getId());
            passwordVO.setPassword(mdUser.getPassword());
            //判断邮箱号是否已被注册
            if (StringUtils.isNotBlank(orgUserParam.getEmail())) {
                Integer count = mdUserMapper.selectCount(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getEmail, orgUserParam.getEmail()).ne(MdUser::getPhone, orgUserParam.getPhone()));
                if (count > 0) {
                    throw new IllegalParamException("该邮箱号已被注册，但与原手机号不符，请确认是否同一用户或修改后再提交");
                }
            }
            //添加人员覆盖之前的空邮箱
            if (StringUtils.isBlank(mdUser.getEmail()) && StringUtils.isNotBlank(orgUserParam.getEmail())) {
                mdUser.setEmail(orgUserParam.getEmail());
                mdUserMapper.updateById(mdUser);
            }
            orgUserParam.setId(mdUser.getId());
            update(orgUserParam);

        }
        return passwordVO;
    }

    /**
     * 新增员工
     *
     * @param orgUserParam
     * @return
     */
    PasswordVO addUser(OrgUserParam orgUserParam) throws Exception {
        //员工不存在,添加员工,并在用户、组织、角色关联表添加
        //根据账号创建初密码
        MdUser user = new MdUser();
        user.setNickname(orgUserParam.getNickname());
        String abbreviation = null;
        try {
            abbreviation = chineseToPinYinS(orgUserParam.getNickname());
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        StringBuilder pinyinBuilder = new StringBuilder();
        String userPinyin = null;
        if (PinYinMultiCharactersUtils.isMultiChineseWord(orgUserParam.getNickname())) {
            userPinyin = pinyinBuilder.append(getChinesePinyinFromName(orgUserParam.getNickname())).append(",")
                    .append(abbreviation).append(",")
                    .append(getMultiCharactersPinYin(orgUserParam.getNickname())).toString();
        } else {
            userPinyin = pinyinBuilder.append(getChinesePinyinFromName(orgUserParam.getNickname())).append(",")
                    .append(abbreviation).toString();
        }
        user.setPinyin(userPinyin);
        user.setEmail(orgUserParam.getEmail());
        user.setPhone(orgUserParam.getPhone());
        //校验用户信息
        String pw = null;
        if (!StringUtils.isBlank(orgUserParam.getAccount())) {
            StringBuilder stringBuilder = new StringBuilder();
//            String account = stringBuilder.append(orgUserParam.getTenantId()).append(orgUserParam.getAccount()).toString();
            user.setAccount(orgUserParam.getAccount());
            pw = RandomAccountGenerator.getRandomPassword(8);
            user.setPassword(passwordEncoder.encode(pw));
        }
        //系统是否设置默认密码
        String appInitialPassword = mdAppMapper.getAppInitialPassword(orgUserParam.getAppId());
        if (!StringUtils.isBlank(appInitialPassword)) {
            user.setPassword(passwordEncoder.encode(appInitialPassword));
        }
        orgUserParam.setAccount(user.getAccount());
        if (null == orgUserParam.getIsCheckAccount() || orgUserParam.getIsCheckAccount() == true) {
            checkoutUser(orgUserParam);
        }
        mdUserMapper.insert(user);
        orgUserParam.setId(user.getId());
        //用户、组织、角色关联表添加关联关系
        mdUserOrgService.addOrgUser(orgUserParam);
        PasswordVO passwordVO = new PasswordVO();
        passwordVO.setPassword(pw);
        passwordVO.setUserId(user.getId());
        return passwordVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PasswordVO addExternalUser(OrgUserParam orgUserParam) throws Exception {
        PasswordVO passwordVO = new PasswordVO();
        if (ObjectUtils.isEmpty(orgUserParam.getNickname())) {
            throw new IllegalParamException("请输入姓名！");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getOrgIds())) {
            throw new IllegalParamException("组织不可为空");
        }
        //判断添加的成员是通过手机号添加还是账号添加
        checkoutUser(orgUserParam);
        if (ObjectUtils.isNotEmpty(orgUserParam.getPhone())) {
            //如果添加用户电话不为空，使用电话进行创建
            passwordVO = addUserByPhone(orgUserParam);
        } else if (ObjectUtils.isNotEmpty(orgUserParam.getAccount()) && ObjectUtils.isEmpty(orgUserParam.getPhone())) {
            //通过账号进行创建
            //判断账号名是否已经被注册
            passwordVO = addUserByAccount(orgUserParam);
        } else if (ObjectUtils.isNotEmpty(orgUserParam.getEmail()) && ObjectUtils.isEmpty(orgUserParam.getAccount())) {
            passwordVO = addByEmail(orgUserParam);
        }
        return passwordVO;
    }

    /**
     * 模糊展示租户下所有员工
     *
     * @param tenantId
     * @param userState
     * @return
     */
    @Override
    public PageResult<UserDetailsVO> getUserList(Long pageNum, Long pageSize, Long tenantId, Long orgId, String account, String keyword, String appId, Integer userState) {
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new IllegalParamException("租户ID不能为空");
        }
        if (ObjectUtils.isEmpty(appId)) {
            throw new IllegalParamException("appID不能为空");
        }
        if (keyword != null && !keyword.isEmpty() && keyword.chars().allMatch(Character::isWhitespace)) {
            return PageResult.build(pageNum, pageSize, 0L, 0L, null);
        }
        Integer status = null;
        if (!ObjectUtils.isEmpty(userState)) {
            if (userState == 0) {
                status = MdUserStatusConstant.NORMAL;
            } else if (userState == 1) {
                status = MdUserStatusConstant.BLACK;
            } else {
                status = null;
            }
        }
        //查找租客下所有人员
        IPage<UserDetailsVO> page = mdUserMapper.searchUserList(new Page<>(pageNum, pageSize), keyword, tenantId, account, orgId, appId, status);
        return PageResult.build(pageNum, pageSize, page.getPages(), page.getTotal(), getOrgList(page.getRecords(), orgId, tenantId, appId));
    }

    /**
     * 获取所有组织
     *
     * @param userDetailsVOList
     * @param appId
     * @return
     */
    private List<UserDetailsVO> getOrgList(List<UserDetailsVO> userDetailsVOList, Long orgId, Long tenantId, String appId) {

        for (UserDetailsVO userDetailsVO : userDetailsVOList) {
            List<RoleVO> mdRoleList = userTenantRoleService.getRoleListByTenantAndUserId(tenantId, userDetailsVO.getId(), appId);
            if (mdRoleList != null) {
                List<Long> idList = mdRoleList.stream()
                        .map(RoleVO::getId)
                        .collect(Collectors.toList());
                List<String> nameList = mdRoleList.stream()
                        .map(RoleVO::getName)
                        .collect(Collectors.toList());
                userDetailsVO.setRoleList(idList);
                userDetailsVO.setRoleName(nameList);
            } else {
                //必要返回前端一个数组
                userDetailsVO.setRoleList(new ArrayList<>());
                userDetailsVO.setRoleName(new ArrayList<>());
            }
            List<OrgNewVO> orgNewParamsList;
            orgNewParamsList = mdUserMapper.getOrgNameByUserId(userDetailsVO.getId(), orgId, tenantId);
            userDetailsVO.setOrgList(orgNewParamsList);
            userDetailsVO.setTenantTime(getTenantTime(tenantId, userDetailsVO.getId()));
            List<UserChannel> userChannelList = mdUserChannelService.getChannelListByUserId(appId, tenantId, userDetailsVO.getId());
            List<String> channelName = new ArrayList<>();
            if (!CollectionUtils.isEmpty(userChannelList)) {
                for (int i = 0; i < userChannelList.size(); i++) {
                    UserChannel userChannel = userChannelList.get(i);
                    channelName.add(userChannel.getName());
                }
                userDetailsVO.setChannelName(channelName);
            }
            if (StringUtils.isNotBlank(userDetailsVO.getPhone())) {
                userDetailsVO.setPhone(PhoneNumberUtils.maskPhoneNumber(userDetailsVO.getPhone()));
            }
            if (StringUtils.isNotBlank(userDetailsVO.getStatus())) {
                if (userDetailsVO.getStatus().equals("0")) {
                    userDetailsVO.setStatus("黑名单状态");
                } else {
                    userDetailsVO.setStatus("正常");
                }
                userDetailsVO.setPhone(PhoneNumberUtils.maskPhoneNumber(userDetailsVO.getPhone()));
            }
        }
        return userDetailsVOList;
    }


    /**
     * 修改人员信息
     *
     * @param userParam
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(OrgUserParam userParam) throws Exception {
        MdUserOperationLog mdUserOperationLog = new MdUserOperationLog();
        mdUserOperationLog.setOperateUserId(userParam.getOperateUserId());
        mdUserOperationLog.setOperateUserName(userParam.getOperateUserName());
        mdUserOperationLog.setOperationType(UserOperationType.UPDATE.getCode());
        mdUserOperationLog.setAppId(userParam.getAppId());
        mdUserOperationLog.setTenantId(userParam.getTenantId());
        mdUserOperationLog.setUserId(userParam.getId());
        mdUserOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdUserOperationLog.setCreateTime(new Date());
        mdUserOperationLog.setNotes(JSON.toJSONString(userParam));
        mdUserOperationLog.setUserIp(userParam.getUserIp());
        mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
        //校验账号信息
        checkoutUser(userParam);
        MdUser mdUser = new MdUser();
        mdUser.setId(userParam.getId());
        mdUser.setPhone(userParam.getPhone());
        mdUser.setNickname(userParam.getNickname());
        String abbreviation = null;
        try {
            abbreviation = chineseToPinYinS(userParam.getNickname());
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        StringBuilder pinyinBuilder = new StringBuilder();
        String userPinyin = null;
        if (PinYinMultiCharactersUtils.isMultiChineseWord(userParam.getNickname())) {
            userPinyin = pinyinBuilder.append(getChinesePinyinFromName(userParam.getNickname())).append(",")
                    .append(abbreviation).append(",")
                    .append(getMultiCharactersPinYin(userParam.getNickname())).toString();
        } else {
            userPinyin = pinyinBuilder.append(getChinesePinyinFromName(userParam.getNickname())).append(",")
                    .append(abbreviation).toString();
        }
        mdUser.setPinyin(userPinyin);
        mdUser.setEmail(userParam.getEmail());
        mdUser.setAccount(userParam.getAccount());
        mdUserMapper.updateById(mdUser);
        //新增用户组织关联关系
        mdUserOrgService.addOrgUser(userParam);

    }

    /**
     * 校验用户参数
     *
     * @param orgUserParam
     */
    public void checkoutUser(OrgUserParam orgUserParam) {
        //判断邮箱号是否已被注册
        if (StringUtils.isNotBlank(orgUserParam.getEmail())) {
            Integer count = mdUserMapper.selectCount(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getEmail, orgUserParam.getEmail()).ne(MdUser::getId, orgUserParam.getId()));
            if (count > 0) {
                throw new IllegalParamException("该邮箱号已被注册，但与原手机号不符，请确认是否同一用户或修改后再提交");
            }
        }
        if (StringUtils.isNotBlank(orgUserParam.getPhone())) {
            Integer count = mdUserMapper.selectCount(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getPhone, orgUserParam.getPhone()).ne(MdUser::getId, orgUserParam.getId()));
            if (count > 0) {
                throw new IllegalParamException("该手机号已被注册，但与原邮箱号不符，请确认是否同一用户或修改后再提交");
            }
        }
        if (orgUserParam.getNickname().length() > 30) {
            throw new IllegalParamException("最长可输入30位字符，请修改!");
        }
        if (StringUtils.isNotBlank(orgUserParam.getEmail()) && !orgUserParam.getEmail().matches("^[a-zA-Z0-9\\@\\.]+$")) {
            throw new IllegalParamException("邮箱号仅支持输入英文、数字，“@”、“.”，请修改！");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getNickname())) {
            throw new IllegalParamException("成员姓名不能为空");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getOrgIds())) {
            throw new IllegalParamException("组织不可为空");
        }
//        if (ObjectUtils.isEmpty(orgUserParam.getPhone()) && ObjectUtils.isEmpty(orgUserParam.getAccount())) {
//            throw new IllegalParamException("不允许手机号、账号均为空");
//        }
        if (StringUtils.isNotBlank(orgUserParam.getAccount())) {
            Integer count = mdUserMapper.selectCount(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getAccount, orgUserParam.getAccount()).ne(MdUser::getId, orgUserParam.getId()));
            if (count > 0) {
                throw new IllegalParamException("该账号已被注册，请修改！");
            }
            String reg = "^[^0-9][a-zA-Z0-9_\\-\\\\.\\\\w]+$";
            if (orgUserParam.getAccount().matches("^[a-zA-Z]")) {
                throw new IllegalParamException("账号的首位字符需为英文!");
            }
            if (!orgUserParam.getAccount().matches(reg)) {
                throw new IllegalParamException("账号仅支持数字、英文、中划线、下划线、'.' 等格式字符创建且账号首位字符需为英文");
            }
            if (orgUserParam.getAccount().length() < 3) {
                throw new IllegalParamException("账号不可少于3位字符!");
            }
            if (orgUserParam.getAccount().length() > 30) {
                throw new IllegalParamException("账号最长支持输入30位字符!");
            }
        }
        if (StringUtils.isNotBlank(orgUserParam.getPhone()) && !orgUserParam.getPhone().matches("^[0-9]+$")) {
            throw new IllegalParamException("手机号仅支持输入数字，请修改!");
        }

    }

    /**
     * 删除人员信息
     *
     * @param userParamList
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DeleteUserParam> userParamList) {

        if (CollectionUtils.isEmpty(userParamList)) {
            throw new IllegalParamException("参数不能为空");
        }
        for (DeleteUserParam userParam : userParamList) {
            MdUserOperationLog mdUserOperationLog = new MdUserOperationLog();
            mdUserOperationLog.setOperateUserId(userParam.getOperateUserId());
            mdUserOperationLog.setOperateUserName(userParam.getOperateUserName());
            mdUserOperationLog.setOperationType(UserOperationType.DELETE.getCode());
            mdUserOperationLog.setAppId(userParam.getAppId());
            mdUserOperationLog.setTenantId(userParam.getTenantId());
            mdUserOperationLog.setUserId(userParam.getUserId());
            mdUserOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
            mdUserOperationLog.setCreateTime(new Date());
            mdUserOperationLog.setUserIp(userParam.getUserIp());
            mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
            QueryWrapper<MdUserOrg> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userParam.getUserId()).eq("org_id", userParam.getOrgId());
            mdUserOrgMapper.delete(wrapper);
            //判断租户下有无此成员的，若该租户下没有此成员，需将此成员的租户角色人员关联关系删除
            Long tenantId = orgMapper.selectOne(Wrappers.<Org>lambdaQuery().eq(Org::getId, userParam.getOrgId())).getTenantId();
            Integer count = mdUserOrgMapper.selectCount(Wrappers.<MdUserOrg>lambdaQuery().eq(MdUserOrg::getTenantId, tenantId).
                    eq(MdUserOrg::getUserId, userParam.getUserId()));
            //如果在人员组织表中不存在该成员的任何关联信息，删除人员与该组织角色的绑定关系
            if (count == 0) {
                userTenantRoleMapper.delete(Wrappers.<UserTenantRole>lambdaQuery().eq(UserTenantRole::getUserId, userParam.getUserId()).
                        eq(UserTenantRole::getTenantId, tenantId));
            }
            userChannelRelatedMapper.delete(new QueryWrapper<UserChannelRelated>().eq("tenant_id", tenantId).eq("user_id", userParam.getUserId()));
        }


    }

    /**
     * 查看看用户详情
     *
     * @param id     用户Id
     * @param action
     * @return 用户信息
     */
    @Override
    public UserDetailsVO getUserDetail(Long id, Long orgId, Long tenantId, String appId, String action) {
        if (ObjectUtils.isEmpty(id)) {
            throw new IllegalParamException("用户ID不能为空");
        }
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new IllegalParamException("租户ID不能为空");
        }
        if (ObjectUtils.isEmpty(appId)) {
            throw new IllegalParamException("项目ID不能为空");
        }
        //查找此人员的所在组织
        List<OrgNewVO> orgNewParamsList = mdUserMapper.getOrgNameByUserId(id, orgId, tenantId);
        //查找此人员的所有角色
        List<UserTenantRole> userTenantRoles = userTenantRoleMapper.selectList(Wrappers.<UserTenantRole>lambdaQuery()
                .eq(UserTenantRole::getUserId, id)
                .eq(UserTenantRole::getTenantId, tenantId)
                .eq(UserTenantRole::getAppId, appId));
        List<Long> roleList = new ArrayList<>();
        for (UserTenantRole userTenantRole : userTenantRoles) {
            roleList.add(userTenantRole.getRoleId());
        }
        List<Long> listChannelIds = mdUserChannelService.getChannelListByUser(appId, tenantId, id);
        //查找人员
        MdUser mdUser = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery()
                .eq(MdUser::getId, id)
                .eq(MdUser::getDeleted, false));
        if (ObjectUtils.isEmpty(mdUser)) {
            return null;
        } else {
            UserDetailsVO userDetail = new UserDetailsVO();
            userDetail.setNickname(mdUser.getNickname());
            if (StringUtils.isBlank(action)) {
                userDetail.setPhone(PhoneNumberUtils.maskPhoneNumber(mdUser.getPhone()));
            } else {
                userDetail.setPhone(mdUser.getPhone());
            }
            userDetail.setEmail(mdUser.getEmail());
            userDetail.setOrgList(orgNewParamsList);
            userDetail.setRoleList(roleList);
            userDetail.setChannelIds(listChannelIds);
            userDetail.setAccount(mdUser.getAccount());
            Date tenantTime = getTenantTime(tenantId, id);
            userDetail.setCreateTime(tenantTime);
            userDetail.setId(mdUser.getId());
            return userDetail;
        }
    }

    public Date getTenantTime(Long tenantId, Long userId) {
        List<MdUserOrg> timeList = mdUserOrgMapper.selectList(Wrappers.<MdUserOrg>lambdaQuery().
                eq(MdUserOrg::getUserId, userId).
                eq(MdUserOrg::getTenantId, tenantId));
        if (ObjectUtils.isNotEmpty(timeList)) {
            timeList = timeList.stream().sorted(Comparator.comparing(MdUserOrg::getCreateTime)).collect(Collectors.toList());
            return timeList.get(0).getCreateTime();
        } else {
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(DeleteUserParam deleteUserParam) {
        if (ObjectUtils.isEmpty(deleteUserParam.getUserId())) {
            mdUserOrgMapper.deleteUser(deleteUserParam.getOrgId());
        } else if (!ObjectUtils.isEmpty(deleteUserParam.getOrgId())) {
            QueryWrapper<MdUserOrg> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", deleteUserParam.getUserId()).eq("org_id", deleteUserParam.getOrgId());
            mdUserOrgMapper.delete(wrapper);
        }
    }

    @Override
    public UserInfoVO getUserInfo(String phone, Long tenantId, String appId) {
        if (ObjectUtils.isEmpty(phone)) {
            throw new IllegalParamException("电话号码不能为空");
        }
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new IllegalParamException("租户不能为空");
        }
        if (ObjectUtils.isEmpty(appId)) {
            throw new IllegalParamException("项目ID不能为空");
        }
        //查找人员信息
        MdUser mdUser = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery()
                .eq(MdUser::getPhone, phone));
        if (ObjectUtils.isEmpty(mdUser)) {
            mdUser = mdUserMapper.searchByEmail(phone);
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        if (!ObjectUtils.isEmpty(mdUser)) {
            userInfoVO.setId(mdUser.getId());
            userInfoVO.setNickname(mdUser.getNickname());
            userInfoVO.setEmail(mdUser.getEmail());
            userInfoVO.setPhone(mdUser.getPhone());
            userInfoVO.setAccount(mdUser.getAccount());
            //查找角色信息
            List<UserTenantRole> userTenantRoleList = userTenantRoleMapper.selectList(Wrappers.<UserTenantRole>lambdaQuery()
                    .select(UserTenantRole::getRoleId)
                    .eq(UserTenantRole::getUserId, mdUser.getId())
                    .eq(UserTenantRole::getTenantId, tenantId)
                    .eq(UserTenantRole::getAppId, appId));
            //查找组织 信息
            List<OrgNewVO> mdUserOrgList = mdUserOrgMapper.getOrgInfo(mdUser.getId(), tenantId);
            List<Long> roleList = new ArrayList<>();

            for (UserTenantRole userTenantRole : userTenantRoleList) {
                roleList.add(userTenantRole.getRoleId());
            }
            userInfoVO.setRoleList(roleList);
            userInfoVO.setOrgList(mdUserOrgList);

            //判断该用户不在同一租户下
            if (ObjectUtils.isEmpty(mdUserOrgList)) {
                userInfoVO.setLogo("该用户已存在于统一账号资源库中，请确认是否同一用户");
            }
        }
        return userInfoVO;
    }

    /**
     * 删除单个用户
     *
     * @param deleteUserParam
     */
    @Override
    public void deleteOrgUser(DeleteUserParam deleteUserParam) {
        if (ObjectUtils.isEmpty(deleteUserParam)) {
            throw new IllegalParamException("用户信息不能为空");
        }
        if (ObjectUtils.isEmpty(deleteUserParam.getUserId())) {
            throw new IllegalParamException("用户ID不为空");
        }
        if (ObjectUtils.isEmpty(deleteUserParam.getOrgId())) {
            throw new IllegalParamException("组织ID不能为空");
        }
        mdUserOrgMapper.delete(Wrappers.<MdUserOrg>lambdaQuery()
                .eq(MdUserOrg::getOrgId, deleteUserParam.getOrgId())
                .eq(MdUserOrg::getUserId, deleteUserParam.getUserId()));
    }

    /**
     * 获取租户下所有用户
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<TenantUserVO> getUserByTenantId(Long tenantId, Long orgId, String keyword) {
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new IllegalParamException("租户ID不能为空");
        }
        List<TenantUserVO> tenantUserVOList = mdUserMapper.selectUserByTenantId(tenantId, orgId, keyword);
        return tenantUserVOList;
    }

    @Override
    public UserVO userByPhone(String phone) {
        MdUser mdUser = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getPhone, phone));
        UserVO userVO = new UserVO();
        if (ObjectUtils.isNotEmpty(mdUser)) {
            userVO.setNickname(mdUser.getNickname());
            userVO.setPhone(mdUser.getPhone());
        } else {
            userVO.setPhone(phone);
        }
        return userVO;
    }

    @Override
    public List<UserInfoVO> listUserDetails(List<String> keys, String type, Long tenantId, Long height) {
        if (ObjectUtils.isEmpty(keys)) {
            throw new IllegalParamException("传参错误，请重试");
        }
        List<UserInfoVO> userInfoVOList = new ArrayList<>();
        for (String key : keys) {
            UserInfoVO userInfoVO = mdUserMapper.selectByKey(key, type, tenantId);
            if (userInfoVO != null && StringUtils.isNotBlank(userInfoVO.getLogo())) {
                List<OrgNewVO> orgById = new ArrayList<>();

                List<String> orgIds = Arrays.asList(userInfoVO.getLogo().split(","));
                List<Long> longs = orgIds.stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
                QueryOrgNameParam nameParam = new QueryOrgNameParam();
                nameParam.setIds(longs);
                nameParam.setHeight(height);
                OrgHeightNameVO orgName = orgService.getOrgName(nameParam);
                for (int i = 0; i < orgName.getOrgIdLists().size(); i++) {
                    OrgNewVO orgNewVO = new OrgNewVO();
                    orgNewVO.setOrgId(orgName.getOrgIdLists().get(i));
                    orgNewVO.setOrgName(orgName.getOrgNameLists().get(i));
                    orgNewVO.setCustomId(orgName.getOrgCustomIdLists().get(i));
                    orgById.add(orgNewVO);
                }
                userInfoVO.setOrgList(orgById);
                userInfoVO.setLogo(null);
            }
            userInfoVOList.add(userInfoVO);
        }
        return userInfoVOList;
    }

    @Override
    public UserVO getUserInfoByAccount(String account) {
        MdUser mdUser = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getPhone, account).or().eq(MdUser::getAccount, account));
        UserVO userVO = new UserVO();
        if (ObjectUtils.isNotEmpty(mdUser)) {
            userVO.setId(mdUser.getId());
            userVO.setNickname(mdUser.getNickname());
            userVO.setPhone(mdUser.getPhone());
            userVO.setAccount(mdUser.getAccount());
        }
        return userVO;
    }

    @Override
    public Map<String, Object> importTeamUser(ImportTeamUserParam param) {
        List<ExcelImportTeamUserParam> successList = new ArrayList();
        List<ExcelImportTeamUserParam> failList = new ArrayList();
        if (param != null && param.getUserHeads().size() > 0) {
            List<OrgNewVO> orgList = mdUserOrgMapper.getOrgInfo(param.getUserId(), param.getTenantId());
            if (CollectionUtils.isEmpty(orgList)) {
                throw new IllegalParamException("没有可匹配的组织！");
            }

            for (ExcelImportTeamUserParam excelImportTeamUserHead : param.getUserHeads()) {
                StringBuilder fileMsg = new StringBuilder();
                if (StringUtils.isBlank(excelImportTeamUserHead.getNickname())) {
                    fileMsg.append("用户姓名为空！");
                    excelImportTeamUserHead.setStatus("1");
                    excelImportTeamUserHead.setFailReason(fileMsg.toString());
                    failList.add(excelImportTeamUserHead);
                    continue;
                }
                if (StringUtils.isBlank(excelImportTeamUserHead.getOrgName())) {
                    if (!StringUtils.isBlank(fileMsg)) {
                        fileMsg.append(",");
                    }
                    fileMsg.append("用户组织为空！");
                    excelImportTeamUserHead.setStatus("1");
                    excelImportTeamUserHead.setFailReason(fileMsg.toString());
                    failList.add(excelImportTeamUserHead);
                    continue;
                }
                if (StringUtils.isBlank(excelImportTeamUserHead.getPhone()) && StringUtils.isBlank(excelImportTeamUserHead.getEmail())) {
                    if (!StringUtils.isBlank(fileMsg)) {
                        fileMsg.append(",");
                    }
                    fileMsg.append("用户手机号和邮箱号不能同时为空！");
                    excelImportTeamUserHead.setStatus("1");
                    excelImportTeamUserHead.setFailReason(fileMsg.toString());
                    failList.add(excelImportTeamUserHead);
                    continue;
                }
                if (StringUtils.isBlank(excelImportTeamUserHead.getOrgName())) {
                    if (!StringUtils.isBlank(fileMsg)) {
                        fileMsg.append(",");
                    }
                    fileMsg.append("用户组织不能为空！");
                    excelImportTeamUserHead.setStatus("1");
                    excelImportTeamUserHead.setFailReason(fileMsg.toString());
                    failList.add(excelImportTeamUserHead);
                    continue;
                }
                if (StringUtils.isBlank(excelImportTeamUserHead.getRoleName())) {
                    if (!StringUtils.isBlank(fileMsg)) {
                        fileMsg.append(",");
                    }
                    fileMsg.append("用户角色不能为空！");
                    excelImportTeamUserHead.setStatus("1");
                    excelImportTeamUserHead.setFailReason(fileMsg.toString());
                    failList.add(excelImportTeamUserHead);
                    continue;
                }
                if (excelImportTeamUserHead.getNickname().length() > 20) {
                    if (!StringUtils.isBlank(fileMsg)) {
                        fileMsg.append(",");
                    }
                    fileMsg.append("用户姓名不能超过20个字符");
                    excelImportTeamUserHead.setStatus("1");
                    excelImportTeamUserHead.setFailReason(fileMsg.toString());
                    failList.add(excelImportTeamUserHead);
                    continue;
                } else {
                    String regex = "^[\\p{L}\\u4e00-\\u9fff\\d·]+$";

                    // 编译正则表达式
                    Pattern pattern = Pattern.compile(regex);

                    // 创建Matcher对象
                    Matcher matcher = pattern.matcher(excelImportTeamUserHead.getNickname());
                    if (!matcher.matches()) {
                        if (!StringUtils.isBlank(fileMsg)) {
                            fileMsg.append(",");
                        }
                        fileMsg.append("姓名仅支持中英文数字字符及“·”，请修改!");
                        excelImportTeamUserHead.setStatus("1");
                        excelImportTeamUserHead.setFailReason(fileMsg.toString());
                        failList.add(excelImportTeamUserHead);
                        continue;
                    }

                }
                //用户手机号格式校验
                if (!StringUtils.isBlank(excelImportTeamUserHead.getPhone())) {
                    if (!excelImportTeamUserHead.getPhone().matches("^1[3-9]\\d{9}$")) {
                        if (!StringUtils.isBlank(fileMsg)) {
                            fileMsg.append(",");
                        }
                        fileMsg.append("用户手机号格式不正确");
                        excelImportTeamUserHead.setStatus("1");
                        excelImportTeamUserHead.setFailReason(fileMsg.toString());
                        failList.add(excelImportTeamUserHead);
                        continue;
                    }
                }
                if (!StringUtils.isBlank(excelImportTeamUserHead.getEmail())) {
                    if (excelImportTeamUserHead.getEmail().length() > 10 || !excelImportTeamUserHead.getEmail().matches("^[a-zA-Z0-9]+$")) {
                        if (!StringUtils.isBlank(fileMsg)) {
                            fileMsg.append(",");
                        }
                        fileMsg.append("邮箱号格式不正确！");
                        excelImportTeamUserHead.setStatus("1");
                        excelImportTeamUserHead.setFailReason(fileMsg.toString());
                        failList.add(excelImportTeamUserHead);
                        continue;
                    }
                }
                Long childOrgId = 0L;
                String[] pathOrg = excelImportTeamUserHead.getOrgName().split("/");
                String orgName = "";
                if (pathOrg.length > 0) {
                    orgName = pathOrg[pathOrg.length - 1];
                }
                List<OrgVO> orgVOList = orgMapper.getOrgByName(param.getTenantId(), orgName);
                for (OrgVO orgNewVO :
                        orgVOList) {
                    List<OrgVO> pathList = new ArrayList<>();
                    pathList = selectOrgPath(param.getTenantId(), orgNewVO.getId(), pathList);
                    if (!CollectionUtils.isEmpty(pathList)) {
                        List<String> pathNameList = pathList.stream().map(OrgVO::getName).collect(Collectors.toList());
                        if (!CollectionUtils.isEmpty(pathNameList)) {
                            Collections.reverse(pathNameList);
                        }
                        String orgPath = String.join("/", pathNameList);
                        if (excelImportTeamUserHead.getOrgName().startsWith(orgPath)) {
                            if (excelImportTeamUserHead.getOrgName().equals(orgPath)) {
                                childOrgId = orgNewVO.getId();
                            }
                        }
                    }
                }
                if (childOrgId == null || childOrgId.compareTo(0L) == 0) {
                    if (!StringUtils.isBlank(fileMsg)) {
                        fileMsg.append(",");
                    }
                    fileMsg.append("请填写组织");
                } else {
                    List<OrgNewVO> userOrdlist = mdUserOrgMapper.getOrgInfo(param.getUserId(), param.getTenantId());
                    if (!CollectionUtils.isEmpty(userOrdlist)) {
                    }
                    for (OrgNewVO orgNewVO : userOrdlist) {
                        List<Long> subOrgIdList = orgMapper.getSubId(orgNewVO.getOrgId());
                        if (subOrgIdList.contains(childOrgId)) {
                            excelImportTeamUserHead.setOrgId(childOrgId);
                            break;
                        }
                    }
                    if (excelImportTeamUserHead.getOrgId() == null || excelImportTeamUserHead.getOrgId() == 0) {
                        if (!StringUtils.isBlank(fileMsg)) {
                            fileMsg.append(",");
                        }
                        fileMsg.append("导入组织不在该用户组织范围内！");
                    }
                }
                MdUser mdUser = null;
                if (StringUtils.isNotBlank(excelImportTeamUserHead.getPhone())) {
                    mdUser = mdUserMapper.searchByPhone(excelImportTeamUserHead.getPhone());
                }
                if (mdUser == null && StringUtils.isNotBlank(excelImportTeamUserHead.getEmail())) {
                    mdUser = mdUserMapper.searchByEmail(excelImportTeamUserHead.getEmail());
                }
                if (mdUser != null) {
                    MdBlackUser blackUser = mdBlackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, param.getAppId())
                            .eq(MdBlackUser::getUserId, mdUser.getId()).last(" limit 1"));
                    if (ObjectUtils.isNotEmpty(blackUser)) {
                        if (!StringUtils.isBlank(fileMsg)) {
                            fileMsg.append(",");
                        }
                        fileMsg.append("此用户已添加到黑名单，不允许添加");
                    }
                }
                if (StringUtils.isNotBlank(excelImportTeamUserHead.getPhone()) && StringUtils.isNotBlank(excelImportTeamUserHead.getEmail())) {
                    mdUser = mdUserMapper.searchByPhone(excelImportTeamUserHead.getPhone());
                    if (mdUser != null && StringUtils.isNotBlank(mdUser.getEmail())) {
                        if (!mdUser.getEmail().equals(excelImportTeamUserHead.getEmail())) {
                            if (!StringUtils.isBlank(fileMsg)) {
                                fileMsg.append(",");
                            }
                            fileMsg.append("此手机号已绑定其他邮箱号");
                        }
                    }
                    //判断用户是否加入黑名单如果加入不让导入
                    mdUser = mdUserMapper.searchByEmail(excelImportTeamUserHead.getEmail());
                    if (mdUser != null && StringUtils.isNotBlank(mdUser.getPhone())) {
                        if (!mdUser.getPhone().equals(excelImportTeamUserHead.getPhone())) {
                            if (!StringUtils.isBlank(fileMsg)) {
                                fileMsg.append(",");
                            }
                            fileMsg.append("此邮箱号已被其他手机号绑定");
                        }
                    }
                }
                List<RoleVO> roleVOList = userTenantRoleMapper.getRoleListByUserId(param.getAppId(), param.getTenantId(), param.getUserId());
                for (RoleVO roleVO : roleVOList) {
                    if (roleVO.getName().equals(excelImportTeamUserHead.getRoleName())) {
                        excelImportTeamUserHead.setRoleId(roleVO.getId());
                    }
                }
                if (excelImportTeamUserHead.getRoleName().contains("一级") || excelImportTeamUserHead.getRoleName().contains("管理员")) {
                    if (!StringUtils.isBlank(fileMsg)) {
                        fileMsg.append(",");
                    }
                    fileMsg.append("用户没有要导入的权限！");
                    excelImportTeamUserHead.setRoleId(null);
                }
                if (excelImportTeamUserHead.getRoleId() == null) {
                    if (!StringUtils.isBlank(fileMsg)) {
                        fileMsg.append(",");
                    }
                    fileMsg.append("沒有可匹配的角色，请重新填写！");
                }
                if (excelImportTeamUserHead.getChannelName() != null) {
                    UserChannel userChannel = mdUserChannelService.getChannelByName(param.getAppId(), param.getTenantId(), excelImportTeamUserHead.getChannelName());
                    if (null == userChannel) {
                        fileMsg.append(",");
                        fileMsg.append("用户群体不存在，请重新填写！");
                    } else {
                        excelImportTeamUserHead.setChannelId(userChannel.getId());
                        excelImportTeamUserHead.setChannelName(userChannel.getName());
                    }

                }
                if (!StringUtils.isBlank(fileMsg)) {
                    excelImportTeamUserHead.setStatus("1");
                    excelImportTeamUserHead.setFailReason(fileMsg.toString());
                    failList.add(excelImportTeamUserHead);
                } else {
                    excelImportTeamUserHead.setStatus("0");
                    excelImportTeamUserHead.setFailReason("");
                    successList.add(excelImportTeamUserHead);
                }
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("successData", successList);
        resultMap.put("failData", failList);
        return resultMap;
    }

    /**
     * 递归查找组织树路径
     *
     * @param tenantId
     * @param orgId
     * @param orgVOList
     * @return
     */
    @Override
    public List<OrgVO> selectOrgPath(Long tenantId, Long orgId, List<OrgVO> orgVOList) {
        OrgVO orgVO = orgMapper.getMeOrg(orgId, tenantId);
        if (orgVO == null || orgVO.getPid() == 0) {
            orgVOList.add(orgVO);
            return orgVOList;
        } else {
            orgVOList.add(orgVO);
        }
        return selectOrgPath(tenantId, orgVO.getPid(), orgVOList);
    }

    @Override
    @Async
    public List<MdUser> saveUserData(ImportTeamUserParam param) {
        List<MdUser> listUser = new ArrayList<>();
        if (param != null && !CollectionUtils.isEmpty(param.getUserHeads())) {
            for (ExcelImportTeamUserParam excelImportTeamUserHead : param.getUserHeads()) {
                OrgUserParam orgUserParam = new OrgUserParam();
                orgUserParam.setNickname(excelImportTeamUserHead.getNickname());
                orgUserParam.setTenantId(param.getTenantId());
                orgUserParam.setEmail(excelImportTeamUserHead.getEmail());
                orgUserParam.setPhone(excelImportTeamUserHead.getPhone());
                Set<Long> orgSet = Sets.newHashSet();
                orgSet.add(excelImportTeamUserHead.getOrgId());
                orgUserParam.setOrgIds(orgSet);
                Set<Long> orgRole = Sets.newHashSet();
                orgRole.add(excelImportTeamUserHead.getRoleId());
                orgUserParam.setRoleList(orgRole);
                orgUserParam.setAppId(param.getAppId());
                orgUserParam.setUserIp(param.getUserIp());
                orgUserParam.setOperateUserId(param.getOperateUserId());
                orgUserParam.setOperateUserName(param.getOperateUserName());
                if (ObjectUtils.isEmpty(orgUserParam.getRoleList())) {
                    throw new IllegalParamException("角色不能为空！");
                }
                PasswordVO passwordVO = null;
                try {
                    passwordVO = add(orgUserParam);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (passwordVO != null && passwordVO.getUserId() != null) {
                    UserTenantRoleParam userTenantRoleParam = new UserTenantRoleParam();
                    userTenantRoleParam.setUserId(passwordVO.getUserId());
                    userTenantRoleParam.setTenantId(param.getTenantId());
                    userTenantRoleParam.setAppId(param.getAppId());
                    List roleList = new ArrayList();
                    roleList.add(excelImportTeamUserHead.getRoleId());
                    userTenantRoleParam.setRoleList(roleList);
                    userTenantRoleService.addUserTenantRole(userTenantRoleParam);
                    //导入用户群体
                    if (StringUtils.isNotBlank(excelImportTeamUserHead.getChannelName())) {
                        UserChannel userChannel = mdUserChannelService.getChannelByName(param.getAppId(), param.getTenantId(), excelImportTeamUserHead.getChannelName());
                        if (userChannel != null) {
                            UserChannelRelatedParam userChannelRelatedParam = new UserChannelRelatedParam();
                            userChannelRelatedParam.setAppId(param.getAppId());
                            userChannelRelatedParam.setChannelId(userChannel.getId().longValue());
                            userChannelRelatedParam.setTenantId(param.getTenantId());
                            List<UserChannelRelated> listRelated = new ArrayList<>();
                            UserChannelRelated userChannelRelated = new UserChannelRelated();
                            userChannelRelated.setAppId(param.getAppId());
                            userChannelRelated.setChannelId(userChannel.getId().longValue());
                            userChannelRelated.setUserId(passwordVO.getUserId());
                            userChannelRelated.setTenantId(param.getTenantId());
                            listRelated.add(userChannelRelated);
                            userChannelRelatedParam.setList(listRelated);
                            mdUserChannelService.batchRelatedUser(userChannelRelatedParam);
                        }

                    }
                }
                MdUser mdUser = new MdUser();
                mdUser.setId(passwordVO.getUserId());
                listUser.add(mdUser);
            }
        }
        return listUser;
    }

    @Override
    public Map<String, Object> getUserOrgAndRole(Long userId, Long tenantId, String appId, Long orgId) {
        Map<String, Object> map = new HashMap<>();
        List<String> orgLists = new ArrayList<>();
        List<RoleVO> roleVOList = userTenantRoleMapper.getRoleListByUserId(appId, tenantId, userId);
        List<Org> orgList = orgMapper.selectList(new QueryWrapper<Org>().eq("id", orgId));
        List<String> roleNames = roleVOList.stream().map(RoleVO::getName).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(orgList)) {
            List<OrgVO> pathList = new ArrayList<>();
            pathList = selectOrgPath(tenantId, orgList.get(0).getId(), pathList);
            List<String> pathNameList = pathList.stream().map(OrgVO::getName).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(pathNameList)) {
                Collections.reverse(pathNameList);
            }
            String orgPath = String.join("/", pathNameList);
            orgLists.add(orgPath);
        }

        Iterator<String> iterator = roleNames.iterator();
        while (iterator.hasNext()) {
            String roleName = iterator.next();
            if (roleName.contains("一级") || roleName.contains("管理员")) {
                iterator.remove();
            }
        }
        List<UserChannel> userChannelList = mdUserChannelService.getChannelList(appId, tenantId, null);
        map.put("userRole", roleNames);
        map.put("orgLists", orgLists);
        map.put("userChannelList", userChannelList);
        return map;
    }

    @Override
    public UserVO getUserById(Long id) {
        MdUser mdUser = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getId, id));
        UserVO userVO = new UserVO();
        if (ObjectUtils.isNotEmpty(mdUser)) {
            userVO.setNickname(mdUser.getNickname());
            userVO.setPhone(mdUser.getPhone());
            userVO.setAccount(mdUser.getAccount());
            userVO.setEmail(mdUser.getEmail());
            userVO.setId(mdUser.getId());
        }
        return userVO;
    }

    @Override
    public MdUser addOutUserByAccount(OrgUserParam orgUserParam) throws Exception {
        if (CollectionUtils.isEmpty(orgUserParam.getOrgIds())) {
            throw new IllegalParamException("用户组织不能为空！");
        }
        if (CollectionUtils.isEmpty(orgUserParam.getRoleList())) {
            throw new IllegalParamException("用户角色不能为空！");
        }
        MdUser mdUser = mdUserMapper.selectOne(Wrappers.<MdUser>lambdaQuery().eq(MdUser::getAccount, orgUserParam.getAccount()));
        if (mdUser != null) {
            orgUserParam.setId(mdUser.getId());
            UserTenantRoleParam roleUserTenantParam = new UserTenantRoleParam();
            roleUserTenantParam.setUserId(mdUser.getId());
            roleUserTenantParam.setTenantId(orgUserParam.getTenantId());
            roleUserTenantParam.setAppId(orgUserParam.getAppId());
            roleUserTenantParam.setRoleList(orgUserParam.getRoleList().stream().collect(Collectors.toList()));
            //新增用户角色
            Set<String> userRoles = userTenantRoleService.getRoleNames(orgUserParam.getAppId(), orgUserParam.getTenantId(), mdUser.getId());
            if (CollectionUtils.isEmpty(userRoles)) {
                userTenantRoleService.addUserTenantRole(roleUserTenantParam);
                mdUserOrgService.addOrgUser(orgUserParam);
            }

            return mdUser;
        } else {
            throw new IllegalParamException("该系统中没有此用户！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdUser outUserAdd(OutUserAddParam param) throws Exception {
        if (null == param || null == param.getOrgUserParam() || null == param.getOrgUserChannelRelated()) {
            throw new IllegalParamException("参数不能为空！");
        }
        MdUser mdUser = addOutUserByAccount(param.getOrgUserParam());
//        List<UserChannelRelated> userChannelRelatedList = userChannelRelatedMapper.selectList(new QueryWrapper<UserChannelRelated>()
//                .eq("app_id", param.getOrgUserParam().getAppId())
//                .eq("user_id",mdUser.getId())
//                .eq("tenant_id", param.getOrgUserParam().getTenantId()));
//        if (org.springframework.util.CollectionUtils.isEmpty(userChannelRelatedList)) {
//        }
        mdUserChannelService.relatedUserChannel(param.getOrgUserChannelRelated());

        return mdUser;
    }

    @Override
    public List<Map<String, Object>> getDeptLeaderByUserEmail(String email, Long tenantId) {
        if (StringUtils.isBlank(email)) {
            throw new IllegalParamException("邮箱不能为空！");
        }
        List<Map<String, Object>> mdUsers = new ArrayList<>();
        List<MdCompanyUsers> companyUsers = mdCompanyUserMapper.getDeptLeaderByUserEmail(email);
        if (!CollectionUtils.isEmpty(companyUsers)) {
            for (MdCompanyUsers mdCompanyUsers : companyUsers) {
                MdUser mdUser = mdUserMapper.searchByEmail(mdCompanyUsers.getEmail());
                if (mdUser != null) {
                    Map<String, Object> map = new HashMap<>();
                    List<OrgNewVO> orgNewVOS = mdUserOrgMapper.getOrgInfo(mdUser.getId(), tenantId);
                    map.put("id", mdUser.getId());
                    map.put("phone", mdUser.getPhone());
                    map.put("nickname", mdUser.getNickname());
                    map.put("email", mdUser.getEmail());
                    map.put("account", mdUser.getAccount());
                    map.put("orgList", orgNewVOS);
                    mdUsers.add(map);
                }
            }
        }
        return mdUsers;
    }

    @Override
    public Long addHumanResourceOrg(OrgUserParam orgUserParam) {
        log.info("自注册添加组织参数{}", JSON.toJSON(orgUserParam));
        MdCompanyUsers mdCompanyUsers = null;
        if (StringUtils.isNotEmpty(orgUserParam.getEmail())) {
            mdCompanyUsers = mdCompanyUserMapper.selectOne(new QueryWrapper<MdCompanyUsers>().eq("email", orgUserParam.getEmail()).eq("is_on_job", 1).last("limit 1"));
        } else {
            mdCompanyUsers = mdCompanyUserMapper.selectOne(new QueryWrapper<MdCompanyUsers>().eq("employee_id", orgUserParam.getEmployeeId()).eq("is_on_job", 1).last("limit 1"));
        }
        Long orgId = null;
        if (mdCompanyUsers != null) {
            if (!org.springframework.util.StringUtils.isEmpty(mdCompanyUsers.getCompanyName())) {
                Org companyOrg = orgMapper.selectOne(new QueryWrapper<Org>().eq("pid", orgUserParam.getPid()).eq("tenant_id", orgUserParam.getTenantId()).eq("name", mdCompanyUsers.getCompanyName()).last("limit 1"));
                if (companyOrg == null) {
                    OrgParam orgParam = new OrgParam();
                    orgParam.setCustomId(null);
                    orgParam.setTenantId(orgUserParam.getTenantId());
                    orgParam.setPid(orgUserParam.getPid());
                    orgParam.setName(mdCompanyUsers.getCompanyName());
                    OrgVO orgVO = orgService.add(orgParam);
                    orgId = orgVO.getId();
                } else {
                    orgId = companyOrg.getId();
                }
            }

            if (!org.springframework.util.StringUtils.isEmpty(mdCompanyUsers.getDeptName())) {
                // 查找或者创建部门组织
                Org deptOrg = orgMapper.selectOne(new QueryWrapper<Org>().eq("tenant_id", orgUserParam.getTenantId()).eq("pid", orgId).eq("name", mdCompanyUsers.getDeptName()));
                if (deptOrg == null) {
                    OrgParam orgParam = new OrgParam();
                    orgParam.setCustomId(null);
                    orgParam.setTenantId(orgUserParam.getTenantId());
                    orgParam.setPid(orgId);
                    orgParam.setName(mdCompanyUsers.getDeptName());
                    OrgVO orgVO = orgService.add(orgParam);
                    orgId = orgVO.getId();
                } else {
                    orgId = deptOrg.getId();
                }
            }
            if (!org.springframework.util.StringUtils.isEmpty(mdCompanyUsers.getOfficeName())) {
                // 查找或者创建办公室组织
                Org officeOrg = orgMapper.selectOne(new QueryWrapper<Org>().eq("tenant_id", orgUserParam.getTenantId()).eq("pid", orgId).eq("name", mdCompanyUsers.getOfficeName()));
                if (officeOrg == null) {
                    OrgParam orgParam = new OrgParam();
                    orgParam.setCustomId(null);
                    orgParam.setTenantId(orgUserParam.getTenantId());
                    orgParam.setPid(orgId);
                    orgParam.setName(mdCompanyUsers.getOfficeName());
                    OrgVO orgVO = orgService.add(orgParam);
                    orgId = orgVO.getId();
                } else {
                    orgId = officeOrg.getId();
                }
            }


        }
        return orgId;
    }

    @Override
    public Object exportUserLog(ExportUserParam param) {
        MdUserOperationLog mdUserOperationLog = new MdUserOperationLog();
        mdUserOperationLog.setOperateUserId(param.getOperateUserId());
        mdUserOperationLog.setOperateUserName(param.getOperateUserName());
        mdUserOperationLog.setOperationType(UserOperationType.EXPORT.getCode());
        mdUserOperationLog.setAppId(param.getAppId());
        mdUserOperationLog.setTenantId(param.getTenantId());
        mdUserOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdUserOperationLog.setCreateTime(new Date());
        mdUserOperationLog.setUserIp(param.getUserIp());
        mdUserOperationLog.setNotes(JSON.toJSONString(param.getUserIds()));
        mdUserOperationLogService.addUserOperationLog(mdUserOperationLog);
        return null;
    }

    public OrgMsgResult buildChildPaths(String parentPath, Long parentId, List<String> list, long childOrgId) {
        OrgMsgResult result = new OrgMsgResult();
        if (list.isEmpty()) {
            // 基础情况：如果列表为空，返回null或默认结果
            return result; // 假设这里返回的是一个空的Result对象
        }

        String strName = list.get(0);
        OrgVO orgVO = orgMapper.getParentOrg(545L, parentId, strName);
        if (orgVO == null) {
            // 未找到匹配项，设置相应的msg并返回
            result.setMsg("Name not found: " + strName);
            result.setIsMatch(false);
            return result;
        }

        String childPath = parentPath + "/" + orgVO.getName();
        childOrgId = orgVO.getId();

        // 递归调用，处理剩余列表部分
        OrgMsgResult childResult = buildChildPaths(childPath, childOrgId, list.subList(1, list.size()), childOrgId);
        if (childResult.getChildOrgId() != 0) { // 假设0表示childOrgId未设置
            result.setChildOrgId(childResult.getChildOrgId());
        }
        result.setIsMatch(true);

        return result;
    }

    public void setUserTenantRoleMapper(UserTenantRoleMapper userTenantRoleMapper) {
        this.userTenantRoleMapper = userTenantRoleMapper;
    }

    private boolean isValidPhone(String phone) {
        // 一个常见的中国大陆手机号正则匹配 (1开头，11位数字)
        String regex = "^1[1-9]\\d{9}$";
        return phone != null && phone.matches(regex);
    }
}
