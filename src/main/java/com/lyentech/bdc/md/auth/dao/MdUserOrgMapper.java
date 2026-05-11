package com.lyentech.bdc.md.auth.dao;

import com.lyentech.bdc.md.auth.model.entity.MdUserOrg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyentech.bdc.md.auth.model.vo.OrgNewVO;
import com.lyentech.bdc.md.auth.model.vo.OrgUserVO;
import com.lyentech.bdc.md.auth.model.vo.StatusCountVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yan
 * @since 2022-05-24
 */
public interface MdUserOrgMapper extends BaseMapper<MdUserOrg> {

    /**
     * 通过组织id查找，电话号码相同的员工
     * @param orgId
     * @param phone
     * @return
     */
    Long getByOrgId(@Param("orgId") Long orgId, @Param("phone") String phone);

    /**
     * 查找与添加的人员电话号码相同name不同组织不相同的人员
     * @param orgId
     * @param phone
     * @return
     */
    Long selectUser(@Param("orgId") Long orgId, @Param("phone") String phone,@Param("name")String name);

    /**
     * 查找与添加的人员电话号码相同youxiang不同组织不相同的人员
     *
     * @param orgId
     * @param phone
     * @param email
     * @return
     */
    Long selectUserByEmail(@Param("orgId") Long orgId, @Param("phone") String phone, @Param("email") String email);


    /**
     * 删除该组织id下所有的人员
     *
     * @param orgId 组织id
     */
    void deleteUser(@Param("orgId") Long orgId);

    /**
     * 通过人员查找所有组织
     *
     * @param userId
     * @return
     */
    List<OrgNewVO> getOrgInfo(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    List<Long> getUserIds(@Param("orgId") Long orgId, @Param("tenantId") Long tenantId);

    List<Long> getOrgIds(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    /**
     * 判断用户是否存在本项目中（查找此用户在项目中的组织数）
     *
     * @param userId
     * @param appKey
     * @return 返回>0视为存在
     */
    Long existUserInAppKey(@Param("userId") Long userId, @Param("appKey") String appKey);

    void updateByd(@Param("userId") Long userId, @Param("time")Date time);

    List<OrgUserVO> listUsersById(@Param("id") Long id);

    Long countOrgUser(@Param("orgId") Long id);

    /**
     * 通过组织查询组织下所有用户个数
     * @param orgIds
     * @param tenantId
     * @return
     */
    List<StatusCountVO> getUserStatusNum(@Param("orgIds") List<Long> orgIds, @Param("tenantId") Long tenantId);
}
