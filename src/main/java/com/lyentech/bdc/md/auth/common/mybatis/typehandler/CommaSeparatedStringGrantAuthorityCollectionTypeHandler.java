package com.lyentech.bdc.md.auth.common.mybatis.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * @author guolanren
 */
@MappedTypes(Collection.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class CommaSeparatedStringGrantAuthorityCollectionTypeHandler extends BaseTypeHandler<Collection<? extends GrantedAuthority>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Collection<? extends GrantedAuthority> parameter, JdbcType jdbcType) throws SQLException {
        Optional<String> commaSeparatedGrantedAuthority = parameter.parallelStream()
                .map(grantAuthority -> grantAuthority.getAuthority())
                .reduce((s1, s2) -> s1 + "," + s2);

        ps.setString(i, commaSeparatedGrantedAuthority.orElse(""));
    }

    @Override
    public Collection<? extends GrantedAuthority> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return AuthorityUtils.commaSeparatedStringToAuthorityList(rs.getString(columnName));
    }

    @Override
    public Collection<? extends GrantedAuthority> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return AuthorityUtils.commaSeparatedStringToAuthorityList(rs.getString(columnIndex));
    }

    @Override
    public Collection<? extends GrantedAuthority> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return AuthorityUtils.commaSeparatedStringToAuthorityList(cs.getString(columnIndex));
    }

}