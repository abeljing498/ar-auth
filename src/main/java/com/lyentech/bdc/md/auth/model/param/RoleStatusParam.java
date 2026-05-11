package com.lyentech.bdc.md.auth.model.param;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class RoleStatusParam {
    Long roleId;
    Boolean status;
}
