package com.lyentech.bdc.md.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.model.entity.Org;
import com.lyentech.bdc.md.auth.model.param.OrgOrderParam;
import com.lyentech.bdc.md.auth.model.param.OrgParam;
import com.lyentech.bdc.md.auth.model.param.QueryOrgNameParam;
import com.lyentech.bdc.md.auth.model.vo.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
public interface OrgService extends IService<Org> {

    /**
     * 根据tenantId获取组织架构树
     * @param tenantId
     * @return
     */
    @Cacheable(value = "au:app:org", keyGenerator = "springCacheKeyGenerator")
    @CacheEvict(value = "au:app:org", allEntries = true)
    List<OrgTree> getTreeByTenantId(Long tenantId);

    /**
     * 在某租户下新增组织
     * @param orgParam
     */
    @CacheEvict(value = "au:app:org", allEntries = true)
    OrgVO add(OrgParam orgParam);


    /**
     * 根据id修改组织
     * @param orgParam
     */
    @CacheEvict(value = "au:app:org", allEntries = true)
    void update(OrgParam orgParam);

    /**
     * 调整顺序
     * @param orderParamList
     */
    @CacheEvict(value = "au:app:org", allEntries = true)
    void updateOrder(List<OrgOrderParam> orderParamList);

    /**
     * 根据id删除组织
     * @param id
     */
    @CacheEvict(value = "au:app:org", allEntries = true)
    void delete(Long id);

    /**
     * 根据组织id获取下级组织
     *
     * @param id
     * @return
     */
    @Cacheable(value = "au:app:org", keyGenerator = "springCacheKeyGenerator")
    @CacheEvict(value = "au:app:org", allEntries = true)
    PageResult getSubById(Long pageNum, Long pageSize, Long id, Long tenantId);

    /**
     * 获取组织详情
     *
     * @param id
     * @return
     */
    @Cacheable(value = "au:app:org", keyGenerator = "springCacheKeyGenerator")
    @CacheEvict(value = "au:app:org", allEntries = true)
    OrgVO getDetail(Long id);

    /**
     * 获取租户id
     *
     * @param orgId 组织id
     * @return tenantId
     */
    Long getTenantId(Long orgId);

    /**
     * 根据组织id和层高获取组织id
     *
     * @param param
     * @return
     */
    OrgHeightNameVO getOrgName(QueryOrgNameParam param);

    OrgVO getOrgByCid(String customId, Long tenantId);

    Integer getOrgUserNum(Long id, Long tenantId);

    List<MeOrgVO> getMeOrg(Long userId, Long tenantId);

    /**
     * 获取组织详情
     *
     * @param tenantId
     * @return
     */
    @Cacheable(value = "au:app:org", keyGenerator = "springCacheKeyGenerator")
    @CacheEvict(value = "au:app:org", allEntries = true)
    List<OrgUserTree> treeOrgUsers(Long tenantId);

    List<OrgTree> getTreeByUser(Long tenantId, Long userId);

    List<OrgUserTree> treeOrgUsersByOrgId(Long tenantId, Long orgId);

    Map<String,Object> getUserStatusNum(Long id, Long tenantId, String appId);

    void deleteOrg(OrgParam orgParam);
}
