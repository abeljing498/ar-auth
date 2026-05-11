package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.param.LoginLogParam;
import com.lyentech.bdc.md.auth.model.vo.LoginLogVO;
import org.apache.ibatis.annotations.Param;

/**
 * @Author :yan
 * @Date :Create in 2022/11/25
 * @Description :
 */

public interface MdLoginLogMapper extends BaseMapper<MdLoginLog> {

    IPage<LoginLogVO> selectLoginLog(@Param("page") Page<LoginLogVO> page,
                                     @Param("loginLogParam") LoginLogParam loginLogParam);
}
