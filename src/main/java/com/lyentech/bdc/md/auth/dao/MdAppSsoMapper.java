package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyentech.bdc.md.auth.model.entity.MdAppSso;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author YuYi
 * @create 2022/7/4
 * @create 15:15
 */
@Mapper
@Repository
public interface MdAppSsoMapper extends BaseMapper<MdAppSso> {
}
