package com.lyentech.bdc.md.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lyentech.bdc.exception.IllegalParamException;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.common.constant.MdResutConstant;
import com.lyentech.bdc.md.auth.common.constant.MdUserStatusConstant;
import com.lyentech.bdc.md.auth.common.constant.OrgOperationType;
import com.lyentech.bdc.md.auth.dao.*;
import com.lyentech.bdc.md.auth.model.entity.*;
import com.lyentech.bdc.md.auth.model.param.OrgOrderParam;
import com.lyentech.bdc.md.auth.model.param.OrgParam;
import com.lyentech.bdc.md.auth.model.param.QueryOrgNameParam;
import com.lyentech.bdc.md.auth.model.vo.*;
import com.lyentech.bdc.md.auth.service.OrgService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyentech.bdc.md.auth.util.BasicTree;
import com.lyentech.bdc.md.auth.util.PinYinMultiCharactersUtils;
import com.lyentech.bdc.md.auth.util.RandomAccountGenerator;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


import static com.lyentech.bdc.md.auth.util.PinYinMultiCharactersUtils.getMultiCharactersPinYin;
import static com.lyentech.bdc.md.auth.util.PinYinUtils.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yan
 * @since 2022-05-23
 */
@Service
public class OrgServiceImpl extends ServiceImpl<OrgMapper, Org> implements OrgService {

    @Resource
    private MdUserOrgMapper mdUserOrgMapper;

    @Resource
    private MdOrgHeadMapper orgHeadMapper;
    @Value("${spring.profiles.active}")
    private String ENV;
    @Resource
    private MdOrgExternalMapper mdOrgExternalMapper;
    @Resource
    private AppTenantMapper appTenantMapper;
    @Resource
    private MdOrgOperationLogMapper mdOrgOperationLogMapper;

    @Override
    public List<OrgTree> getTreeByTenantId(Long tenantId) {
        String orgName = null;
        return BasicTree.listToTree(baseMapper.getByTenantId(tenantId, null));
    }

