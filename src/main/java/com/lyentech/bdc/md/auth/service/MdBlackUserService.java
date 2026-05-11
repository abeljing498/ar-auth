package com.lyentech.bdc.md.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.param.BlackUserParam;
import com.lyentech.bdc.md.auth.model.param.JoinBlackListParam;
import com.lyentech.bdc.md.auth.model.vo.MdBlackUserVO;
import com.lyentech.bdc.md.auth.service.impl.MdBlackUserServiceImpl;

import java.util.List;

/**
 * @author YuYi
 * @create 2023/4/12
 * @create 10:54
 */
public interface MdBlackUserService extends IService<MdBlackUser> {

    /**
     * @param blackUserParam 加入黑名单
     */
    void joinBlack(BlackUserParam blackUserParam);

    void listJoinUser(JoinBlackListParam blackListParam);

    /**
     * @param blackUserParam 移除黑名单
     */
    void removeBlack(BlackUserParam blackUserParam);

    /**
     * @param blackUserParam 更新黑名单
     */
    void updateBlack(BlackUserParam blackUserParam);

    List<Long> listExistUser(String appId);

    Boolean getIsBlack(String clientId, Long userId);

    /**
     * 获取黑名单成员列表
     *
     * @param pageNum
     * @param pageSize
     * @param appId
     * @param keyword
     * @param beginTime
     * @param endTime
     * @param reason
     * @return
     */
    PageResult<MdBlackUserVO> getUserList(Long pageNum, Long pageSize, String appId, String keyword, String beginTime, String endTime, String reason);

    MdBlackUserVO searchUser(String keyword, String appId);


}
