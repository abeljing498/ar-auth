package com.lyentech.bdc.md.auth.config.security.login.sign;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginBlackUserException;
import com.lyentech.bdc.md.auth.config.security.login.exception.MdLoginSignErrorException;
import com.lyentech.bdc.md.auth.dao.MdBlackUserMapper;
import com.lyentech.bdc.md.auth.dao.MdUserMapper;
import com.lyentech.bdc.md.auth.model.entity.MdBlackUser;
import com.lyentech.bdc.md.auth.model.entity.MdUser;
import com.lyentech.bdc.md.auth.util.SpringContextUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;

import static com.lyentech.bdc.md.auth.common.constant.MdUserAuthChangeConstant.USER_CHANGE_PATH;

/**
 * @author 260583
 */
public class MdSignAuthenticationProvider implements AuthenticationProvider {
    private MdUserMapper userMapper;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    private StringRedisTemplate stringRedisTemplate;
    private MdBlackUserMapper blackUserMapper;
    public MdSignAuthenticationProvider(MdUserMapper userMapper, MdBlackUserMapper blackUserMapper, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.blackUserMapper = blackUserMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MdSignAuthenticationToken authenticationToken = (MdSignAuthenticationToken) authentication;

        String credential = authenticationToken.getCredential();
        String clientId = authenticationToken.getClientId();
        StringBuilder stringBuilder = new StringBuilder();
        String credentialKey = stringBuilder.append("ar:auth:login:credential:clientId:").append(clientId).append(":").append(credential).toString();
        String s = stringRedisTemplate.boundValueOps(credentialKey).get();
        if (s == null ){
            throw new MdLoginSignErrorException("凭证无效，请重试");
        }
        MdUser mdUser = userMapper.selectById(Long.valueOf(s));
        MdBlackUser mdBlackUser = blackUserMapper.selectOne(Wrappers.<MdBlackUser>lambdaQuery().eq(MdBlackUser::getAppId, clientId)
                .eq(MdBlackUser::getUserId, mdUser.getId()));
        if (ObjectUtils.isNotEmpty(mdBlackUser)) {
            throw new MdLoginBlackUserException("用户因" + mdBlackUser.getReason() + "被加入黑名单，暂无权限进入当 前系统");
        }
        UserDetails userDetails = mdUser;

        StringBuilder authBuilder = new StringBuilder();
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        String authKey = authBuilder.append(USER_CHANGE_PATH)
                .append(mdUser.getId()).append(":")
                .append("client:").append(clientId).toString();
        stringRedisTemplate.delete(authKey);
        //删除凭证
        stringRedisTemplate.delete(credentialKey);
        return createSuccessAuthentication(userDetails, authentication, userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (MdSignAuthenticationToken.class
                .isAssignableFrom(authentication));
    }

    protected Authentication createSuccessAuthentication(Object principal,
                                                         Authentication authentication, UserDetails user) {
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                principal, authentication.getCredentials(),
                authoritiesMapper.mapAuthorities(user.getAuthorities()) );
        result.setDetails(authentication.getDetails());

        return result;
    }
}
