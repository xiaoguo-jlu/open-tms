package com.opentms.dealing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.dealing.vo.ActionVO;

public interface ActionService {

    Page<ActionVO> queryPage(String dealType, String actionType, String actionStatus, int pageNum, int pageSize);

    ActionVO getActionById(Long id);

    ActionVO getActionByDealNumber(String dealNumber);
}