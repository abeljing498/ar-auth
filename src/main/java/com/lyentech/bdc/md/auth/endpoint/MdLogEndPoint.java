package com.lyentech.bdc.md.auth.endpoint;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.dao.MdTenantLoginLogMapper;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.entity.MdTenantLoginLog;
import com.lyentech.bdc.md.auth.model.param.BlackLogParam;
import com.lyentech.bdc.md.auth.model.param.LoginLogParam;
import com.lyentech.bdc.md.auth.model.param.MdTenantLoginLogParam;
import com.lyentech.bdc.md.auth.model.param.RoleParam;
import com.lyentech.bdc.md.auth.model.vo.PushMessageDto;
import com.lyentech.bdc.md.auth.service.LoginLogService;
import com.lyentech.bdc.md.auth.service.MdBlackLogService;
import com.lyentech.bdc.md.auth.service.TenantLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author 260583
 */
@RestController
@RequestMapping("log")
public class MdLogEndPoint {

    @Autowired
    LoginLogService loginLogService;
    @Autowired
    TenantLoginLogService tenantLoginLogService;
    @Autowired
    MdTenantLoginLogMapper mdTenantLoginLogMapper;
    @Autowired
    MdBlackLogService blackLogService;

    @GetMapping("/login")
    public ResultEntity getLoginLog(@RequestParam(defaultValue = "1") Long pageNum,
                                    @RequestParam(defaultValue = "10") Long pageSize,
                                    @RequestParam(required = false) String beginTime,
                                    @RequestParam(required = false) String endTime,
                                    @RequestParam(required = false) String user,
                                    @RequestParam(required = false) String loginWay,
                                    @RequestParam String appkey) throws UnsupportedEncodingException {
        beginTime = !beginTime.isEmpty() ? URLDecoder.decode(beginTime, "UTF-8") : beginTime;
        endTime = !endTime.isEmpty() ? URLDecoder.decode(endTime, "UTF-8") : endTime;
        user = !user.isEmpty() ? URLDecoder.decode(user, "UTF-8") : user;
        loginWay = !loginWay.isEmpty() ? URLDecoder.decode(loginWay, "UTF-8") : loginWay;
        appkey = !appkey.isEmpty() ? URLDecoder.decode(appkey, "UTF-8") : appkey;
        return ResultEntity.success(loginLogService.getLoginLog(pageNum, pageSize, beginTime, endTime, user, loginWay, appkey));
    }

    @GetMapping("/tenantLogin")
    public ResultEntity tenantLogin(@RequestParam(defaultValue = "1") Long pageNum,
                                    @RequestParam(defaultValue = "10") Long pageSize,
                                    @RequestParam(required = false) String beginTime,
                                    @RequestParam(required = false) String endTime,
                                    @RequestParam(required = false) String user,
                                    @RequestParam(required = false) String loginWay,
                                    @RequestParam String appKey,
                                    @RequestParam Long tenantId) throws UnsupportedEncodingException {
        beginTime = !beginTime.isEmpty() ? URLDecoder.decode(beginTime, "UTF-8") : beginTime;
        endTime = !endTime.isEmpty() ? URLDecoder.decode(endTime, "UTF-8") : endTime;
        user = !user.isEmpty() ? URLDecoder.decode(user, "UTF-8") : user;
        loginWay = !loginWay.isEmpty() ? URLDecoder.decode(loginWay, "UTF-8") : loginWay;
        appKey = !appKey.isEmpty() ? URLDecoder.decode(appKey, "UTF-8") : appKey;
        return ResultEntity.success(tenantLoginLogService.getLoginLog(pageNum, pageSize, beginTime, endTime, user, loginWay, appKey, tenantId));
    }

    @GetMapping("/black")
    public ResultEntity getBlackLog(@RequestParam(defaultValue = "1") Long pageNum,
                                    @RequestParam(defaultValue = "10") Long pageSize,
                                    @RequestParam(required = false) String beginTime,
                                    @RequestParam(required = false) String endTime,
                                    @RequestParam(required = false) String type,
                                    @RequestParam Long userId,
                                    @RequestParam String appkey) throws UnsupportedEncodingException {
        beginTime = !beginTime.isEmpty() ? URLDecoder.decode(beginTime, "UTF-8") : beginTime;
        endTime = !endTime.isEmpty() ? URLDecoder.decode(endTime, "UTF-8") : endTime;
        type = !type.isEmpty() ? URLDecoder.decode(type, "UTF-8") : type;
        appkey = !appkey.isEmpty() ? URLDecoder.decode(appkey, "UTF-8") : appkey;
        BlackLogParam blackLogParam = new BlackLogParam();
        blackLogParam.setPageNum(pageNum);
        blackLogParam.setPageSize(pageSize);
        blackLogParam.setUserId(userId);
        blackLogParam.setAppId(appkey);
        blackLogParam.setType(type);
        blackLogParam.setBeginTime(beginTime);
        blackLogParam.setEndTime(endTime);
        return ResultEntity.success(blackLogService.getBlackList(blackLogParam));
    }

    @PostMapping("/addTenantLoginLog")
    public ResultEntity addTenantLoginLog(@RequestBody MdTenantLoginLogParam loginLogParam) {
        if (StringUtils.isEmpty(loginLogParam.getLoginWay())) {
            return ResultEntity.success();
        }

        if (StringUtils.isEmpty(loginLogParam.getAppKey())) {
            throw new IllegalParamException("系统Id不能为空！");
        }
        if (StringUtils.isEmpty(loginLogParam.getTenantId())) {
            throw new IllegalParamException("租户Id不能为空！");
        }
        if (StringUtils.isEmpty(loginLogParam.getTenantName())) {
            throw new IllegalParamException("租户名称不能为空！");
        }
        if (StringUtils.isEmpty(loginLogParam.getUserId())) {
            throw new IllegalParamException("用户ID不能为空！");
        }
        MdLoginLog loginLog = loginLogService.getOne(new QueryWrapper<MdLoginLog>().eq("app_id", loginLogParam.getAppKey()).eq("user_id", loginLogParam.getUserId()).orderByDesc("create_time").last("limit 1"));
        if (loginLog != null) {
            MdTenantLoginLog mdTenantLoginLog = new MdTenantLoginLog();
            mdTenantLoginLog.setAppId(loginLogParam.getAppKey());
            mdTenantLoginLog.setTenantId(loginLogParam.getTenantId());
            mdTenantLoginLog.setTenantName(loginLogParam.getTenantName());
            mdTenantLoginLog.setUserId(loginLogParam.getUserId());
            mdTenantLoginLog.setBrowser(loginLogParam.getBrowser());
            mdTenantLoginLog.setOperateSystem(loginLogParam.getOperateSystem());
            mdTenantLoginLog.setIp(loginLog.getIp());
            mdTenantLoginLog.setLoginWay(loginLog.getLoginWay());
            mdTenantLoginLogMapper.insert(mdTenantLoginLog);
        }
        return ResultEntity.success();
    }
}
