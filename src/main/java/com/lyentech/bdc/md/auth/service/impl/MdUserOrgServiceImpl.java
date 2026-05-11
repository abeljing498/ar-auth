package com.lyentech.bdc.md.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.md.auth.model.entity.MdUserOrg;
import com.lyentech.bdc.md.auth.dao.MdUserOrgMapper;
import com.lyentech.bdc.md.auth.model.param.OrgUserParam;
import com.lyentech.bdc.md.auth.service.MdUserOrgService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yan
 * @since 2022-05-24
 */
@Service
public class MdUserOrgServiceImpl extends ServiceImpl<MdUserOrgMapper, MdUserOrg> implements MdUserOrgService {

    @Resource
    MdUserOrgMapper mdUserOrgMapper;
    @Autowired
    MdUserOrgService mdUserOrgService;
    /**
     * 添加人员组织关系
     * @param orgUserParam
     */
    @Override
    public void addOrgUser(OrgUserParam orgUserParam) throws  Exception{
        if (CollectionUtils.isEmpty(orgUserParam.getOrgIds())) {
            throw new IllegalParamException("添加人员组织不能为空");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getTenantId())) {
            throw new IllegalParamException("租户不能为空");
        }
        if (ObjectUtils.isEmpty(orgUserParam.getId())) {
            throw new IllegalParamException("用户id不为空");
        }
        //删除该租户下用户组织关系
        //查出已经
        List<Long> mdUserOrgList1 = mdUserOrgMapper.getOrgIds(orgUserParam.getId(), orgUserParam.getTenantId());
        List<Long> deleteList = mdUserOrgList1.parallelStream()
                .filter(x -> !orgUserParam.getOrgIds().contains(x)).collect(Collectors.toList());
        List<Long> addList = orgUserParam.getOrgIds().parallelStream()
                .filter(x -> !mdUserOrgList1.contains(x)).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(deleteList)) {
            for (Long id : deleteList) {
                mdUserOrgMapper.delete(Wrappers.<MdUserOrg>lambdaQuery()
                        .eq(MdUserOrg::getUserId, orgUserParam.getId())
                        .eq(MdUserOrg::getTenantId, orgUserParam.getTenantId())
                        .eq(MdUserOrg::getOrgId, id));
            }
        }

        List<MdUserOrg> mdUserOrgList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(addList)) {
            for (Long orgId : addList) {
                MdUserOrg mdUserOrg = new MdUserOrg();
                mdUserOrg.setUserId(orgUserParam.getId());
                mdUserOrg.setTenantId(orgUserParam.getTenantId());
                mdUserOrg.setOrgId(orgId);
                mdUserOrgList.add(mdUserOrg);
            }
        }
        //添加人员组织关系
        mdUserOrgService.saveBatch(mdUserOrgList);
    }
}
