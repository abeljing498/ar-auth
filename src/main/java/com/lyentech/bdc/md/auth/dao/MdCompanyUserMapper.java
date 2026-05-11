package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyentech.bdc.md.auth.model.entity.MdCompanyUsers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author YuYi
 * @create 2022/5/24
 * @create 14:57
 */
@Mapper
@Repository
public interface MdCompanyUserMapper extends BaseMapper<MdCompanyUsers> {


    IPage<MdCompanyUsers> getLeaveData(Page<MdCompanyUsers> page);

    List<MdCompanyUsers> getDeptLeaderByUserEmail(@Param("email")String email);
}
