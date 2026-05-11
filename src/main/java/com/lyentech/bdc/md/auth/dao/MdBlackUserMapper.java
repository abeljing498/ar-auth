package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyentech.bdc.md.auth.model.entity.MdBlackLog;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.param.BlackUserListParam;
import com.lyentech.bdc.md.auth.model.param.LoginLogParam;
import com.lyentech.bdc.md.auth.model.vo.LoginLogVO;
import com.lyentech.bdc.md.auth.model.vo.MdBlackUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author YuYi
 * @create 2023/4/12
 * @create 10:32
 */
@Mapper
@Repository
public interface MdBlackUserMapper extends BaseMapper<MdBlackUser> {
    IPage<MdBlackUserVO> getUserList(@Param("page") Page<MdBlackUserVO> page,
                                     @Param("blackUserListParam") BlackUserListParam blackUserListParam);

}
