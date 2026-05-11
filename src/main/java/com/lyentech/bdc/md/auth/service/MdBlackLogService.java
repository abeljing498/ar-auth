package com.lyentech.bdc.md.auth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.model.entity.MdBlackLog;
import com.lyentech.bdc.md.auth.model.param.BlackLogParam;
import com.lyentech.bdc.md.auth.model.vo.MdBlackLogVO;
import com.lyentech.bdc.md.auth.service.impl.MdBlackLogServiceImpl;
import com.lyentech.bdc.md.auth.service.impl.MdBlackUserServiceImpl;

/**
 * @author YuYi
 * @create 2023/4/12
 * @create 16:48
 */
public interface MdBlackLogService extends IService<MdBlackLog> {

    PageResult<MdBlackLogVO> getBlackList(BlackLogParam blackLogParam);
}
