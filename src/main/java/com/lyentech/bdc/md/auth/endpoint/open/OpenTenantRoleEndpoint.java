package com.lyentech.bdc.md.auth.endpoint.open;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.common.exception.MdAppAuthorizationException;
import com.lyentech.bdc.md.auth.dao.TenantRoleMapper;
import com.lyentech.bdc.md.auth.model.entity.TenantRole;
import com.lyentech.bdc.md.auth.model.param.RoleParam;
import com.lyentech.bdc.md.auth.model.param.TenantRoleParam;
import com.lyentech.bdc.md.auth.model.vo.MdRoleVO;
import com.lyentech.bdc.md.auth.model.vo.PushMessageDto;
import com.lyentech.bdc.md.auth.model.vo.RoleDetailVO;
import com.lyentech.bdc.md.auth.model.vo.RoleVO;
import com.lyentech.bdc.md.auth.service.MdAppService;
import com.lyentech.bdc.md.auth.service.MdRoleService;
import com.lyentech.bdc.md.auth.service.SendMessageToWebSocketService;
import com.lyentech.bdc.md.auth.service.TenantRoleService;

import com.lyentech.bdc.md.auth.util.SignUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static com.lyentech.bdc.md.auth.common.constant.MdParamConstant.APP_KEY_PARAM;
import static com.lyentech.bdc.md.auth.common.constant.MdParamConstant.KEY_SIGN_PARAM;

/**
 * @Author :yan
 * @Date :Create in 2022/9/19
 * @Description : 租户角色开放接口
 */

@RestController
@RequestMapping("/open/tenantRole")
public class OpenTenantRoleEndpoint {
    @Autowired
    private MdAppService appService;
    @Autowired
    private MdRoleService roleService;
    @Autowired
    private TenantRoleService tenantRoleService;
    @Autowired
    private SendMessageToWebSocketService sendMessageToWebSocketService;
    @Resource
    private TenantRoleMapper tenantRoleMapper;

    /**
     * 在指定的app和租户下添加角色
     *
     * @param body
     * @param param
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/add")
    public ResultEntity add(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        //验证签名
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            TenantRoleParam tenantRoleParam = JSONObject.parseObject(body, TenantRoleParam.class);
            if (StringUtils.isEmpty(tenantRoleParam.getName()) || tenantRoleParam.getTenantId() == null) {
                throw new IllegalParamException("角色名和租户id不能为空");
            }
            RoleParam roleParam = new RoleParam();
            BeanUtils.copyProperties(tenantRoleParam, roleParam);
            roleParam.setAppId(appKey);
            Long id = roleService.add(roleParam);
            PushMessageDto pushMessageDto = new PushMessageDto();
            pushMessageDto.setRoleId(Long.toString(id));
            pushMessageDto.setTenantId(String.valueOf(roleParam.getTenantId()));
            pushMessageDto.setStatus("ADD");
            pushMessageDto.setAppKey(roleParam.getAppId());
            sendMessageToWebSocketService.sendMessage(pushMessageDto);
            return ResultEntity.success();

        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    /**
     * 修改角色信息
     *
     * @param body
     * @param param
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/update")
    public ResultEntity update(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            TenantRoleParam tenantRoleParam = JSONObject.parseObject(body, TenantRoleParam.class);
            if (tenantRoleParam.getId() == null && tenantRoleParam.getTenantId() == null) {
                throw new IllegalParamException("角色id和租户id不能为空");
            }
            RoleParam roleParam = new RoleParam();
            BeanUtils.copyProperties(tenantRoleParam, roleParam);
            roleParam.setAppId(appKey);
            roleService.update(roleParam);
            TenantRole tenantRole = tenantRoleMapper.selectOne(Wrappers.<TenantRole>lambdaQuery().eq(TenantRole::getRoleId, roleParam.getId()));
            if (tenantRole != null) {
                PushMessageDto pushMessageDto = new PushMessageDto();
                pushMessageDto.setRoleId(String.valueOf(roleParam.getId()));
                pushMessageDto.setTenantId(String.valueOf(tenantRole.getTenantId()));
                pushMessageDto.setStatus("ADD");
                pushMessageDto.setAppKey(tenantRole.getAppId());
                sendMessageToWebSocketService.sendMessage(pushMessageDto);
            }

            return ResultEntity.success();
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    /**
     * 删除角色
     *
     * @param body
     * @param param
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/delete")
    public ResultEntity delete(@RequestBody String body, @RequestParam Map<String, String> param) throws Exception {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            TenantRoleParam tenantRoleParam = JSONObject.parseObject(body, TenantRoleParam.class);
            if (tenantRoleParam.getId() == null) {
                throw new IllegalParamException("角色id不能为空");
            }
            roleService.delete(tenantRoleParam.getId());
            return ResultEntity.success();
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    /**
     * 分页获取指定app和租户下的角色列表
     *
     * @param body
     * @param param
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/getPage")
    public ResultEntity getPage(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            TenantRoleParam tenantRoleParam = JSONObject.parseObject(body, TenantRoleParam.class);
            if (tenantRoleParam.getTenantId() == null) {
                throw new IllegalParamException("租户id不能为空");
            }

            PageResult<RoleVO> page = tenantRoleService.selectRoleList(appKey,
                    tenantRoleParam.getTenantId(),
                    tenantRoleParam.getPageNum(),
                    tenantRoleParam.getPageSize(),
                    tenantRoleParam.getName());
            return ResultEntity.success(page);
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/getList")
    public ResultEntity getList(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            TenantRoleParam tenantRoleParam = JSONObject.parseObject(body, TenantRoleParam.class);
            if (tenantRoleParam.getTenantId() == null) {
                throw new IllegalParamException("租户id不能为空");
            }

            List<MdRoleVO> roleList =
                    tenantRoleService.getRoleList(tenantRoleParam.getTenantId(), appKey);
            return ResultEntity.success(roleList);
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    /**
     * 获取角色详情
     *
     * @param param
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/getDetail")
    public ResultEntity getDetail(@RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, null, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            String id = param.get("id");
            if (StringUtils.isEmpty(id)) {
                throw new IllegalParamException("角色id不能为空");
            }
            RoleDetailVO roleDetail = roleService.getRoleDetail(Long.valueOf(id));
            return ResultEntity.success(roleDetail);
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/switchStatus")
    public ResultEntity switchStatus(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            TenantRoleParam tenantRoleParam = JSONObject.parseObject(body, TenantRoleParam.class);
            if (tenantRoleParam.getId() == null && tenantRoleParam.getStatus() == null) {
                throw new IllegalParamException("角色id和状态不能为空");
            }
            RoleParam roleParam = new RoleParam();
            BeanUtils.copyProperties(tenantRoleParam, roleParam);
            roleParam.setAppId(appKey);
            roleService.changeStatus(tenantRoleParam.getId(), tenantRoleParam.getStatus());
            return ResultEntity.success();
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }
}
