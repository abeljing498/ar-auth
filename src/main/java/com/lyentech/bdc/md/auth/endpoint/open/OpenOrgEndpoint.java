package com.lyentech.bdc.md.auth.endpoint.open;

import com.alibaba.fastjson.JSONObject;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.common.exception.MdAppAuthorizationException;
import com.lyentech.bdc.md.auth.model.param.OrgParam;
import com.lyentech.bdc.md.auth.model.param.QueryOrgByCidParam;
import com.lyentech.bdc.md.auth.model.param.QueryOrgNameParam;
import com.lyentech.bdc.md.auth.model.vo.OrgHeightNameVO;
import com.lyentech.bdc.md.auth.model.vo.OrgTree;
import com.lyentech.bdc.md.auth.model.vo.OrgVO;
import com.lyentech.bdc.md.auth.service.MdAppService;
import com.lyentech.bdc.md.auth.service.OrgService;
import com.lyentech.bdc.md.auth.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static com.lyentech.bdc.md.auth.common.constant.MdParamConstant.APP_KEY_PARAM;
import static com.lyentech.bdc.md.auth.common.constant.MdParamConstant.KEY_SIGN_PARAM;

/**
 * @Author :yan
 * @Date :Create in 2022/9/19
 * @Description : 组织模块开放接口
 */

@RestController
@RequestMapping("/open/org")
public class OpenOrgEndpoint {

    @Autowired
    private MdAppService appService;
    @Autowired
    private OrgService orgService;

    @PostMapping("/add")
    public ResultEntity add(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            OrgParam orgParam = JSONObject.parseObject(body, OrgParam.class);
            if (StringUtils.isEmpty(orgParam.getName()) || orgParam.getPid() == null || orgParam.getTenantId() == null) {
                throw new IllegalParamException("组织名、父组织id和租户id不能为空");
            }
            if (ObjectUtils.isEmpty(orgParam.getType()) || orgParam.getType()==0) {
                orgService.add(orgParam);
                return ResultEntity.success();
            } else {
                return ResultEntity.success(orgService.add(orgParam));
            }
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/update")
    public ResultEntity update(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            OrgParam orgParam = JSONObject.parseObject(body, OrgParam.class);
            if ( orgParam.getId() == null || orgParam.getTenantId() == null) {
                throw new IllegalParamException("组织id、租户id不能为空");
            }
            orgService.update(orgParam);
            return ResultEntity.success();
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/delete")
    public ResultEntity delete(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            OrgParam orgParam = JSONObject.parseObject(body, OrgParam.class);
            if ( orgParam.getId() == null) {
                throw new IllegalParamException("组织id不能为空");
            }
            orgService.delete(orgParam.getId());
            return ResultEntity.success();
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/getTree")
    public ResultEntity getTree(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            OrgParam orgParam = JSONObject.parseObject(body, OrgParam.class);
            if (orgParam.getTenantId() == null) {
                throw new IllegalParamException("组织id、租户id不能为空");
            }
            List<OrgTree> tree = orgService.getTreeByTenantId(orgParam.getTenantId());
            return ResultEntity.success(tree);
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

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
                throw new IllegalParamException("组织id不能为空");
            }
            OrgVO detail = orgService.getDetail(Long.valueOf(id));
            return ResultEntity.success(detail);
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/getSubById")
    public ResultEntity getSubById(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            OrgParam orgParam = JSONObject.parseObject(body, OrgParam.class);
            if (orgParam.getPageNum() == null || orgParam.getPageSize() == null) {
                throw new IllegalParamException("页数和页码不能为空");
            }
            PageResult pageResult = orgService.getSubById(orgParam.getPageNum(), orgParam.getPageSize(), orgParam.getId(), orgParam.getTenantId());
            return ResultEntity.success(pageResult);
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/getOrgName")
    public ResultEntity getOrgName(@RequestBody String body, @RequestParam Map<String,String> param ) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            QueryOrgNameParam nameParam = JSONObject.parseObject(body, QueryOrgNameParam.class);
            if ((nameParam.getHeight() == null) && (ObjectUtils.isEmpty(nameParam.getIds()))) {
                throw new IllegalParamException("组织id和层级高度不能为空");
            }
            OrgHeightNameVO nameVO = orgService.getOrgName(nameParam);
            return ResultEntity.success(nameVO);
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }

    @PostMapping("/getOrgByCid")
    public ResultEntity getOrgByCid(@RequestBody String body, @RequestParam Map<String, String> param) throws UnsupportedEncodingException {
        String appKey = param.get(APP_KEY_PARAM);
        if (StringUtils.isEmpty(appKey)) {
            throw new IllegalParamException("appKey不能为空");
        }
        String secret = appService.getSecret(appKey);
        String sign = SignUtil.sign(param, body, secret);
        if (sign.equals(param.get(KEY_SIGN_PARAM))) {
            QueryOrgByCidParam orgByCidParam = JSONObject.parseObject(body,QueryOrgByCidParam.class);

            if (orgByCidParam.getCustomId() == null || orgByCidParam.getTenantId() == null ) {
                throw new IllegalParamException("用户Id和租户Id能为空");
            }
            OrgVO org = orgService.getOrgByCid(orgByCidParam.getCustomId(),orgByCidParam.getTenantId());
            return ResultEntity.success(org);
        } else {
            throw new MdAppAuthorizationException("签名失败");
        }
    }


}
