package com.lyentech.bdc.md.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.entity.MdTenantLoginLog;
import com.lyentech.bdc.md.auth.model.vo.TenantLoginLogVO;

/**
 * @author 260583
 */
public interface TenantLoginLogService extends IService<MdTenantLoginLog> {
    /**
     * 获取登录日志
     * @param
     * @param tenantId
     * @return
     */
    PageResult<TenantLoginLogVO> getLoginLog(Long pageNum, Long pageSize, String beginTime, String endTime, String user, String loginWay, String appkey, Long tenantId);
}