    @Override
    public OrgVO add(OrgParam orgParam) {
        MdOrgOperationLog mdOrgOperationLog = new MdOrgOperationLog();
        mdOrgOperationLog.setOperationUserId(orgParam.getOperateUserId());
        mdOrgOperationLog.setNotes(JSON.toJSONString(orgParam));
        mdOrgOperationLog.setOperationUserName(orgParam.getOperateUserName());
        mdOrgOperationLog.setAppId(orgParam.getAppId());
        mdOrgOperationLog.setOperationType(OrgOperationType.ADD.getCode());
        mdOrgOperationLog.setUserIp(orgParam.getUserIp());
        mdOrgOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdOrgOperationLog.setTenantId(orgParam.getTenantId());
        mdOrgOperationLog.setCreateTime(new Date());
        if (null == orgParam.getName() || null == orgParam.getPid()) {
            mdOrgOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdOrgOperationLogMapper.insert(mdOrgOperationLog);
            throw new IllegalParamException("组织名或上级id不能为空");
        }
        //判断当前组织是否和该父节点的所有组织重名
        Integer count = baseMapper.selectCount(Wrappers.<Org>lambdaQuery().eq(Org::getPid, orgParam.getPid()).eq(Org::getName, orgParam.getName()));
        if (count > 0) {
            mdOrgOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdOrgOperationLogMapper.insert(mdOrgOperationLog);
            throw new IllegalParamException("组织名称已存在，请使用其他名称");
        }
        Org org = new Org();
        BeanUtils.copyProperties(orgParam, org);
        Long height;
        if (orgParam.getPid().equals(0)) {
            height = 1L;
        } else {
            height = baseMapper.selectOne(Wrappers.<Org>lambdaQuery().eq(Org::getId, orgParam.getPid())).getHeight() + 1;
        }
        org.setHeight(height);
        //设置组织名拼音
        String abbreviation = null;
        try {
            abbreviation = chineseToPinYinS(org.getName());
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        String orgPinyin = null;
        if (PinYinMultiCharactersUtils.isMultiChineseWord(org.getName())) {
            orgPinyin = stringBuilder.append(getChinesePinyinFromName(org.getName())).append(",")
                    .append(abbreviation).append(",")
                    .append(getMultiCharactersPinYin(org.getName())).toString();
        } else {
            orgPinyin = stringBuilder.append(getChinesePinyinFromName(org.getName())).append(",")
                    .append(abbreviation).toString();
        }
        org.setOrgPinyin(orgPinyin);
        List<Org> orgs = baseMapper.selectList(Wrappers.<Org>lambdaQuery().eq(Org::getPid, orgParam.getPid()));
        if (ObjectUtils.isNotEmpty(orgs)) {
            List<Long> orders = orgs.stream()
                    .map(Org::getOrgOrder)
                    .collect(Collectors.toList());
            Long max = Collections.max(orders);
            if (max == 0L) {
                org.setOrgOrder(0L);
            } else {
                org.setOrgOrder(max + 1);
            }
        } else {
            org.setOrgOrder(0L);
        }
        baseMapper.insert(org);
        //将组织负责人信息插入到组织负责人表
        if (StringUtils.isNotBlank(orgParam.getDirector()) || StringUtils.isNotBlank(orgParam.getPhone())) {
            MdOrgHead orgHead = new MdOrgHead();
            orgHead.setDirector(orgParam.getDirector());
            orgHead.setPhone(orgParam.getPhone());
            orgHead.setOrgId(org.getId());
            orgHeadMapper.insert(orgHead);
        }
        //判断是否有自定义Id,若有将自定义Id插入到关联表中
        MdOrgExternal mdOrgExternal = new MdOrgExternal();
        mdOrgExternal.setTenantId(orgParam.getTenantId());
        mdOrgExternal.setOrgId(org.getId());
        if (ObjectUtils.isNotEmpty(orgParam.getCustomId())) {
            mdOrgExternal.setCustomId(orgParam.getCustomId());
        } else {
            String s = RandomAccountGenerator.getRandomAccount(5);
            mdOrgExternal.setCustomId(s);
        }
        Integer count1 = mdOrgExternalMapper.selectCount(Wrappers.<MdOrgExternal>lambdaQuery().eq(MdOrgExternal::getCustomId, mdOrgExternal.getCustomId()).eq(MdOrgExternal::getTenantId, orgParam.getTenantId()));
        if (count1 > 0) {
            mdOrgOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdOrgOperationLogMapper.insert(mdOrgOperationLog);
            throw new IllegalParamException("自定义id与其他id重复,请重新输入");
        }
        mdOrgExternalMapper.insert(mdOrgExternal);
        OrgVO orgVO = new OrgVO();
        orgVO.setCustomId(mdOrgExternal.getCustomId());
        orgVO.setId(org.getId());
        orgVO.setName(org.getName());
        orgVO.setPid(org.getPid());
        mdOrgOperationLog.setOrgId(org.getId());
        mdOrgOperationLogMapper.insert(mdOrgOperationLog);
        return orgVO;
    }

    @Override
    public void update(OrgParam orgParam) {
        MdOrgOperationLog mdOrgOperationLog = new MdOrgOperationLog();
        mdOrgOperationLog.setOperationUserId(orgParam.getOperateUserId());
        mdOrgOperationLog.setNotes(JSON.toJSONString(orgParam));
        mdOrgOperationLog.setOperationUserName(orgParam.getOperateUserName());
        mdOrgOperationLog.setAppId(orgParam.getAppId());
        mdOrgOperationLog.setOperationType(OrgOperationType.UPDATE.getCode());
        mdOrgOperationLog.setOrgId(orgParam.getId());
        mdOrgOperationLog.setUserIp(orgParam.getUserIp());
        mdOrgOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdOrgOperationLog.setTenantId(orgParam.getTenantId());
        mdOrgOperationLog.setCreateTime(new Date());
        if (null == orgParam.getId()) {
            mdOrgOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdOrgOperationLogMapper.insert(mdOrgOperationLog);
            throw new IllegalParamException("组织id不能为空");
        }


        Integer count1 = baseMapper.selectCount(Wrappers.<Org>lambdaQuery().eq(Org::getPid, orgParam.getPid()).eq(Org::getName, orgParam.getName()).ne(Org::getId, orgParam.getId()));
        if (count1 > 0) {
            mdOrgOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdOrgOperationLogMapper.insert(mdOrgOperationLog);
            throw new IllegalParamException("组织名称已存在，请使用其他名称");
        }
        Org org = new Org();
        BeanUtils.copyProperties(orgParam, org);
        if (isChineseName(org.getName())) {
            org.setOrgPinyin(getChinesePinyinFromName(org.getName()));
        } else {
            org.setOrgPinyin(null);
        }
        if (PinYinMultiCharactersUtils.isMultiChineseWord(org.getName())) {
            org.setOrgPinyin2(getMultiCharactersPinYin(org.getName()));
        } else {
            org.setOrgPinyin2(null);
        }
        Org orderOrg = baseMapper.selectById(org.getId());
        if (!orderOrg.getPid().equals(org.getPid())) {
            mdOrgOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdOrgOperationLogMapper.insert(mdOrgOperationLog);
            throw new IllegalParamException("不可切换上级组织");
        }
        String abbreviation = null;
        try {
            abbreviation = chineseToPinYinS(org.getName());
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        String orgPinyin = null;
        if (PinYinMultiCharactersUtils.isMultiChineseWord(org.getName())) {
            orgPinyin = stringBuilder.append(getChinesePinyinFromName(org.getName())).append(",")
                    .append(abbreviation).append(",")
                    .append(getMultiCharactersPinYin(org.getName())).toString();
        } else {
            orgPinyin = stringBuilder.append(getChinesePinyinFromName(org.getName())).append(",")
                    .append(abbreviation).toString();
        }
        org.setOrgPinyin(orgPinyin);
        baseMapper.updateById(org);
        Integer count = orgHeadMapper.selectCount(Wrappers.<MdOrgHead>lambdaQuery().eq(MdOrgHead::getOrgId, orgParam.getId()));
        MdOrgHead orgHead = new MdOrgHead();
        if (StringUtils.isNotBlank(orgParam.getDirector()) || StringUtils.isNotBlank(orgParam.getPhone())) {
            orgHead.setDirector(orgParam.getDirector());
            orgHead.setPhone(orgParam.getPhone());
            orgHead.setOrgId(orgParam.getId());
            if (count == 0) {
                orgHeadMapper.insert(orgHead);
            } else {
                orgHeadMapper.update(orgHead, Wrappers.<MdOrgHead>lambdaQuery().eq(MdOrgHead::getOrgId, orgParam.getId()));
            }
        } else if (count > 0) {
            orgHeadMapper.delete(Wrappers.<MdOrgHead>lambdaQuery().eq(MdOrgHead::getOrgId, orgParam.getId()));
        }
        mdOrgOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdOrgOperationLogMapper.insert(mdOrgOperationLog);
    }

    @Override
    public void updateOrder(List<OrgOrderParam> orderParamList) {
        for (OrgOrderParam orgOrderParam : orderParamList) {
            if (ObjectUtils.isNotEmpty(orgOrderParam)) {
                Org org = baseMapper.selectById(orgOrderParam.getId());
                org.setOrgOrder(orgOrderParam.getOrder());
                baseMapper.updateById(org);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {

        Integer userOrgCount = mdUserOrgMapper.selectCount(Wrappers.<MdUserOrg>lambdaQuery().eq(MdUserOrg::getOrgId, id));
        Integer orgCount = baseMapper.selectCount(Wrappers.<Org>lambdaQuery().eq(Org::getPid, id));
        if (orgCount > 0 || userOrgCount > 0) {
            throw new IllegalParamException("该组织存在子级或员工，无法删除");
        }
        baseMapper.deleteById(id);
        orgHeadMapper.delete(Wrappers.<MdOrgHead>lambdaQuery().eq(MdOrgHead::getOrgId, id));
        mdOrgExternalMapper.delete(Wrappers.<MdOrgExternal>lambdaQuery().eq(MdOrgExternal::getOrgId, id));
    }

    @Override
    public PageResult getSubById(Long pageNum, Long pageSize, Long id, Long tenantId) {
        IPage<Org> orgPage = baseMapper.selectPage(new Page<>(pageNum, pageSize), Wrappers.<Org>lambdaQuery()
                .eq(ObjectUtils.isNotEmpty(id), Org::getPid, id)
                .eq(ObjectUtils.isEmpty(id), Org::getTenantId, tenantId));
        List<OrgVO> orgVOS = new ArrayList<>();
        if (orgPage != null) {
            List<Org> records = orgPage.getRecords();
            orgVOS = records.stream().map(org -> {
                OrgVO orgVO = new OrgVO();
                BeanUtils.copyProperties(org, orgVO);
                List<Long> subIds = baseMapper.getSubId(org.getId());
                subIds.add(org.getId());
                Integer count = mdUserOrgMapper.selectCount(new QueryWrapper<MdUserOrg>()
                        .select("Distinct user_id").eq("org_id", org.getId()));
                orgVO.setUserNum(count);
                MdOrgHead orgHead = getHeadById(org.getId());
                if (orgHead != null) {
                    orgVO.setDirector(orgHead.getDirector());
                    orgVO.setPhone(orgHead.getPhone());
                }
                //获取自定义id
                MdOrgExternal orgExternal = mdOrgExternalMapper.selectOne(Wrappers.<MdOrgExternal>lambdaQuery().eq(MdOrgExternal::getOrgId, org.getId()).eq(MdOrgExternal::getTenantId, tenantId));
                if (ObjectUtils.isNotEmpty(orgExternal)) {
                    orgVO.setCustomId(orgExternal.getCustomId());
                }
                return orgVO;
            }).collect(Collectors.toList());
        }
        return PageResult.build(orgPage.getCurrent(), orgPage.getSize(), orgPage.getPages(), orgPage.getTotal(), orgVOS);

    }

    @Override
    public OrgVO getDetail(Long id) {
        OrgVO vo = new OrgVO();
        Org org = baseMapper.selectOne(Wrappers.<Org>lambdaQuery().eq(Org::getId, id));
        BeanUtils.copyProperties(org, vo);
        if (org.getPid() == 0L) {
            vo.setPidName(null);
        } else {
            String pidName = baseMapper.selectOne(Wrappers.<Org>lambdaQuery().eq(Org::getId, vo.getPid())).getName();
            vo.setPidName(pidName);
        }
        Integer count = mdUserOrgMapper.selectCount(new QueryWrapper<MdUserOrg>().select("Distinct user_id").eq("org_id", id));
        if (count == null) {
            vo.setUserNum(0);
        } else {
            vo.setUserNum(count);
        }
        MdOrgHead orgHead = getHeadById(id);
        if (orgHead != null) {
            vo.setDirector(orgHead.getDirector());
            vo.setPhone(orgHead.getPhone());
        }
        //获取自定义id
        MdOrgExternal orgExternal = mdOrgExternalMapper.selectById(id);
        if (ObjectUtils.isNotEmpty(orgExternal)) {
            vo.setCustomId(orgExternal.getCustomId());
        }
        return vo;
    }

    public MdOrgHead getHeadById(Long id) {
        MdOrgHead mdOrgHead = orgHeadMapper.selectOne(Wrappers.<MdOrgHead>lambdaQuery().eq(MdOrgHead::getOrgId, id));
        return mdOrgHead;
    }

    @Override
    public Long getTenantId(Long orgId) {
        return baseMapper.selectOne(Wrappers.<Org>lambdaQuery().eq(Org::getId, orgId)).getTenantId();
    }


    @Override
    public OrgHeightNameVO getOrgName(QueryOrgNameParam nameParam) {
        if (ObjectUtils.isEmpty(nameParam)) {
            throw new IllegalParamException("组织id和组织高度不能为空");
        }
        OrgHeightNameVO nameVO = new OrgHeightNameVO();
        List<String> nameLists = new ArrayList<>();
        List<Long> idLists = new ArrayList<>();
        List<String> customIdLists = new ArrayList<>();
        for (Long id : nameParam.getIds()) {
            OrgVO orgVO = getOrgNameById(id, nameParam.getHeight());
            if (ObjectUtils.isNotEmpty(orgVO)) {
                nameLists.add(orgVO.getName());
                idLists.add(orgVO.getId());
                customIdLists.add(orgVO.getCustomId());
            }
        }
        nameLists = nameLists.stream().distinct().collect(Collectors.toList());
        idLists = idLists.stream().distinct().collect(Collectors.toList());
        customIdLists = customIdLists.stream().distinct().collect(Collectors.toList());
        nameVO.setOrgNameLists(nameLists);
        nameVO.setOrgCustomIdLists(customIdLists);
        nameVO.setOrgIdLists(idLists);
        return nameVO;
    }


    public OrgVO getOrgNameById(Long id, Long height) {
        Org org = baseMapper.selectOne(Wrappers.<Org>lambdaQuery().eq(Org::getId, id));
        OrgVO orgVO = new OrgVO();
        BeanUtils.copyProperties(org, orgVO);
        if (100L == height) {
            String customId = mdOrgExternalMapper.selectOne(Wrappers.<MdOrgExternal>lambdaQuery().eq(MdOrgExternal::getOrgId, org.getId())).getCustomId();
            orgVO.setCustomId(customId);
            return orgVO;
        }
        if (height > org.getHeight()) {
            return null;
        } else if (height < org.getHeight()) {
            orgVO = getOrgNameById(org.getPid(), height);
            return orgVO;
        } else {
            String customId = mdOrgExternalMapper.selectOne(Wrappers.<MdOrgExternal>lambdaQuery().eq(MdOrgExternal::getOrgId, org.getId())).getCustomId();
            orgVO.setCustomId(customId);
            return orgVO;
        }
    }

    @Override
    public OrgVO getOrgByCid(String customId, Long tenantId) {
        if (ObjectUtils.isEmpty(customId) || ObjectUtils.isEmpty(tenantId)) {
            throw new IllegalParamException("自定义id不能为空或租户id不能为空");
        }
        OrgVO orgByCid = mdOrgExternalMapper.getOrgByCid(customId, tenantId);
        return orgByCid;
    }

    @Override
    public Integer getOrgUserNum(Long id, Long tenantId) {
        //获取该节点包括该节点的所有组织id
        List<Long> orgIdList = selectOrgAndChildren(id);
        Set<Long> userIdList = Sets.newHashSet();
        if (ObjectUtils.isNotEmpty(orgIdList)) {
            for (Long idItem : orgIdList) {
                List<Long> userIds = mdUserOrgMapper.getUserIds(idItem, tenantId);
                for (Long userId : userIds) {
                    userIdList.add(userId);
                }
            }
        }
        int userNum = userIdList.size();
        return userNum;
    }

    public List<Long> selectOrgAndChildren(Long id) {
        Set<Long> orgSet = Sets.newHashSet();
        findChildOrg(orgSet, id);
        List<Long> orgIdList = Lists.newArrayList();
        if (id != null) {
            for (Long idItem : orgSet) {
                orgIdList.add(idItem);
            }
        }
        return orgIdList;
    }

    private Set<Long> findChildOrg(Set<Long> orgSet, Long id) {
        Org org = baseMapper.selectById(id);
        if (org != null) {
            orgSet.add(org.getId());
        }
        //查找子节点。递归
        List<Org> orgList = baseMapper.selectList(Wrappers.<Org>lambdaQuery().eq(Org::getPid, id));
        for (Org org1 : orgList) {
            findChildOrg(orgSet, org1.getId());
        }
        return orgSet;
    }

    @Override
    public List<MeOrgVO> getMeOrg(Long userId, Long tenantId) {
        if (ObjectUtils.isEmpty(userId)) {
            throw new IllegalParamException("用户id不能为空");
        }
        List<MdUserOrg> orgs = mdUserOrgMapper.selectList(Wrappers.<MdUserOrg>lambdaQuery().
                eq(MdUserOrg::getUserId, userId));
        List<MeOrgVO> orgVOS = new ArrayList<>();
        MeOrgVO meOrgVO = new MeOrgVO();
        if (ObjectUtils.isNotEmpty(orgs)) {
            for (MdUserOrg userOrg : orgs) {
                meOrgVO = getPidName(userOrg.getOrgId(), tenantId, null, null);
                if (ObjectUtils.isNotEmpty(meOrgVO) && StringUtils.isNotBlank(meOrgVO.getOrgName())) {
                    orgVOS.add(meOrgVO);
                }
            }
        }
        return orgVOS;
    }


    public MeOrgVO getPidName(Long id, Long tenantId, String orgName, String customId) {
        OrgVO org = baseMapper.getMeOrg(id, tenantId);
        MeOrgVO meOrgVO = new MeOrgVO();
        if (ObjectUtils.isEmpty(org)) {
            return null;
        }
        if (orgName == null) {
            orgName = org.getName();
            customId = org.getCustomId();
        } else {
            orgName = org.getName().concat("/" + orgName);
            if (org.getCustomId() != null) {
                customId = org.getCustomId().concat("/" + customId);
            }
        }
        meOrgVO.setOrgName(orgName);
        meOrgVO.setCustomIds(customId);
        if (org.getPid() != 0) {
            meOrgVO = getPidName(org.getPid(), tenantId, orgName, customId);
        }
        return meOrgVO;
    }

    @Override
    public List<OrgUserTree> treeOrgUsers(Long tenantId) {
        return OrgUserTree.listToTree(baseMapper.getTreeAndUser(tenantId, null));
    }

    @Override
    public List<OrgTree> getTreeByUser(Long tenantId, Long userId) {
        Long companyId = null;
        if (ENV.equals("prod")) {
            companyId = 8072L;
        } else {
            companyId = 2588L;
        }
        if (userId == null) {
            return baseMapper.getFirstByTenantIds(tenantId, null, companyId);
        } else {
            List<Long> userOrgIds = mdUserOrgMapper.getOrgIds(userId, tenantId);
            if (CollectionUtils.isEmpty(userOrgIds)) {
                return Collections.emptyList();
            } else {
                List<Long> longList = new ArrayList<>();
                for (Long orgId : userOrgIds) {
                    List<Long> pIds = findParentIds(orgId);
                    longList.addAll(pIds);
                    longList.add(orgId);
                }
                if (!CollectionUtils.isEmpty(longList)) {
                    List<Long> noDuplicatesStreamList = longList.stream()
                            .distinct()
                            .collect(Collectors.toList());
                    return baseMapper.getFirstByTenantIds(tenantId, noDuplicatesStreamList, companyId);
                } else {
                    return null;
                }

            }
        }

    }

    public List<Long> findParentIds(Long nodeId) {
        List<Long> parentIds = new ArrayList<>();
        LambdaQueryWrapper<Org> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Org::getId, nodeId);
        Org entity = baseMapper.selectOne(queryWrapper);
        if (entity != null && entity.getPid() != null) {
            parentIds.add(entity.getPid());
            parentIds.addAll(findParentIds(entity.getPid()));

        }

        return parentIds;
    }

    @Override
    public List<OrgUserTree> treeOrgUsersByOrgId(Long tenantId, Long orgId) {
        List<Long> idList = baseMapper.getSubId(orgId);
        if (!CollectionUtils.isEmpty(idList)) {
            return OrgUserTree.listToTree(baseMapper.getTreeAndUser(tenantId, idList));
        }
        return null;
    }

    @Override
    public Map<String, Object> getUserStatusNum(Long id, Long tenantId, String appId) {
        Map<String, Object> countMap = new HashMap<>();
        List<Long> orgIdList = null;
        if (id != null) {
            orgIdList = selectOrgAndChildren(id);
        }

        List<StatusCountVO> mapList = mdUserOrgMapper.getUserStatusNum(orgIdList, tenantId);
        Long normalCount = 0L;
        Long blackCount = 0L;
        for (StatusCountVO statusCountVO : mapList) {
            Integer status = statusCountVO.getStatus();
            if (MdUserStatusConstant.BLACK == status) {
                blackCount = statusCountVO.getCnt();
            } else {
                normalCount = statusCountVO.getCnt();
            }

        }
        Long allCount = normalCount + blackCount;
        countMap.put("normalCount", normalCount);
        countMap.put("blackCount", blackCount);
        countMap.put("allCount", allCount);
        return countMap;
    }

    @Override
    public void deleteOrg(OrgParam orgParam) {
        MdOrgOperationLog mdOrgOperationLog = new MdOrgOperationLog();
        mdOrgOperationLog.setOperationUserId(orgParam.getOperateUserId());
        mdOrgOperationLog.setNotes(JSON.toJSONString(orgParam));
        mdOrgOperationLog.setOperationUserName(orgParam.getOperateUserName());
        mdOrgOperationLog.setAppId(orgParam.getAppId());
        mdOrgOperationLog.setOperationType(OrgOperationType.DELETE.getCode());
        mdOrgOperationLog.setOrgId(orgParam.getId());
        mdOrgOperationLog.setUserIp(orgParam.getUserIp());
        mdOrgOperationLog.setIsSuccess(MdResutConstant.SUCCESS);
        mdOrgOperationLog.setTenantId(orgParam.getTenantId());
        mdOrgOperationLog.setCreateTime(new Date());
        Integer userOrgCount = mdUserOrgMapper.selectCount(Wrappers.<MdUserOrg>lambdaQuery().eq(MdUserOrg::getOrgId, orgParam.getId()));
        Integer orgCount = baseMapper.selectCount(Wrappers.<Org>lambdaQuery().eq(Org::getPid, orgParam.getId()));
        if (orgCount > 0 || userOrgCount > 0) {
            mdOrgOperationLog.setIsSuccess(MdResutConstant.FAIL);
            mdOrgOperationLogMapper.insert(mdOrgOperationLog);
            throw new IllegalParamException("该组织存在子级或员工，无法删除");
        }
        mdOrgOperationLogMapper.insert(mdOrgOperationLog);
        delete(orgParam.getId());
    }
}
