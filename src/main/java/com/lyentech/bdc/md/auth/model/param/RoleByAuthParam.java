package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author YuYi
 * @create 2023/4/3
 * @create 9:18
 */
@Data
public class RoleByAuthParam {
    Long roleId;
    List<Long> authIds;
    Boolean isMax;
    Boolean isDelete;
    Long tenantId;
}
