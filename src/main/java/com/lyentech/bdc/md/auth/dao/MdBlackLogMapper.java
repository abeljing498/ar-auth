package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.param.BlackLogParam;
import com.lyentech.bdc.md.auth.model.param.BlackUserListParam;
import com.lyentech.bdc.md.auth.model.vo.MdBlackLogVO;
import com.lyentech.bdc.md.auth.model.vo.MdBlackUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author YuYi
 * @create 2023/4/12
 * @create 10:28
 */
@Mapper
@Repository
public interface MdBlackLogMapper extends BaseMapper<com.lyentech.bdc.md.auth.model.entity.MdBlackLog> {
    IPage<MdBlackLogVO> getBlackList(@Param("page") Page<MdBlackLogVO> page,
                                     @Param("blackLogParam") BlackLogParam blackLogParam);
}
