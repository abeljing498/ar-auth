package com.lyentech.bdc.md.auth.endpoint;

import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.model.param.BlackUserListParam;
import com.lyentech.bdc.md.auth.model.param.BlackUserParam;
import com.lyentech.bdc.md.auth.model.param.JoinBlackListParam;
import com.lyentech.bdc.md.auth.service.MdBlackUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author YuYi
 * @create 2023/4/12
 * @create 10:37
 * 黑名单
 */
@RestController
@RequestMapping("/black")
public class MdBlackUserEndpoint {

    @Resource
    MdBlackUserService blackUserService;

    @PostMapping("/join")
    public ResultEntity joinBlack(@RequestBody BlackUserParam blackUserParam) {
        blackUserService.joinBlack(blackUserParam);
        return ResultEntity.success();
    }

    @PostMapping("/joinBatch")
    public ResultEntity joinBatch(@RequestBody JoinBlackListParam blackListParam) {
        blackUserService.listJoinUser(blackListParam);
        return ResultEntity.success();
    }

    @PostMapping("/remove")
    public ResultEntity removeBlack(@RequestBody BlackUserParam blackUserParam) {
        blackUserService.removeBlack(blackUserParam);
        return ResultEntity.success();
    }

    @PostMapping("/update")
    public ResultEntity updateBlack(@RequestBody BlackUserParam blackUserParam) {
        blackUserService.updateBlack(blackUserParam);
        return ResultEntity.success();
    }

    @GetMapping("/listExistUsers")
    public ResultEntity listExistUsers(@RequestParam String appId) {

        return ResultEntity.success(blackUserService.listExistUser(appId));
    }

    @GetMapping("/autoJoinBlackByNP")
    public ResultEntity authJoinBlackByNP(@RequestParam String userId,
                                          @RequestParam String appId,
                                          @RequestParam Long oid,
                                          @RequestParam String reason) throws UnsupportedEncodingException {
        reason = !reason.isEmpty() ? URLDecoder.decode(reason, "UTF-8") : reason;
        BlackUserParam blackUserParam = new BlackUserParam();
        blackUserParam.setAppId(appId);
        blackUserParam.setOid(oid);
        blackUserParam.setReason(reason);
        blackUserParam.setUserId(Long.valueOf(userId));
        blackUserService.joinBlack(blackUserParam);
        return ResultEntity.success();
    }

    @GetMapping("/getUserList")
    public ResultEntity getUserList(@RequestParam(defaultValue = "1") Long pageNum,
                                    @RequestParam(defaultValue = "10") Long pageSize,
                                    @RequestParam(required = true) String appId,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String beginTime,
                                    @RequestParam(required = false) String endTime,
                                    @RequestParam(required = false) String reason) throws UnsupportedEncodingException {
        appId = !appId.isEmpty() ? URLDecoder.decode(appId, "UTF-8") : appId;
        keyword = !keyword.isEmpty() ? URLDecoder.decode(keyword, "UTF-8") : keyword;
        beginTime = !beginTime.isEmpty() ? URLDecoder.decode(beginTime, "UTF-8") : beginTime;
        endTime = !endTime.isEmpty() ? URLDecoder.decode(endTime, "UTF-8") : endTime;
        reason = !reason.isEmpty() ? URLDecoder.decode(reason, "UTF-8") : reason;
        return ResultEntity.success(blackUserService.getUserList(pageNum, pageSize, appId, keyword, beginTime, endTime, reason));
    }

    @GetMapping("/searchUser")
    public ResultEntity searchUser(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String appId
    ) throws UnsupportedEncodingException {
        keyword = !keyword.isEmpty() ? URLDecoder.decode(keyword, "UTF-8") : keyword;
        appId = !appId.isEmpty() ? URLDecoder.decode(appId, "UTF-8") : appId;
        return ResultEntity.success(blackUserService.searchUser(keyword, appId));
    }

}
