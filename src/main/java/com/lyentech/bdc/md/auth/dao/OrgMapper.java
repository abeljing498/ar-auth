package com.lyentech.bdc.md.auth.dao;

import com.lyentech.bdc.md.auth.model.entity.Org;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyentech.bdc.md.auth.model.vo.OrgNewVO;
import com.lyentech.bdc.md.auth.model.vo.OrgTree;
import com.lyentech.bdc.md.auth.model.vo.OrgUserTree;
import com.lyentech.bdc.md.auth.model.vo.OrgVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
public interface OrgMapper extends BaseMapper<Org> {

    List<OrgTree> getByTenantId(@Param("tenantId") Long tenantId, @Param("orgIds") List<Long> orgIds);

    List<OrgTree> getFirstByTenantIds(@Param("tenantId") Long tenantId, @Param("orgIds") List<Long> orgIds, @Param("companyId")Long companyId);

    /**
     * 根据id获取所有子级id
     * @param id
     * @return
     */
    List<Long> getSubId(@Param("id") Long id);

    /**
     * 通过名称和父亲节点获取组织
     * @param pid
     * @param tenantId
     * @param name
     * @return
     */
    OrgVO getParentOrg( @Param("tenantId") Long tenantId,@Param("pid") Long pid,@Param("name") String name);

    OrgVO getMeOrg(@Param("id") Long id, @Param("tenantId") Long tenantId);

    List<OrgUserTree> getTreeAndUser(@Param("tenantId") Long tenantId, @Param("orgIds") List<Long> orgIds);

    List<OrgNewVO> getOrgById(@Param("orgId") String orgId);
    List<OrgVO>getOrgByName( @Param("tenantId") Long tenantId,@Param("name") String name);

}
