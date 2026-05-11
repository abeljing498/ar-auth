package com.lyentech.bdc.md.auth.endpoint;

import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.http.response.ResultEntity;
import com.lyentech.bdc.md.auth.model.entity.UserChannel;
import com.lyentech.bdc.md.auth.model.param.AddUserChannelRelatedParam;
import com.lyentech.bdc.md.auth.model.param.UserChannelGroupParam;
import com.lyentech.bdc.md.auth.model.param.UserChannelParam;
import com.lyentech.bdc.md.auth.model.param.UserChannelRelatedParam;
import com.lyentech.bdc.md.auth.model.vo.UserChannelRelatedVO;
import com.lyentech.bdc.md.auth.service.MdUserChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/userChannel")
public class MdUserChannelEndpoint {
    @Autowired
    private MdUserChannelService mdUserChannelService;

    @GetMapping("/getChannelListByAppId")
    public ResultEntity getChannelListByAppId(@RequestParam String appId, @RequestParam Long tenantId) {
        return ResultEntity.success(mdUserChannelService.getChannelListByAppId(appId, tenantId));
    }

    @GetMapping("/getChannelList")
    public ResultEntity getChannelList(@RequestParam String appId, @RequestParam Long tenantId,@RequestParam(required = false) String name) throws UnsupportedEncodingException {
        Map<String, Object> map = new HashMap<>();
        List<UserChannel> userChannelList = mdUserChannelService.getChannelList(appId, tenantId,name);
        map.put("appChannelList", userChannelList);
        return ResultEntity.success(map);
    }
    @GetMapping("/getChannelListByUserChannel")
    public ResultEntity getChannelListByUserChannel(@RequestParam String appId, @RequestParam Long tenantId,@RequestParam(required = false) String name,Long userId) throws UnsupportedEncodingException {
        Map<String, Object> map = new HashMap<>();
        List<UserChannel> userChannelList = mdUserChannelService.getChannelListByUserChannel(appId, tenantId,name,userId);
        map.put("appChannelList", userChannelList);
        return ResultEntity.success(map);
    }
    @GetMapping("/getChannelListByUser")
    public ResultEntity getChannelListByUser(@RequestParam String appId, @RequestParam Long tenantId, @RequestParam Long userId) {
        return ResultEntity.success(mdUserChannelService.getChannelListByUser(appId, tenantId, userId));
    }
    @GetMapping("/getChannelByName")
    public ResultEntity getChannelListByUser(@RequestParam String appId, @RequestParam Long tenantId, @RequestParam String channel) throws Exception {
        appId= URLDecoder.decode(appId, "UTF-8");
        channel= URLDecoder.decode(channel, "UTF-8");
        UserChannel userChannel=  mdUserChannelService.getChannelByName(appId, tenantId, channel);
        return ResultEntity.success(userChannel);
    }

    @PostMapping("/updateChannelById")
    public ResultEntity updateChannelById(@RequestBody UserChannelParam param) {
        return ResultEntity.success(mdUserChannelService.updateChannelById(param));
    }

    @PostMapping("/addChannel")
    public ResultEntity addChannel(@RequestBody UserChannelParam param) {
        return ResultEntity.success(mdUserChannelService.addChannel(param));
    }

    @PostMapping("/deleteChannel")
    public ResultEntity deleteChannel(@RequestBody UserChannelParam param) {
        return ResultEntity.success(mdUserChannelService.deleteChannel(param));
    }

    @PostMapping("/addGroup")
    public ResultEntity addGroup(@RequestBody UserChannelGroupParam param) {
        return ResultEntity.success(mdUserChannelService.addGroup(param));
    }

    @PostMapping("/updateGroup")
    public ResultEntity updateGroup(@RequestBody UserChannelGroupParam param) {
        return ResultEntity.success(mdUserChannelService.updateGroup(param));
    }

    @PostMapping("/deleteGroup")
    public ResultEntity deleteGroup(@RequestBody UserChannelGroupParam param) {
        return ResultEntity.success(mdUserChannelService.deleteGroup(param));
    }

    @PostMapping("/batchRelatedUser")
    public ResultEntity batchRelatedUser(@RequestBody UserChannelRelatedParam param) {
        mdUserChannelService.batchRelatedUser(param);
        return ResultEntity.success();
    }

    @PostMapping("/relatedUserChannel")
    public ResultEntity relatedUserChannel(@RequestBody AddUserChannelRelatedParam param) throws Exception {
        mdUserChannelService.relatedUserChannel(param);
        return ResultEntity.success();
    }

    @PostMapping("/removeRelatedUser")
    public ResultEntity removeRelatedUser(@RequestBody UserChannelRelatedParam param) {
        mdUserChannelService.removeRelatedUser(param);
        return ResultEntity.success();
    }

    /**
     * 获取该渠道下所绑定的用户列表
     *
     * @param appId
     * @param groupId
     * @param channelId
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/getChannelRelatedUserList")
    public ResultEntity getRelatedUserList(@RequestParam String appId,
                                           @RequestParam(required = false) Long groupId,
                                           @RequestParam Long channelId,
                                           @RequestParam(defaultValue = "1") Long pageNum,
                                           @RequestParam(defaultValue = "10") Long pageSize,
                                           @RequestParam(required = false) String name) throws Exception {
        PageResult<UserChannelRelatedVO> pageResult = mdUserChannelService.getChannelRelatedUserList(appId, groupId, channelId, pageNum, pageSize, name);
        return ResultEntity.success(pageResult);
    }

    @GetMapping("/getChannelListByUserId")
    public ResultEntity getChannelListByUserId(@RequestParam String appId,
                                               @RequestParam(required = false) Long tenantId,
                                               @RequestParam Long userId) throws Exception {
        List<UserChannel> userChannelList=mdUserChannelService.getChannelListByUserId(appId,tenantId,userId);
        return ResultEntity.success(userChannelList);
    }

}
