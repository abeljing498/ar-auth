package com.lyentech.bdc.md.auth.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.dao.TenantRoleMapper;
import com.lyentech.bdc.md.auth.model.entity.MdRole;
import com.lyentech.bdc.md.auth.model.entity.TenantRole;
import com.lyentech.bdc.md.auth.model.vo.MdRoleVO;
import com.lyentech.bdc.md.auth.model.vo.RoleVO;
import com.lyentech.bdc.md.auth.service.TenantRoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
@Service
public class TenantRoleServiceImpl extends ServiceImpl<TenantRoleMapper, TenantRole> implements TenantRoleService {
    @Resource
    TenantRoleMapper tenantRoleMapper;
    /**
     * 分页查找租户下角色列表
     *
     * @param appId
     * @param tenantId
     * @param pageNum
     * @param pageSize
     * @param roleName
     * @return
     */
    @Override
    public PageResult<RoleVO> selectRoleList(String appId, Long tenantId, Long pageNum, Long pageSize, String roleName) {
        Page<RoleVO> page = new Page<>(pageNum,pageSize);
        IPage<RoleVO> iPage = tenantRoleMapper.getRoleListByAppId(page,appId,tenantId,roleName);

        return PageResult.build(pageNum,pageSize,iPage.getPages(),iPage.getTotal(),iPage.getRecords());
    }

    /**
     * 获取租户下所有角色
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<MdRoleVO> getRoleList(Long tenantId, String appId) {
        List<MdRole> mdRoleList = tenantRoleMapper.getRoleListByTenantId(tenantId, appId);
        List<MdRoleVO> voList = new ArrayList<>();
        for (MdRole mdRole: mdRoleList) {
            MdRoleVO mdRoleVO = new MdRoleVO();
            BeanUtils.copyProperties(mdRole,mdRoleVO);
            mdRoleVO.setId(mdRole.getId());
            mdRoleVO.setStatus(mdRole.getStatus());
            voList.add(mdRoleVO);
        }
        return voList;
    }
}
