package com.lyentech.bdc.md.auth.service.impl;

import com.lyentech.bdc.md.auth.dao.MdUserOperationLogMapper;
import com.lyentech.bdc.md.auth.model.entity.MdUserOperationLog;
import com.lyentech.bdc.md.auth.service.MdUserOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MdUserOperationLogServiceImpl implements MdUserOperationLogService {
    @Autowired
    private MdUserOperationLogMapper mdUserOperationLogMapper;
    @Override
    public void addUserOperationLog(MdUserOperationLog mdUserOperationLog) {
        mdUserOperationLogMapper.insert(mdUserOperationLog);
    }
}
