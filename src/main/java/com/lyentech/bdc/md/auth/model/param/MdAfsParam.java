package com.lyentech.bdc.md.auth.model.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author guolanren
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MdAfsParam implements Serializable {

    private static final long serialVersionUID = -4427056233855742010L;

    private String ticket;
    private String randStr;

}
