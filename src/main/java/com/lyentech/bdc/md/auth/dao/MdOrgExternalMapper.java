package com.lyentech.bdc.md.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyentech.bdc.md.auth.model.entity.MdOrgExternal;
import com.lyentech.bdc.md.auth.model.vo.OrgTree;
import com.lyentech.bdc.md.auth.model.vo.OrgVO;
import org.apache.ibatis.annotations.Param;

public interface MdOrgExternalMapper extends BaseMapper<MdOrgExternal> {

    OrgVO getOrgByCid(@Param("customId") String customId, @Param("tenantId") Long tenantId);
}
