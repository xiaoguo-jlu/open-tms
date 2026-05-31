package com.opentms.dealing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.dealing.dto.DealDTO;
import com.opentms.dealing.entity.*;
import com.opentms.dealing.mapper.*;
import com.opentms.dealing.service.DealService;
import com.opentms.dealing.vo.DealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DealServiceImpl extends ServiceImpl<DealMapper, Deal> implements DealService {

    private final AcDealMapper acDealMapper;
    private final ActionMapper actionMapper;
    private final DealImageMapper dealImageMapper;
    private final AcDealImageMapper acDealImageMapper;

    private static final String DEAL_STATUS_NEW = "New";
    private static final String DEAL_STATUS_SUBMITTED = "Submitted";
    private static final String DEAL_STATUS_APPROVED = "Approved";
    private static final String DEAL_STATUS_REJECTED = "Rejected";
    private static final String DEAL_STATUS_SETTLED = "Settled";
    private static final String DEAL_STATUS_CANCELED = "Canceled";

    private static final String ACTION_TYPE_CREATE = "CREATE";
    private static final String ACTION_TYPE_UPDATE = "UPDATE";
    private static final String ACTION_TYPE_DELETE = "DELETE";
    private static final String ACTION_TYPE_SUBMIT = "SUBMIT";
    private static final String ACTION_TYPE_APPROVE = "APPROVE";
    private static final String ACTION_TYPE_REJECT = "REJECT";
    private static final String ACTION_TYPE_EXECUTE = "EXECUTE";

    private static final String ACTION_STATUS_PENDING = "Pending";
    private static final String ACTION_STATUS_APPROVED = "Approved";
    private static final String ACTION_STATUS_REJECTED = "Rejected";
    private static final String ACTION_STATUS_EXECUTED = "Executed";

    private static final String APPROVAL_STATUS_PENDING = "Pending";
    private static final String APPROVAL_STATUS_APPROVED = "Approved";
    private static final String APPROVAL_STATUS_REJECTED = "Rejected";

    private static final String IMAGE_TYPE_CREATE = "CREATE";
    private static final String IMAGE_TYPE_UPDATE = "UPDATE";
    private static final String IMAGE_TYPE_DELETE = "DELETE";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public DealServiceImpl(AcDealMapper acDealMapper, ActionMapper actionMapper,
                          DealImageMapper dealImageMapper, AcDealImageMapper acDealImageMapper) {
        this.acDealMapper = acDealMapper;
        this.actionMapper = actionMapper;
        this.dealImageMapper = dealImageMapper;
        this.acDealImageMapper = acDealImageMapper;
    }

    @Override
    public Page<DealVO> queryPage(String keyword, String dealType, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Deal::getDealNumber, keyword);
        }

        if (StringUtils.hasText(dealType)) {
            wrapper.eq(Deal::getDealType, dealType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Deal::getStatus, status);
        }

        wrapper.orderByDesc(Deal::getCreatedAt);

        Page<Deal> page = page(new Page<>(pageNum, pageSize), wrapper);
        Page<DealVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<DealVO> voList = page.getRecords().stream().map(this::convertToVO).toList();
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public DealVO getDealById(Long id) {
        Deal deal = getById(id);
        if (deal == null) {
            return null;
        }
        return convertToVO(deal);
    }

    @Override
    public DealVO getDealByDealNumber(String dealNumber) {
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deal::getDealNumber, dealNumber);
        Deal deal = getOne(wrapper);
        return deal != null ? convertToVO(deal) : null;
    }

    @Override
    @Transactional
    public boolean saveDeal(DealDTO dealDTO) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Generate dealNumber
        String dealNumber = generateDealNumber();

        // 2. Create Deal
        Deal deal = new Deal();
        BeanUtils.copyProperties(dealDTO, deal);
        deal.setDealNumber(dealNumber);
        deal.setStatus(DEAL_STATUS_NEW);
        deal.setVersion(1);
        deal.setCreatedBy(dealDTO.getOperator());
        deal.setCreatedAt(now);
        deal.setUpdatedBy(dealDTO.getOperator());
        deal.setUpdatedAt(now);
        save(deal);

        // 3. Create AC Deal
        AcDeal acDeal = new AcDeal();
        acDeal.setDealNumber(dealNumber);
        acDeal.setBankAccountId(dealDTO.getBankAccountId());
        acDeal.setCounterpartyAccountId(dealDTO.getCounterpartyAccountId());
        acDeal.setPaymentMethod(dealDTO.getPaymentMethod());
        acDeal.setCreatedBy(dealDTO.getOperator());
        acDeal.setCreatedAt(now);
        acDeal.setUpdatedBy(dealDTO.getOperator());
        acDeal.setUpdatedAt(now);
        acDeal.setVersion(1);
        acDealMapper.insert(acDeal);

        // 4. Create Action
        String actionNumber = generateActionNumber();
        Action action = new Action();
        action.setActionNumber(actionNumber);
        action.setDealNumber(dealNumber);
        action.setDealType(dealDTO.getDealType());
        action.setActionType(ACTION_TYPE_CREATE);
        action.setActionStatus(ACTION_STATUS_PENDING);
        action.setOperator(dealDTO.getOperator());
        action.setOperateAt(now);
        action.setApprovalStatus1(APPROVAL_STATUS_PENDING);
        action.setApprovalStatus2(APPROVAL_STATUS_PENDING);
        action.setCreatedBy(dealDTO.getOperator());
        action.setCreatedAt(now);
        action.setVersion(1);
        actionMapper.insert(action);

               // 5. Create Deal Image
        String dealImageNumber = generateImageNumber();
        DealImage dealImage = new DealImage();
        dealImage.setImageNumber(dealImageNumber);
        dealImage.setDealNumber(dealNumber);
        dealImage.setDealType(dealDTO.getDealType());
        dealImage.setVersion(1);
        BeanUtils.copyProperties(dealDTO, dealImage);
        dealImage.setDealNumber(dealNumber); // Restore dealNumber after BeanUtils copy
        dealImage.setImageType(IMAGE_TYPE_CREATE);
        dealImage.setOperator(dealDTO.getOperator());
        dealImage.setOperateAt(now);
        dealImage.setCreatedBy(dealDTO.getOperator());
        dealImage.setCreatedAt(now);
        dealImageMapper.insert(dealImage);

        // 6. Create AC Deal Image
        String acDealImageNumber = generateImageNumber();
        AcDealImage acDealImage = new AcDealImage();
        acDealImage.setImageNumber(acDealImageNumber);
        acDealImage.setDealNumber(dealNumber);
        acDealImage.setVersion(1);
        acDealImage.setBankAccountId(dealDTO.getBankAccountId());
        acDealImage.setCounterpartyAccountId(dealDTO.getCounterpartyAccountId());
        acDealImage.setPaymentMethod(dealDTO.getPaymentMethod());
        acDealImage.setImageType(IMAGE_TYPE_CREATE);
        acDealImage.setOperator(dealDTO.getOperator());
        acDealImage.setOperateAt(now);
        acDealImage.setCreatedBy(dealDTO.getOperator());
        acDealImage.setCreatedAt(now);
        acDealImageMapper.insert(acDealImage);

        // 7. Update deal's latestActionNumber
        deal.setLatestActionNumber(actionNumber);
        updateById(deal);

        return true;
    }

    @Override
    @Transactional
    public boolean updateDeal(DealDTO dealDTO) {
        LocalDateTime now = LocalDateTime.now();
        String dealNumber = dealDTO.getDealNumber();

        // Get existing deal
        Deal existingDeal = getByDealNumber(dealNumber);
        if (existingDeal == null) {
            throw new RuntimeException("Deal not found");
        }

        // Get existing AC deal
        AcDeal existingAcDeal = getAcDealByDealNumber(dealNumber);
        if (existingAcDeal == null) {
            throw new RuntimeException("AC Deal not found");
        }

        // Get existing action (should be only one)
        Action existingAction = getActionByDealNumber(dealNumber);
        if (existingAction == null) {
            throw new RuntimeException("Action not found");
        }

        // Get current version
        int newVersion = existingDeal.getVersion() + 1;

        // 1. Update Action - change actionType to UPDATE
        existingAction.setActionType(ACTION_TYPE_UPDATE);
        existingAction.setOperator(dealDTO.getOperator());
        existingAction.setOperateAt(now);
        existingAction.setUpdatedBy(dealDTO.getOperator());
        existingAction.setUpdatedAt(now);
        existingAction.setVersion(newVersion);
        actionMapper.updateById(existingAction);

        // 2. Create new Deal Image (storing old values)
        String dealImageNumber = generateImageNumber();
        DealImage dealImage = new DealImage();
        dealImage.setImageNumber(dealImageNumber);
        dealImage.setDealNumber(dealNumber);
        dealImage.setDealType(existingDeal.getDealType());
        dealImage.setVersion(newVersion);
        BeanUtils.copyProperties(existingDeal, dealImage);
        dealImage.setImageType(IMAGE_TYPE_UPDATE);
        dealImage.setOperator(dealDTO.getOperator());
        dealImage.setOperateAt(now);
        dealImage.setCreatedBy(dealDTO.getOperator());
        dealImage.setCreatedAt(now);
        dealImageMapper.insert(dealImage);

        // 3. Create new AC Deal Image (storing old values)
        String acDealImageNumber = generateImageNumber();
        AcDealImage acDealImage = new AcDealImage();
        acDealImage.setImageNumber(acDealImageNumber);
        acDealImage.setDealNumber(dealNumber);
        acDealImage.setVersion(newVersion);
        BeanUtils.copyProperties(existingAcDeal, acDealImage);
        acDealImage.setImageType(IMAGE_TYPE_UPDATE);
        acDealImage.setOperator(dealDTO.getOperator());
        acDealImage.setOperateAt(now);
        acDealImage.setCreatedBy(dealDTO.getOperator());
        acDealImage.setCreatedAt(now);
        acDealImageMapper.insert(acDealImage);

        // 4. Update Deal
        BeanUtils.copyProperties(dealDTO, existingDeal);
        existingDeal.setUpdatedBy(dealDTO.getOperator());
        existingDeal.setUpdatedAt(now);
        existingDeal.setVersion(newVersion);
        existingDeal.setLatestActionNumber(existingAction.getActionNumber());
        updateById(existingDeal);

        // 5. Update AC Deal
        BeanUtils.copyProperties(dealDTO, existingAcDeal);
        existingAcDeal.setUpdatedBy(dealDTO.getOperator());
        existingAcDeal.setUpdatedAt(now);
        existingAcDeal.setVersion(newVersion);
        acDealMapper.updateById(existingAcDeal);

        return true;
    }

    @Override
    @Transactional
    public boolean deleteDeal(Long id) {
        Deal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found");
        }

        String dealNumber = deal.getDealNumber();
        LocalDateTime now = LocalDateTime.now();

        // Get existing action
        Action existingAction = getActionByDealNumber(dealNumber);
        if (existingAction != null) {
            // Update Action - change actionType to DELETE
            existingAction.setActionType(ACTION_TYPE_DELETE);
            existingAction.setOperator("system");
            existingAction.setOperateAt(now);
            existingAction.setUpdatedBy("system");
            existingAction.setUpdatedAt(now);
            existingAction.setVersion(deal.getVersion() + 1);
            actionMapper.updateById(existingAction);
        }

        // Update deal status and version
        deal.setStatus(DEAL_STATUS_CANCELED);
        deal.setVersion(deal.getVersion() + 1);
        deal.setUpdatedBy("system");
        deal.setUpdatedAt(now);
        updateById(deal);

        return true;
    }

    @Override
    @Transactional
    public boolean submitDeal(Long id, String operator) {
        Deal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found");
        }
        if (!DEAL_STATUS_NEW.equals(deal.getStatus())) {
            throw new RuntimeException("Only new deal can be submitted");
        }

        LocalDateTime now = LocalDateTime.now();
        String dealNumber = deal.getDealNumber();

        // Get existing action
        Action action = getActionByDealNumber(dealNumber);
        if (action == null) {
            throw new RuntimeException("Action not found");
        }

        // 1. Update Action status only (no new action, no new image)
        action.setActionType(ACTION_TYPE_SUBMIT);
        action.setActionStatus(ACTION_STATUS_PENDING);
        action.setOperator(operator);
        action.setOperateAt(now);
        action.setUpdatedBy(operator);
        action.setUpdatedAt(now);
        action.setVersion(action.getVersion() + 1);
        actionMapper.updateById(action);

        // 2. Update Deal status and version
        deal.setStatus(DEAL_STATUS_SUBMITTED);
        deal.setVersion(deal.getVersion() + 1);
        deal.setUpdatedBy(operator);
        deal.setUpdatedAt(now);
        deal.setLatestActionNumber(action.getActionNumber());
        updateById(deal);

        return true;
    }

    @Override
    @Transactional
    public boolean approveDeal(Long id, String approver, String approvalRemark) {
        Deal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found");
        }
        if (!DEAL_STATUS_SUBMITTED.equals(deal.getStatus())) {
            throw new RuntimeException("Only submitted deal can be approved");
        }

        LocalDateTime now = LocalDateTime.now();
        String dealNumber = deal.getDealNumber();

        // Get existing action
        Action action = getActionByDealNumber(dealNumber);
        if (action == null) {
            throw new RuntimeException("Action not found");
        }

        // Determine which approver level
        String currentApprover = action.getApprover1() == null ? "approver1" : (action.getApprover2() == null ? "approver2" : "already_approved");

        if ("approver1".equals(currentApprover)) {
            action.setApprover1(approver);
            action.setApprovalStatus1(APPROVAL_STATUS_APPROVED);
        } else if ("approver2".equals(currentApprover)) {
            action.setApprover2(approver);
            action.setApprovalStatus2(APPROVAL_STATUS_APPROVED);
        }

        if (approvalRemark != null) {
            action.setApprovalRemark(approvalRemark);
        }

        // If both approvals are done, update action status and deal status
        if (APPROVAL_STATUS_APPROVED.equals(action.getApprovalStatus1()) &&
            (action.getApprover2() == null || APPROVAL_STATUS_APPROVED.equals(action.getApprovalStatus2()))) {
            action.setActionType(ACTION_TYPE_APPROVE);
            action.setActionStatus(ACTION_STATUS_APPROVED);
            deal.setStatus(DEAL_STATUS_APPROVED);
        }

        action.setOperator(approver);
        action.setOperateAt(now);
        action.setUpdatedBy(approver);
        action.setUpdatedAt(now);
        action.setVersion(action.getVersion() + 1);
        actionMapper.updateById(action);

        // Update Deal status and version
        deal.setVersion(deal.getVersion() + 1);
        deal.setUpdatedBy(approver);
        deal.setUpdatedAt(now);
        deal.setLatestActionNumber(action.getActionNumber());
        updateById(deal);

        return true;
    }

    @Override
    @Transactional
    public boolean rejectDeal(Long id, String approver, String approvalRemark) {
        Deal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found");
        }
        if (!DEAL_STATUS_SUBMITTED.equals(deal.getStatus())) {
            throw new RuntimeException("Only submitted deal can be rejected");
        }

        LocalDateTime now = LocalDateTime.now();
        String dealNumber = deal.getDealNumber();

        // Get existing action
        Action action = getActionByDealNumber(dealNumber);
        if (action == null) {
            throw new RuntimeException("Action not found");
        }

        // Determine which approver level
        if (action.getApprover1() == null) {
            action.setApprover1(approver);
            action.setApprovalStatus1(APPROVAL_STATUS_REJECTED);
        } else {
            action.setApprover2(approver);
            action.setApprovalStatus2(APPROVAL_STATUS_REJECTED);
        }

        action.setActionType(ACTION_TYPE_REJECT);
        action.setActionStatus(ACTION_STATUS_REJECTED);
        action.setApprovalRemark(approvalRemark);
        action.setOperator(approver);
        action.setOperateAt(now);
        action.setUpdatedBy(approver);
        action.setUpdatedAt(now);
        action.setVersion(action.getVersion() + 1);
        actionMapper.updateById(action);

        // Update Deal status and version
        deal.setStatus(DEAL_STATUS_REJECTED);
        deal.setVersion(deal.getVersion() + 1);
        deal.setUpdatedBy(approver);
        deal.setUpdatedAt(now);
        deal.setLatestActionNumber(action.getActionNumber());
        updateById(deal);

        return true;
    }

    @Override
    @Transactional
    public boolean executeDeal(Long id, String operator) {
        Deal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found");
        }
        if (!DEAL_STATUS_APPROVED.equals(deal.getStatus())) {
            throw new RuntimeException("Only approved deal can be executed");
        }

        LocalDateTime now = LocalDateTime.now();
        String dealNumber = deal.getDealNumber();

        // Get existing action
        Action action = getActionByDealNumber(dealNumber);
        if (action == null) {
            throw new RuntimeException("Action not found");
        }

        // 1. Update Action status
        action.setActionType(ACTION_TYPE_EXECUTE);
        action.setActionStatus(ACTION_STATUS_EXECUTED);
        action.setOperator(operator);
        action.setOperateAt(now);
        action.setUpdatedBy(operator);
        action.setUpdatedAt(now);
        action.setVersion(action.getVersion() + 1);
        actionMapper.updateById(action);

        // 2. Update Deal status and version
        deal.setStatus(DEAL_STATUS_SETTLED);
        deal.setVersion(deal.getVersion() + 1);
        deal.setUpdatedBy(operator);
        deal.setUpdatedAt(now);
        deal.setLatestActionNumber(action.getActionNumber());
        updateById(deal);

        return true;
    }

    // ==================== Helper Methods ====================

    private Deal getByDealNumber(String dealNumber) {
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deal::getDealNumber, dealNumber);
        return getOne(wrapper);
    }

    private AcDeal getAcDealByDealNumber(String dealNumber) {
        LambdaQueryWrapper<AcDeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AcDeal::getDealNumber, dealNumber);
        return acDealMapper.selectOne(wrapper);
    }

    private Action getActionByDealNumber(String dealNumber) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getDealNumber, dealNumber);
        return actionMapper.selectOne(wrapper);
    }

    private DealVO convertToVO(Deal deal) {
        DealVO vo = new DealVO();
        BeanUtils.copyProperties(deal, vo);

        // Get AC deal info
        AcDeal acDeal = getAcDealByDealNumber(deal.getDealNumber());
        if (acDeal != null) {
            vo.setBankAccountId(acDeal.getBankAccountId());
            vo.setCounterpartyAccountId(acDeal.getCounterpartyAccountId());
            vo.setPaymentMethod(acDeal.getPaymentMethod());
        }

        return vo;
    }

    private String generateDealNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "DEAL" + dateStr;
        // Query max sequence for today
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Deal::getDealNumber, prefix)
               .orderByDesc(Deal::getDealNumber)
               .last("LIMIT 1");
        Deal lastDeal = getOne(wrapper);
        int seq = 1;
        if (lastDeal != null) {
            String lastNo = lastDeal.getDealNumber();
            String lastSeqStr = lastNo.substring(prefix.length());
            seq = Integer.parseInt(lastSeqStr) + 1;
        }
        return prefix + String.format("%04d", seq);
    }

    private String generateActionNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "ACT" + dateStr;
        // Query max sequence for today
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Action::getActionNumber, prefix)
               .orderByDesc(Action::getActionNumber)
               .last("LIMIT 1");
        Action lastAction = actionMapper.selectOne(wrapper);
        int seq = 1;
        if (lastAction != null) {
            String lastNo = lastAction.getActionNumber();
            String lastSeqStr = lastNo.substring(prefix.length());
            seq = Integer.parseInt(lastSeqStr) + 1;
        }
        return prefix + String.format("%04d", seq);
    }

    private String generateImageNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "IMG" + dateStr;
        // Query max sequence for today
        LambdaQueryWrapper<DealImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(DealImage::getImageNumber, prefix)
               .orderByDesc(DealImage::getImageNumber)
               .last("LIMIT 1");
        DealImage lastImage = dealImageMapper.selectOne(wrapper);
        int seq = 1;
        if (lastImage != null) {
            String lastNo = lastImage.getImageNumber();
            String lastSeqStr = lastNo.substring(prefix.length());
            seq = Integer.parseInt(lastSeqStr) + 1;
        }
        return prefix + String.format("%04d", seq);
    }
}