package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyentech.bdc.md.auth.model.entity.MdApp;
import com.lyentech.bdc.md.auth.model.param.HeadParam;
import com.lyentech.bdc.md.auth.model.vo.AppHeadVO;
import com.lyentech.bdc.md.auth.model.vo.HeadListVO;
import org.apache.ibatis.annotations.Param;

/**
 * @author guolanren
 */
public interface MdAppMapper {

    MdApp getById(@Param("id") String id);

    String getHomepage(@Param("id") String id);

    String getAppInitialPassword(@Param("id") String id);


    String getSecret(@Param("appKey") String appKey);


    /**
     * 获取负责人信息
     *
     * @param id
     * @return
     */
    HeadListVO getHead(@Param("id") String id);

    Long getCreateName (@Param("appId") String appId);

    String getAppManagerSetPasswordMsg(@Param("id") String id);
}
