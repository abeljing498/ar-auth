package com.lyentech.bdc.md.auth.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.dao.MdBlackLogMapper;
import com.lyentech.bdc.md.auth.model.entity.MdBlackLog;
import com.lyentech.bdc.md.auth.model.param.BlackLogParam;
import com.lyentech.bdc.md.auth.model.vo.MdBlackLogVO;
import com.lyentech.bdc.md.auth.model.vo.MdBlackUserVO;
import com.lyentech.bdc.md.auth.service.MdBlackLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author YuYi
 * @create 2023/4/12
 * @create 16:47
 */
@Service
public class MdBlackLogServiceImpl extends ServiceImpl<MdBlackLogMapper, MdBlackLog> implements MdBlackLogService {

    @Autowired
    MdBlackLogMapper mdBlackLogMapper;

    @Override
    public PageResult<MdBlackLogVO> getBlackList(BlackLogParam blackLogParam) {
        if (ObjectUtils.isEmpty(blackLogParam.getAppId())) {
            throw new IllegalParamException("项目id不能为空");
        }
        if (ObjectUtils.isEmpty(blackLogParam.getUserId())) {
            throw new IllegalParamException("用户id不能为空");
        }
        Page<MdBlackLogVO> page = new Page(blackLogParam.getPageNum(), blackLogParam.getPageSize());
        IPage<MdBlackLogVO> blackList = mdBlackLogMapper.getBlackList(page, blackLogParam);

        return PageResult.build(blackLogParam.getPageNum(), blackLogParam.getPageSize(), blackList.getPages(), blackList.getTotal(), blackList.getRecords());
    }
}
