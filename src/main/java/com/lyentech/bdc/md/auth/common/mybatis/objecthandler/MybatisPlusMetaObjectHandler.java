package com.lyentech.bdc.md.auth.common.mybatis.objecthandler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author guolanren
 */
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    private static final Logger logger = LoggerFactory.getLogger(MybatisPlusMetaObjectHandler.class);

    StringBuffer sb = new StringBuffer();

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void insertFill(MetaObject metaObject) {
        sb.setLength(0);
        sb.append("before mybatis insert:");

        // 自动生成 createTime 属性值
        setPropertyIfHas("createTime", new Date(), metaObject);

        // 自动生成 updateTime 属性值
        setPropertyIfHas("updateTime", new Date(), metaObject);

        // 自动生成 deleted 属性值，默认 false
        setPropertyIfHas("deleted", false, metaObject);

        // 密码加密
//        String original = (String) getFieldValByName("password", metaObject);
//        if (original != null && original.length() > 0) {
//            setPropertyIfHas("password", passwordEncoder.encode(original), metaObject);
//        }

        logger.debug(sb.toString());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动更新 updateTime 属性值
        setPropertyIfHas("updateTime", new Date(), metaObject);

        // 修改密码加密
//        String originalForUpdate = (String) getFieldValByName("password", metaObject);
//        if (originalForUpdate != null && originalForUpdate.length() == 0) {
//            setPropertyIfHas("password", passwordEncoder.encode(originalForUpdate), metaObject);
//        }
    }

    private <V> void setPropertyIfHas(String propertyName, V value, MetaObject metaObject) {
        if (metaObject.hasSetter(propertyName)) {
            sb.append(" [fill " + propertyName + "] ");
            this.setFieldValByName(propertyName, value, metaObject);
        }
    }

}
