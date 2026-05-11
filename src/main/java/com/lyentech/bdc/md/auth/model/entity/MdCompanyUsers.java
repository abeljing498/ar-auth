package com.lyentech.bdc.md.auth.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("md_company_users")
@Data
public class MdCompanyUsers {
    @TableId(value = "employee_id")
    private Long employeeId;
    @TableField(value = "email")
    private String email;
    @TableField(value = "post_id")
    private Integer postId;
    @TableField(value = "dept_name")
    private String deptName;
    @TableField(value = "office_name")
    private String officeName;
    @TableField(value = "org_name")
    private String orgName;
    @TableField(value = "post_name")
    private String postName;
    @TableField(value = "entry_time")
    private Date entryTime;
    @TableField(value = "name")
    private String name;
    @TableField(value = "company_name")
    private String companyName;
    @TableField(value = "is_on_job")
    private Boolean isOnJob;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
