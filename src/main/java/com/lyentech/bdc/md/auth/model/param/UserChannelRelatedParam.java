package com.lyentech.bdc.md.auth.model.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lyentech.bdc.md.auth.model.entity.UserChannelRelated;
import lombok.Data;

import java.util.List;
@Data
public class UserChannelRelatedParam {

    private Long groupId;

    private Long channelId;

    private String appId;
    private Long tenantId;
    List<UserChannelRelated> list;
}
