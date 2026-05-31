package com.opentms.dealing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.dealing.entity.Action;
import com.opentms.dealing.mapper.ActionMapper;
import com.opentms.dealing.service.ActionService;
import com.opentms.dealing.vo.ActionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ActionServiceImpl extends ServiceImpl<ActionMapper, Action> implements ActionService {

    @Override
    public Page<ActionVO> queryPage(String dealType, String actionType, String actionStatus, int pageNum, int pageSize) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dealType)) {
            wrapper.eq(Action::getDealType, dealType);
        }

        if (StringUtils.hasText(actionType)) {
            wrapper.eq(Action::getActionType, actionType);
        }

        if (StringUtils.hasText(actionStatus)) {
            wrapper.eq(Action::getActionStatus, actionStatus);
        }

        wrapper.orderByDesc(Action::getCreatedAt);

        Page<Action> page = page(new Page<>(pageNum, pageSize), wrapper);
        Page<ActionVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<ActionVO> voList = page.getRecords().stream().map(this::convertToVO).toList();
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ActionVO getActionById(Long id) {
        Action action = getById(id);
        return action != null ? convertToVO(action) : null;
    }

    @Override
    public ActionVO getActionByDealNumber(String dealNumber) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getDealNumber, dealNumber);
        Action action = getOne(wrapper);
        return action != null ? convertToVO(action) : null;
    }

    private ActionVO convertToVO(Action action) {
        ActionVO vo = new ActionVO();
        BeanUtils.copyProperties(action, vo);
        return vo;
    }
}