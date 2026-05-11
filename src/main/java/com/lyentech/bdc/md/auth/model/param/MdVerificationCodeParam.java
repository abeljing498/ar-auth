package com.lyentech.bdc.md.auth.model.param;

import java.io.Serializable;

/**
 * @author guolanren
 */
public class MdVerificationCodeParam implements Serializable {

    private static final long serialVersionUID = 9028767452972965740L;
    private String address;
    private String type;
    private String useTo;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUseTo() {
        return useTo;
    }

    public void setUseTo(String useTo) {
        this.useTo = useTo;
    }

}
