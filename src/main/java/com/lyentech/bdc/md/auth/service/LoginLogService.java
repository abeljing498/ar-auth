package com.lyentech.bdc.md.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.param.LoginLogParam;
import com.lyentech.bdc.md.auth.model.vo.LoginLogVO;
import org.springframework.stereotype.Service;

/**
 * @author 260583
 */
public interface LoginLogService extends IService<MdLoginLog> {
    /**
     * 获取登录日志
     * @param
     * @return
     */
    PageResult<LoginLogVO> getLoginLog(Long pageNum, Long pageSize,String beginTime, String endTime, String user, String loginWay, String appkey);
}
