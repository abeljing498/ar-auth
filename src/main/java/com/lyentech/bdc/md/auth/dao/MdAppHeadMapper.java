package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyentech.bdc.md.auth.model.entity.MdAppHead;
import com.lyentech.bdc.md.auth.model.param.HeadParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author YuYi
 * @create 2022/7/27
 * @create 8:41
 */
public interface MdAppHeadMapper extends BaseMapper<MdAppHead> {
    List<HeadParam> getHead(@Param("id")String id);
}
