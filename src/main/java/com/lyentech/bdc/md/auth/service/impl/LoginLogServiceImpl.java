package com.lyentech.bdc.md.auth.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyentech.bdc.http.response.PageResult;
import com.lyentech.bdc.md.auth.dao.MdLoginLogMapper;
import com.lyentech.bdc.md.auth.model.entity.MdLoginLog;
import com.lyentech.bdc.md.auth.model.param.LoginLogParam;
import com.lyentech.bdc.md.auth.model.vo.LoginLogVO;
import com.lyentech.bdc.md.auth.service.LoginLogService;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoginLogServiceImpl extends ServiceImpl<MdLoginLogMapper, MdLoginLog> implements LoginLogService {

    @Value("${spring.profiles.active}")
    private static String env;

    private static DbSearcher searcher;
    private static DbConfig config;
    @Resource
    private MdLoginLogMapper mdLoginLogMapper;

    @Override
    public PageResult<LoginLogVO> getLoginLog(Long pageNum, Long pageSize,String beginTime, String endTime, String user, String loginWay, String appkey) {
        Page<LoginLogVO> page = new Page(pageNum, pageSize);

        LoginLogParam loginLogParam = new LoginLogParam();
        loginLogParam.setPageNum(pageNum);
        loginLogParam.setPageSize(pageSize);
        loginLogParam.setUser(user);
        loginLogParam.setEndTime(endTime);
        loginLogParam.setBeginTime(beginTime);
        loginLogParam.setLoginWay(loginWay);
        loginLogParam.setAppKey(appkey);
        if (!StringUtils.isEmpty(user)&& user.matches("[0-9]*")){
            loginLogParam.setId(Long.parseLong(user));
            loginLogParam.setUser(null);
        }
        IPage<LoginLogVO> loginLogVOIPage = mdLoginLogMapper.selectLoginLog(page, loginLogParam);
        List<LoginLogVO> loginLogVOList = loginLogVOIPage.getRecords();
        for (LoginLogVO loginLogVO : loginLogVOList) {
            loginLogVO.setRegion(getIpRegion(loginLogVO.getRegion()));
        }
        return PageResult.build(loginLogParam.getPageNum(), loginLogParam.getPageSize(), loginLogVOIPage.getPages(), loginLogVOIPage.getTotal(), loginLogVOList);
    }


    static {
        try {
            config = new DbConfig();
//            if ("dev".equals(env)) {
//            searcher = new DbSearcher(config, LoginLogServiceImpl.class.getResource("/").getPath() + "data/ip2re
//            gion.db");
//            } else {
                searcher = new DbSearcher(config, "/opt/ar-auth-backend/ip2region.db");
//            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public static String getIpRegion(String ip){
        try {
            Method method = searcher.getClass().getMethod("btreeSearch", String.class);
            DataBlock dataBlock = (DataBlock)method.invoke(searcher, ip);
            List<String> collect = Arrays.asList(dataBlock.getRegion().replace("|", ",").split(","))
                    .parallelStream()
                    .map(a -> a.trim())
                    .collect(Collectors.toList());
            if (collect.size() >2 && ("香港").equals(collect.get(2)) || "澳门".equals(collect.get(2)) || "台湾省".equals(collect.get(2))){
                return collect.get(2);
            }
            if ("中国".equals(collect.get(0))){
                if (!"0".equals(collect.get(2)) && !"0".equals(collect.get(3))){
                    return collect.get(2) + collect.get(3);
                }else{
                    return "未知";
                }
            }
            if (collect.contains("内网IP")){
                return "局域网";
            }else {
                return "境外ip";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "未知";
    }

}
