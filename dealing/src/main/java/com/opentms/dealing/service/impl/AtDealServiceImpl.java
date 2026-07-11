package com.opentms.dealing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.dealing.dto.AtDealDTO;
import com.opentms.dealing.entity.Action;
import com.opentms.dealing.entity.AtDeal;
import com.opentms.dealing.entity.AtDealImage;
import com.opentms.dealing.entity.Cashflow;
import com.opentms.dealing.entity.Deal;
import com.opentms.dealing.entity.DealMap;
import com.opentms.dealing.mapper.ActionMapper;
import com.opentms.dealing.mapper.AtDealImageMapper;
import com.opentms.dealing.mapper.AtDealMapper;
import com.opentms.dealing.mapper.CashflowMapper;
import com.opentms.dealing.mapper.DealMapMapper;
import com.opentms.dealing.mapper.DealMapper;
import com.opentms.dealing.service.AtDealService;
import com.opentms.dealing.service.BankAccountLookup;
import com.opentms.dealing.service.EntityNameLookup;
import com.opentms.dealing.vo.ActionVO;
import com.opentms.dealing.vo.AtDealImageVO;
import com.opentms.dealing.vo.AtDealVO;
import com.opentms.dealing.vo.CashflowVO;
import com.opentms.dealing.vo.DealMapVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AT 交易服务实现（v2.0 双腿设计）
 *
 * 核心规则：
 * 1) CREATE: Deal + AtDeal + Action(CREATE) + 4 DealMap + 2 Cashflow（不生成 AtDealImage）
 * 2) UPDATE: Action(UPDATE) + 软删旧 DealMap/Cashflow + 新建 4 DealMap + 2 Cashflow + AtDealImage(v+1)
 * 3) DELETE: Action(DELETE) + 软删 Deal/AtDeal + 级联软删 DealMap/Cashflow + AtDealImage(v+1)
 * 4) APPROVE/REJECT: 仅更新 Action.approval_status1/2，不改变 DealMap/Cashflow 任何状态
 */
@Service
public class AtDealServiceImpl implements AtDealService {

    private static final Logger log = LoggerFactory.getLogger(AtDealServiceImpl.class);

    private final DealMapper dealMapper;
    private final AtDealMapper atDealMapper;
    private final AtDealImageMapper atDealImageMapper;
    private final ActionMapper actionMapper;
    private final DealMapMapper dealMapMapper;
    private final CashflowMapper cashflowMapper;
    private final BankAccountLookup bankAccountLookup;
    /**
     * 现金流镜像服务 (v1.0 - 2026-07-11, 自动写 tms_cashflow_image_t)
     */
    private final com.opentms.dealing.service.CashflowImageService cashflowImageService;
    /**
     * 跨模块关联实体名称查询 (用于详情响应补全名称, 2026-07-05)
     */
    private final EntityNameLookup entityNameLookup;

    private static final String DEAL_TYPE = "AT";
    private static final String DEAL_STATUS_NEW = "New";

    private static final String ACTION_TYPE_CREATE = "CREATE";
    private static final String ACTION_TYPE_UPDATE = "UPDATE";
    private static final String ACTION_TYPE_DELETE = "DELETE";

    private static final String APPROVAL_STATUS_PENDING = "Pending";
    private static final String APPROVAL_STATUS_APPROVED = "Approved";
    private static final String APPROVAL_STATUS_REJECTED = "Rejected";

    private static final String IMAGE_TYPE_UPDATE = "UPDATE";
    private static final String IMAGE_TYPE_DELETE = "DELETE";

    private static final String EVENT_STATUS_ACTIVE = "Active";

    private static final String DEALMAP_TYPE_ACCOUNT_TRANSFER = "AccountTransfer";
    private static final String DEALMAP_TYPE_ACTUAL_CASHFLOW = "ActualCashflow";

    private static final String DIRECTION_OUTFLOW = "Outflow";
    private static final String DIRECTION_INFLOW = "Inflow";

    private static final String CFLOW_STATUS_CREATED = "Created";
    private static final String CFLOW_SOURCE_TYPE = "AT_DEAL";

    private static final String DEALMAP_NOT_DELETED = "0";
    private static final String DELETED = "1";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public AtDealServiceImpl(DealMapper dealMapper,
                             AtDealMapper atDealMapper,
                             AtDealImageMapper atDealImageMapper,
                             ActionMapper actionMapper,
                             DealMapMapper dealMapMapper,
                             CashflowMapper cashflowMapper,
                             BankAccountLookup bankAccountLookup,
                             com.opentms.dealing.service.CashflowImageService cashflowImageService,
                             EntityNameLookup entityNameLookup) {
        this.dealMapper = dealMapper;
        this.atDealMapper = atDealMapper;
        this.atDealImageMapper = atDealImageMapper;
        this.actionMapper = actionMapper;
        this.bankAccountLookup = bankAccountLookup;
        this.dealMapMapper = dealMapMapper;
        this.cashflowMapper = cashflowMapper;
        this.cashflowImageService = cashflowImageService;
        this.entityNameLookup = entityNameLookup;
    }

    // ======================== Page / Query ========================

    @Override
    public Page<AtDealVO> queryPage(String keyword, String transferType, String status,
                                    int pageNum, int pageSize) {
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deal::getDealType, DEAL_TYPE);

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Deal::getDealNumber, keyword);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Deal::getStatus, status);
        }
        wrapper.orderByDesc(Deal::getCreatedAt);

        Page<Deal> page = dealMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<AtDealVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<AtDealVO> voList = page.getRecords().stream()
                .map(d -> convertToVO(d, null))
                .filter(vo -> !StringUtils.hasText(transferType)
                        || transferType.equals(vo.getTransferType()))
                .toList();
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public AtDealVO getById(Long id) {
        Deal deal = dealMapper.selectById(id);
        if (deal == null) {
            return null;
        }
        return convertToVO(deal, null);
    }

    @Override
    public AtDealVO getByDealNumber(String dealNumber) {
        Deal deal = getDealByNumber(dealNumber);
        if (deal == null) {
            return null;
        }
        return convertToVO(deal, null);
    }

    @Override
    public List<DealMapVO> listDealMapsByDeal(String dealNumber) {
        LambdaQueryWrapper<DealMap> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealMap::getDealNumber, dealNumber)
                .eq(DealMap::getDeleted, DEALMAP_NOT_DELETED)
                .orderByAsc(DealMap::getEventDate)
                .orderByAsc(DealMap::getId);
        return dealMapMapper.selectList(wrapper).stream().map(this::convertDealMapToVO).toList();
    }

    @Override
    public List<CashflowVO> listCashflowsByDeal(String dealNumber) {
        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cashflow::getDealNumber, dealNumber)
                .eq(Cashflow::getDeleted, DEALMAP_NOT_DELETED)
                .orderByAsc(Cashflow::getCflowDate);
        return cashflowMapper.selectList(wrapper).stream().map(this::convertCashflowToVO).toList();
    }

    @Override
    public List<ActionVO> listActionsByDeal(String dealNumber) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getDealNumber, dealNumber)
                .eq(Action::getDeleted, DEALMAP_NOT_DELETED)
                .orderByAsc(Action::getCreatedAt);
        return actionMapper.selectList(wrapper).stream().map(this::convertActionToVO).toList();
    }

    @Override
    public List<AtDealImageVO> listImagesByDeal(String dealNumber) {
        LambdaQueryWrapper<AtDealImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AtDealImage::getDealNumber, dealNumber)
                .eq(AtDealImage::getDeleted, DEALMAP_NOT_DELETED)
                .orderByDesc(AtDealImage::getVersion);
        return atDealImageMapper.selectList(wrapper).stream().map(this::convertAtImageToVO).toList();
    }

    // ======================== Create ========================

    @Override
    @Transactional
    public boolean saveAtDeal(AtDealDTO dto) {
        validateAtDeal(dto);

        LocalDateTime now = LocalDateTime.now();
        String operator = dto.getOperator() != null ? dto.getOperator() : "system";

        // 1. generate dealNumber
        String dealNumber = generateAtDealNumber();

        // 2. INSERT Deal
        Deal deal = new Deal();
        deal.setDealNumber(dealNumber);
        deal.setDealType(DEAL_TYPE);
        deal.setManagementEntity(dto.getManagementEntity());
        deal.setDirection("Transfer");
        // AT 内部转账不直接关联外部对手方，counterparty_id / trader_id / instrument_id 设为 0 占位（满足 NOT NULL 约束）
        deal.setCounterpartyId(0L);
        deal.setTraderId(0L);
        deal.setInstrumentId(0L);
        deal.setAmount(dto.getSourceAmount());
        deal.setCurrency(dto.getSourceCurrency());
        deal.setDealDate(dto.getValueDate());
        deal.setValueDate(dto.getValueDate());
        deal.setStatus(DEAL_STATUS_NEW);
        deal.setDescription(dto.getPurpose());
        deal.setVersion(0);
        deal.setCreatedBy(operator);
        deal.setCreatedAt(now);
        deal.setUpdatedBy(operator);
        deal.setUpdatedAt(now);
        deal.setDeleted(DEALMAP_NOT_DELETED);
        dealMapper.insert(deal);

        // 3. INSERT AtDeal
        AtDeal atDeal = new AtDeal();
        BeanUtils.copyProperties(dto, atDeal);
        atDeal.setId(null);
        atDeal.setDealNumber(dealNumber);
        atDeal.setStatus(DEAL_STATUS_NEW);
        atDeal.setVersion(0);
        atDeal.setCreatedBy(operator);
        atDeal.setCreatedAt(now);
        atDeal.setUpdatedBy(operator);
        atDeal.setUpdatedAt(now);
        atDeal.setDeleted(DEALMAP_NOT_DELETED);
        atDealMapper.insert(atDeal);

        // 4. INSERT Action(CREATE)
        String actionNumber = generateActionNumber();
        Action action = new Action();
        action.setActionNumber(actionNumber);
        action.setDealNumber(dealNumber);
        action.setDealType(DEAL_TYPE);
        action.setActionType(ACTION_TYPE_CREATE);
        action.setActionStatus(APPROVAL_STATUS_PENDING);
        action.setOperator(operator);
        action.setOperateAt(now);
        action.setApprovalStatus1(APPROVAL_STATUS_PENDING);
        action.setApprovalStatus2(APPROVAL_STATUS_PENDING);
        action.setRemark(dto.getRemark());
        action.setCreatedBy(operator);
        action.setCreatedAt(now);
        action.setVersion(0);
        action.setDeleted(DEALMAP_NOT_DELETED);
        actionMapper.insert(action);

        // 5. INSERT 4 DealMap（2 AccountTransfer + 2 ActualCashflow）
        List<String> dealmapNumbers = insertAtDealMaps(dealNumber, actionNumber, dto, operator, now);

        // 6. INSERT 2 Cashflow（dealmap_number 指向 DMP#3 和 DMP#4）
        insertAtCashflows(dealNumber, dealmapNumbers, dto, operator, now);

        // 7. UPDATE Deal.latest_action_number
        deal.setLatestActionNumber(actionNumber);
        deal.setUpdatedBy(operator);
        deal.setUpdatedAt(LocalDateTime.now());
        dealMapper.updateById(deal);

        return true;
    }

    // ======================== Update ========================

    @Override
    @Transactional
    public boolean updateAtDeal(AtDealDTO dto) {
        if (dto.getId() == null && !StringUtils.hasText(dto.getDealNumber())) {
            throw new RuntimeException("id 或 dealNumber 必填");
        }
        validateAtDeal(dto);

        LocalDateTime now = LocalDateTime.now();
        String operator = dto.getOperator() != null ? dto.getOperator() : "system";

        Deal existingDeal = dto.getId() != null
                ? dealMapper.selectById(dto.getId())
                : getDealByNumber(dto.getDealNumber());
        if (existingDeal == null) {
            throw new RuntimeException("Deal 不存在");
        }
        String dealNumber = existingDeal.getDealNumber();

        AtDeal existingAtDeal = getAtDealByNumber(dealNumber);
        if (existingAtDeal == null) {
            throw new RuntimeException("AtDeal 不存在");
        }

        int newVersion = (existingDeal.getVersion() == null ? 0 : existingDeal.getVersion()) + 1;

        // 1. INSERT Action(UPDATE)
        String actionNumber = generateActionNumber();
        Action action = new Action();
        action.setActionNumber(actionNumber);
        action.setDealNumber(dealNumber);
        action.setDealType(DEAL_TYPE);
        action.setActionType(ACTION_TYPE_UPDATE);
        action.setActionStatus(APPROVAL_STATUS_PENDING);
        action.setOperator(operator);
        action.setOperateAt(now);
        action.setApprovalStatus1(APPROVAL_STATUS_PENDING);
        action.setApprovalStatus2(APPROVAL_STATUS_PENDING);
        action.setRemark(dto.getRemark());
        action.setCreatedBy(operator);
        action.setCreatedAt(now);
        action.setVersion(0);
        action.setDeleted(DEALMAP_NOT_DELETED);
        actionMapper.insert(action);

        // 2. INSERT AtDealImage(v+1) - 记录修改前字段快照
        AtDealImage atImage = new AtDealImage();
        BeanUtils.copyProperties(existingAtDeal, atImage);
        atImage.setId(null);
        atImage.setImageNumber(generateImageNumber());
        atImage.setVersion(newVersion);
        atImage.setImageType(IMAGE_TYPE_UPDATE);
        atImage.setOperator(operator);
        atImage.setOperateAt(now);
        atImage.setCreatedBy(operator);
        atImage.setCreatedAt(now);
        atImage.setDeleted(DEALMAP_NOT_DELETED);
        atDealImageMapper.insert(atImage);

        // 3. UPDATE Deal
        existingDeal.setManagementEntity(dto.getManagementEntity());
        existingDeal.setAmount(dto.getSourceAmount());
        existingDeal.setCurrency(dto.getSourceCurrency());
        existingDeal.setValueDate(dto.getValueDate());
        existingDeal.setDescription(dto.getPurpose());
        existingDeal.setLatestActionNumber(actionNumber);
        existingDeal.setUpdatedBy(operator);
        existingDeal.setUpdatedAt(now);
        existingDeal.setVersion(newVersion);
        dealMapper.updateById(existingDeal);

        // 4. UPDATE AtDeal
        BeanUtils.copyProperties(dto, existingAtDeal, "id", "dealNumber", "status", "version",
                "createdBy", "createdAt", "latestActionNumber");
        existingAtDeal.setStatus(DEAL_STATUS_NEW);
        existingAtDeal.setLatestActionNumber(actionNumber);
        existingAtDeal.setUpdatedBy(operator);
        existingAtDeal.setUpdatedAt(now);
        existingAtDeal.setVersion(newVersion);
        atDealMapper.updateById(existingAtDeal);

        // 5. 软删旧 DealMap
        LambdaUpdateWrapper<DealMap> dmDel = new LambdaUpdateWrapper<>();
        dmDel.eq(DealMap::getDealNumber, dealNumber)
                .eq(DealMap::getDeleted, DEALMAP_NOT_DELETED)
                .set(DealMap::getDeleted, DELETED)
                .set(DealMap::getUpdatedBy, operator)
                .set(DealMap::getUpdatedAt, now);
        dealMapMapper.update(null, dmDel);

        // 6. 软删旧 Cashflow（dealmap_number 指向旧 DealMap 的,v1.0: 先写 DELETE 镜像再软删）
        List<DealMap> oldDms = dealMapMapper.selectList(new LambdaQueryWrapper<DealMap>()
                .eq(DealMap::getDealNumber, dealNumber)
                .eq(DealMap::getDeleted, DELETED));
        if (!oldDms.isEmpty()) {
            List<String> oldNumbers = oldDms.stream().map(DealMap::getDealmapNumber).toList();
            List<Cashflow> oldCfs = cashflowMapper.selectList(
                    new LambdaQueryWrapper<Cashflow>()
                            .in(Cashflow::getDealmapNumber, oldNumbers)
                            .eq(Cashflow::getDeleted, DEALMAP_NOT_DELETED));
            for (Cashflow cf : oldCfs) {
                writeCashflowImageSafe(cf, "DELETE");
            }
            LambdaUpdateWrapper<Cashflow> cfDel = new LambdaUpdateWrapper<>();
            cfDel.in(Cashflow::getDealmapNumber, oldNumbers)
                    .eq(Cashflow::getDeleted, DEALMAP_NOT_DELETED)
                    .set(Cashflow::getDeleted, DELETED)
                    .set(Cashflow::getUpdatedBy, operator)
                    .set(Cashflow::getUpdatedAt, now);
            cashflowMapper.update(null, cfDel);
        }

        // 7. INSERT 新 DealMap × 4
        List<String> newDealmapNumbers = insertAtDealMaps(dealNumber, actionNumber, dto, operator, now);

        // 8. INSERT 新 Cashflow × 2
        insertAtCashflows(dealNumber, newDealmapNumbers, dto, operator, now);

        return true;
    }

    // ======================== Delete ========================

    @Override
    @Transactional
    public boolean deleteAtDeal(Long id) {
        Deal deal = dealMapper.selectById(id);
        if (deal == null) {
            throw new RuntimeException("Deal 不存在");
        }
        String dealNumber = deal.getDealNumber();
        LocalDateTime now = LocalDateTime.now();
        String operator = "system";

        AtDeal existingAtDeal = getAtDealByNumber(dealNumber);
        int newVersion = (deal.getVersion() == null ? 0 : deal.getVersion()) + 1;

        // 1. INSERT Action(DELETE)
        String actionNumber = generateActionNumber();
        Action action = new Action();
        action.setActionNumber(actionNumber);
        action.setDealNumber(dealNumber);
        action.setDealType(DEAL_TYPE);
        action.setActionType(ACTION_TYPE_DELETE);
        action.setActionStatus(APPROVAL_STATUS_PENDING);
        action.setOperator(operator);
        action.setOperateAt(now);
        action.setApprovalStatus1(APPROVAL_STATUS_PENDING);
        action.setApprovalStatus2(APPROVAL_STATUS_PENDING);
        action.setCreatedBy(operator);
        action.setCreatedAt(now);
        action.setVersion(0);
        action.setDeleted(DEALMAP_NOT_DELETED);
        actionMapper.insert(action);

        // 2. INSERT AtDealImage(v+1) - 记录删除前完整快照
        if (existingAtDeal != null) {
            AtDealImage atImage = new AtDealImage();
            BeanUtils.copyProperties(existingAtDeal, atImage);
            atImage.setId(null);
            atImage.setImageNumber(generateImageNumber());
            atImage.setVersion(newVersion);
            atImage.setImageType(IMAGE_TYPE_DELETE);
            atImage.setOperator(operator);
            atImage.setOperateAt(now);
            atImage.setCreatedBy(operator);
            atImage.setCreatedAt(now);
            atImage.setDeleted(DEALMAP_NOT_DELETED);
            atDealImageMapper.insert(atImage);
        }

        // 3. 软删 Deal
        deal.setStatus("Canceled");
        deal.setLatestActionNumber(actionNumber);
        deal.setUpdatedBy(operator);
        deal.setUpdatedAt(now);
        deal.setVersion(newVersion);
        deal.setDeleted(DELETED);
        dealMapper.updateById(deal);

        // 4. 软删 AtDeal
        if (existingAtDeal != null) {
            existingAtDeal.setUpdatedBy(operator);
            existingAtDeal.setUpdatedAt(now);
            existingAtDeal.setVersion(newVersion);
            existingAtDeal.setDeleted(DELETED);
            atDealMapper.updateById(existingAtDeal);
        }

        // 5. 级联软删 DealMap
        LambdaUpdateWrapper<DealMap> dmDel = new LambdaUpdateWrapper<>();
        dmDel.eq(DealMap::getDealNumber, dealNumber)
                .eq(DealMap::getDeleted, DEALMAP_NOT_DELETED)
                .set(DealMap::getDeleted, DELETED)
                .set(DealMap::getUpdatedBy, operator)
                .set(DealMap::getUpdatedAt, now);
        dealMapMapper.update(null, dmDel);

        // 6. 级联软删 Cashflow (v1.0: 先写 DELETE 镜像再软删)
        List<DealMap> allDms = dealMapMapper.selectList(new LambdaQueryWrapper<DealMap>()
                .eq(DealMap::getDealNumber, dealNumber));
        if (!allDms.isEmpty()) {
            List<String> numbers = allDms.stream().map(DealMap::getDealmapNumber).toList();
            // 先把全部未删 cashflow 拉出来,逐条写 DELETE 镜像
            List<Cashflow> cfsToDel = cashflowMapper.selectList(
                    new LambdaQueryWrapper<Cashflow>()
                            .in(Cashflow::getDealmapNumber, numbers)
                            .eq(Cashflow::getDeleted, DEALMAP_NOT_DELETED));
            for (Cashflow cf : cfsToDel) {
                writeCashflowImageSafe(cf, "DELETE");
            }
            LambdaUpdateWrapper<Cashflow> cfDel = new LambdaUpdateWrapper<>();
            cfDel.in(Cashflow::getDealmapNumber, numbers)
                    .eq(Cashflow::getDeleted, DEALMAP_NOT_DELETED)
                    .set(Cashflow::getDeleted, DELETED)
                    .set(Cashflow::getUpdatedBy, operator)
                    .set(Cashflow::getUpdatedAt, now);
            cashflowMapper.update(null, cfDel);
        }

        return true;
    }

    // ======================== Approve / Reject ========================

    @Override
    @Transactional
    public boolean approveAction(String actionNumber, String approver, String remark) {
        Action action = getActionByNumber(actionNumber);
        if (action == null) {
            throw new RuntimeException("Action 不存在");
        }
        LocalDateTime now = LocalDateTime.now();

        // 关键：仅更新 Action.approval_status1，不改变 DealMap / Cashflow 任何状态
        if (action.getApprover1() == null) {
            action.setApprover1(approver);
        }
        action.setApprovalStatus1(APPROVAL_STATUS_APPROVED);
        if (StringUtils.hasText(remark)) {
            action.setApprovalRemark(remark);
        }
        action.setOperator(approver);
        action.setOperateAt(now);
        action.setUpdatedBy(approver);
        action.setUpdatedAt(now);
        action.setVersion((action.getVersion() == null ? 0 : action.getVersion()) + 1);
        actionMapper.updateById(action);

        // 若 Action 对应 AT 业务，同时更新 Deal.status
        if (DEAL_TYPE.equals(action.getDealType())) {
            Deal deal = getDealByNumber(action.getDealNumber());
            if (deal != null) {
                deal.setStatus(APPROVAL_STATUS_APPROVED);
                deal.setUpdatedBy(approver);
                deal.setUpdatedAt(now);
                deal.setVersion((deal.getVersion() == null ? 0 : deal.getVersion()) + 1);
                dealMapper.updateById(deal);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean rejectAction(String actionNumber, String approver, String remark) {
        Action action = getActionByNumber(actionNumber);
        if (action == null) {
            throw new RuntimeException("Action 不存在");
        }
        LocalDateTime now = LocalDateTime.now();

        // 关键：仅更新 Action.approval_status1=Rejected，不改变 DealMap / Cashflow 状态
        if (action.getApprover1() == null) {
            action.setApprover1(approver);
        }
        action.setApprovalStatus1(APPROVAL_STATUS_REJECTED);
        if (StringUtils.hasText(remark)) {
            action.setApprovalRemark(remark);
        }
        action.setOperator(approver);
        action.setOperateAt(now);
        action.setUpdatedBy(approver);
        action.setUpdatedAt(now);
        action.setVersion((action.getVersion() == null ? 0 : action.getVersion()) + 1);
        actionMapper.updateById(action);

        if (DEAL_TYPE.equals(action.getDealType())) {
            Deal deal = getDealByNumber(action.getDealNumber());
            if (deal != null) {
                deal.setStatus(APPROVAL_STATUS_REJECTED);
                deal.setUpdatedBy(approver);
                deal.setUpdatedAt(now);
                deal.setVersion((deal.getVersion() == null ? 0 : deal.getVersion()) + 1);
                dealMapper.updateById(deal);
            }
        }
        return true;
    }

    // ======================== Internal: DealMap / Cashflow insert ========================

    /**
     * 插入 4 条 DealMap（双腿设计）
     * - DMP#1 AccountTransfer SOURCE
     * - DMP#2 AccountTransfer DESTINATION
     * - DMP#3 ActualCashflow SOURCE
     * - DMP#4 ActualCashflow DESTINATION
     * @return DealMap 编号列表（顺序对应 #1..#4）
     */
    private List<String> insertAtDealMaps(String dealNumber, String actionNumber,
                                          AtDealDTO dto, String operator, LocalDateTime now) {
        List<String> numbers = new ArrayList<>(4);
        numbers.add(insertSingleDealMap(dealNumber, actionNumber, dto, operator, now,
                DEALMAP_TYPE_ACCOUNT_TRANSFER, DIRECTION_OUTFLOW,
                dto.getSourceAmount(), dto.getSourceCurrency(),
                "AT Transfer - Source leg (AccountTransfer)"));
        numbers.add(insertSingleDealMap(dealNumber, actionNumber, dto, operator, now,
                DEALMAP_TYPE_ACCOUNT_TRANSFER, DIRECTION_INFLOW,
                dto.getDestAmount(), dto.getDestCurrency(),
                "AT Transfer - Destination leg (AccountTransfer)"));
        numbers.add(insertSingleDealMap(dealNumber, actionNumber, dto, operator, now,
                DEALMAP_TYPE_ACTUAL_CASHFLOW, DIRECTION_OUTFLOW,
                dto.getSourceAmount(), dto.getSourceCurrency(),
                "AT Transfer - Source leg (ActualCashflow)"));
        numbers.add(insertSingleDealMap(dealNumber, actionNumber, dto, operator, now,
                DEALMAP_TYPE_ACTUAL_CASHFLOW, DIRECTION_INFLOW,
                dto.getDestAmount(), dto.getDestCurrency(),
                "AT Transfer - Destination leg (ActualCashflow)"));
        return numbers;
    }

    private String insertSingleDealMap(String dealNumber, String actionNumber, AtDealDTO dto,
                                       String operator, LocalDateTime now,
                                       String eventType, String direction,
                                       BigDecimal amount, String currency, String description) {
        String dealmapNumber = generateDealMapNumber();
        DealMap dm = new DealMap();
        dm.setDealmapNumber(dealmapNumber);
        dm.setDealNumber(dealNumber);
        dm.setActionNumber(actionNumber);
        dm.setEventType(eventType);
        dm.setEventStatus(EVENT_STATUS_ACTIVE);
        dm.setAmount(amount);
        dm.setCurrency(currency);
        dm.setDirection(direction);
        dm.setEventDate(dto.getValueDate());
        dm.setValueDate(dto.getValueDate());
        dm.setIsReversal(DEALMAP_NOT_DELETED);
        dm.setDescription(description);
        dm.setCreatedBy(operator);
        dm.setCreatedAt(now);
        dm.setUpdatedBy(operator);
        dm.setUpdatedAt(now);
        dm.setVersion(0);
        dm.setDeleted(DEALMAP_NOT_DELETED);
        dealMapMapper.insert(dm);
        return dealmapNumber;
    }

    /**
     * 插入 2 条 Cashflow
     * 两条 Cashflow 的 dealmap_number 分别指向 DealMap 列表的 #3（SOURCE ActualCashflow）和 #4（DEST ActualCashflow）
     */
    private void insertAtCashflows(String dealNumber, List<String> dealmapNumbers,
                                   AtDealDTO dto, String operator, LocalDateTime now) {
        // Cashflow #1: SOURCE
        Cashflow cf1 = new Cashflow();
        cf1.setCflowNumber(generateCflowNumber());
        cf1.setDealNumber(dealNumber);
        cf1.setDealmapNumber(dealmapNumbers.get(2)); // #3 ActualCashflow SOURCE
        cf1.setManagementEntity(dto.getManagementEntity());
        cf1.setBankAccount(String.valueOf(dto.getSourceAccountId()));
        cf1.setCounterpartyAccount(String.valueOf(dto.getDestAccountId()));
        // v1.0: AT 内部转账,我方=源账户、对方=目标账户
        cf1.setBankAccountId(dto.getSourceAccountId());
        cf1.setCounterpartyBankAccountId(dto.getDestAccountId());
        cf1.setDirection(DIRECTION_OUTFLOW);
        cf1.setAmount(dto.getSourceAmount());
        cf1.setCurrency(dto.getSourceCurrency());
        cf1.setCflowDate(dto.getValueDate());
        cf1.setValueDate(dto.getValueDate());
        cf1.setSourceType(CFLOW_SOURCE_TYPE);
        cf1.setSourceRef(dealNumber);
        cf1.setStatus(CFLOW_STATUS_CREATED);
        cf1.setPurpose(dto.getPurpose());
        cf1.setCreatedBy(operator);
        cf1.setCreatedAt(now);
        cf1.setUpdatedBy(operator);
        cf1.setUpdatedAt(now);
        cf1.setVersion(0);
        cf1.setDeleted(DEALMAP_NOT_DELETED);
        cashflowMapper.insert(cf1);
        writeCashflowImageSafe(cf1, "CREATE");

        // Cashflow #2: DESTINATION
        Cashflow cf2 = new Cashflow();
        cf2.setCflowNumber(generateCflowNumber());
        cf2.setDealNumber(dealNumber);
        cf2.setDealmapNumber(dealmapNumbers.get(3)); // #4 ActualCashflow DESTINATION
        cf2.setManagementEntity(dto.getManagementEntity());
        cf2.setBankAccount(String.valueOf(dto.getDestAccountId()));
        cf2.setCounterpartyAccount(String.valueOf(dto.getSourceAccountId()));
        // v1.0: AT 内部转账,我方=目标账户、对方=源账户
        cf2.setBankAccountId(dto.getDestAccountId());
        cf2.setCounterpartyBankAccountId(dto.getSourceAccountId());
        cf2.setDirection(DIRECTION_INFLOW);
        cf2.setAmount(dto.getDestAmount());
        cf2.setCurrency(dto.getDestCurrency());
        cf2.setCflowDate(dto.getValueDate());
        cf2.setValueDate(dto.getValueDate());
        cf2.setSourceType(CFLOW_SOURCE_TYPE);
        cf2.setSourceRef(dealNumber);
        cf2.setStatus(CFLOW_STATUS_CREATED);
        cf2.setPurpose(dto.getPurpose());
        cf2.setCreatedBy(operator);
        cf2.setCreatedAt(now);
        cf2.setUpdatedBy(operator);
        cf2.setUpdatedAt(now);
        cf2.setVersion(0);
        cf2.setDeleted(DEALMAP_NOT_DELETED);
        cashflowMapper.insert(cf2);
        writeCashflowImageSafe(cf2, "CREATE");
    }

    /**
     * v1.0: 写 Cashflow 镜像（失败抛异常触发事务回滚）
     */
    private void writeCashflowImageSafe(Cashflow cf, String imageType) {
        try {
            cashflowImageService.append(cf, imageType);
        } catch (RuntimeException e) {
            log.error("[AtDealService] 写 {} 镜像失败,事务回滚: cflowNumber={}, err={}",
                    imageType, cf.getCflowNumber(), e.getMessage(), e);
            throw e;
        }
    }

    // ======================== Copy ========================

    @Override
    public AtDealDTO getCopyData(String dealNumber) {
        Deal deal = getDealByNumber(dealNumber);
        if (deal == null) {
            return null;
        }

        AtDeal atDeal = getAtDealByNumber(dealNumber);
        if (atDeal == null) {
            return null;
        }

        AtDealDTO dto = new AtDealDTO();
        // 不复制 id, dealNumber — 系统自动生成新编号
        dto.setTransferType(atDeal.getTransferType());
        dto.setManagementEntity(atDeal.getManagementEntity());
        dto.setSourceAccountId(atDeal.getSourceAccountId());
        dto.setDestAccountId(atDeal.getDestAccountId());
        dto.setSourceAmount(atDeal.getSourceAmount());
        dto.setDestAmount(atDeal.getDestAmount());
        dto.setSourceCurrency(atDeal.getSourceCurrency());
        dto.setDestCurrency(atDeal.getDestCurrency());
        dto.setExchangeRate(atDeal.getExchangeRate());
        dto.setValueDate(atDeal.getValueDate());
        dto.setPaymentMethod(atDeal.getPaymentMethod());
        dto.setPurpose(atDeal.getPurpose());
        dto.setRemark(deal.getRemark());
        // 补充源/目标账户的 accountNo，让前端 picker 能直接显示账号
        if (atDeal.getSourceAccountId() != null) {
            Map<String, Object> sourceSnap = bankAccountLookup.findAccountFull(atDeal.getSourceAccountId());
            if (sourceSnap != null) {
                dto.setSourceAccountNo(String.valueOf(sourceSnap.get("account_no")));
            }
        }
        if (atDeal.getDestAccountId() != null) {
            Map<String, Object> destSnap = bankAccountLookup.findAccountFull(atDeal.getDestAccountId());
            if (destSnap != null) {
                dto.setDestAccountNo(String.valueOf(destSnap.get("account_no")));
            }
        }
        // operator 留空，让用户自行填写
        dto.setOperator("");

        return dto;
    }

    // ======================== Validation ========================

    private void validateAtDeal(AtDealDTO dto) {
        if (dto.getSourceAccountId() == null || dto.getDestAccountId() == null) {
            throw new RuntimeException("源账户和目标账户不能为空");
        }
        if (dto.getSourceAccountId().equals(dto.getDestAccountId())) {
            throw new RuntimeException("源账户和目标账户不能相同");
        }
        // ====== 跨币种 / 跨管理主体 硬阻断 (硬规则) ======
        // 加载源/目标账户的币种和管理主体
        Map<String, Object> sourceSnap = bankAccountLookup.findAccountSnapshot(dto.getSourceAccountId());
        Map<String, Object> destSnap   = bankAccountLookup.findAccountSnapshot(dto.getDestAccountId());
        if (sourceSnap == null) {
            throw new RuntimeException("源账户不存在或已删除: id=" + dto.getSourceAccountId());
        }
        if (destSnap == null) {
            throw new RuntimeException("目标账户不存在或已删除: id=" + dto.getDestAccountId());
        }
        Object sourceCurrency = sourceSnap.get("currency");
        Object sourceMgmtObj  = sourceSnap.get("management_entity_id");
        Object destCurrency   = destSnap.get("currency");
        Object destMgmtObj    = destSnap.get("management_entity_id");

        // 跨币种直接拒绝 (哪怕填了汇率也不允许, AT 不做汇兑)
        if (sourceCurrency != null && destCurrency != null
                && !sourceCurrency.toString().equals(destCurrency.toString())) {
            throw new RuntimeException("AT 不支持跨币种转账 (源账户币种="
                    + sourceCurrency + ", 目标账户币种=" + destCurrency + "),请使用 FX 交易");
        }
        // 跨管理主体直接拒绝
        Long sourceMgmt = toLong(sourceMgmtObj);
        Long destMgmt   = toLong(destMgmtObj);
        if (sourceMgmt != null && destMgmt != null && !sourceMgmt.equals(destMgmt)) {
            throw new RuntimeException("AT 不支持跨管理主体转账 (源账户主体="
                    + sourceMgmt + ", 目标账户主体=" + destMgmt + ")");
        }

        if (dto.getSourceAmount() == null || dto.getSourceAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("源金额必须为正数");
        }
        if (dto.getDestAmount() == null || dto.getDestAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("目标金额必须为正数");
        }
        if (!StringUtils.hasText(dto.getSourceCurrency()) || !StringUtils.hasText(dto.getDestCurrency())) {
            throw new RuntimeException("币种不能为空");
        }
        // 跨币种时 exchangeRate 必填 (此处已硬阻断跨币种, 此校验通常不会触发, 但保留作防御)
        if (!dto.getSourceCurrency().equals(dto.getDestCurrency())
                && (dto.getExchangeRate() == null || dto.getExchangeRate().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new RuntimeException("跨币种必须填写汇率（>0）");
        }
        if (!StringUtils.hasText(dto.getTransferType())) {
            throw new RuntimeException("转账类型不能为空");
        }
        if (!StringUtils.hasText(dto.getPaymentMethod())) {
            throw new RuntimeException("支付方式不能为空");
        }
        if (dto.getValueDate() == null) {
            throw new RuntimeException("起息日不能为空");
        }
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ======================== Helper: queries ========================

    private Deal getDealByNumber(String dealNumber) {
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deal::getDealNumber, dealNumber);
        return dealMapper.selectOne(wrapper);
    }

    private AtDeal getAtDealByNumber(String dealNumber) {
        LambdaQueryWrapper<AtDeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AtDeal::getDealNumber, dealNumber);
        return atDealMapper.selectOne(wrapper);
    }

    private Action getActionByNumber(String actionNumber) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getActionNumber, actionNumber);
        return actionMapper.selectOne(wrapper);
    }

    // ======================== Helper: number generators ========================

    private String generateAtDealNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "AT" + dateStr;
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Deal::getDealNumber, prefix)
                .orderByDesc(Deal::getDealNumber)
                .last("LIMIT 1");
        Deal last = dealMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getDealNumber() != null
                && last.getDealNumber().length() > prefix.length()) {
            String lastSeq = last.getDealNumber().substring(prefix.length());
            try {
                seq = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private String generateActionNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "ACT" + dateStr;
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Action::getActionNumber, prefix)
                .orderByDesc(Action::getActionNumber)
                .last("LIMIT 1");
        Action last = actionMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getActionNumber() != null
                && last.getActionNumber().length() > prefix.length()) {
            String lastSeq = last.getActionNumber().substring(prefix.length());
            try {
                seq = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private String generateDealMapNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "DMP" + dateStr;
        LambdaQueryWrapper<DealMap> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(DealMap::getDealmapNumber, prefix)
                .orderByDesc(DealMap::getDealmapNumber)
                .last("LIMIT 1");
        DealMap last = dealMapMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getDealmapNumber() != null
                && last.getDealmapNumber().length() > prefix.length()) {
            String lastSeq = last.getDealmapNumber().substring(prefix.length());
            try {
                seq = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private String generateCflowNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "CF" + dateStr;
        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Cashflow::getCflowNumber, prefix)
                .orderByDesc(Cashflow::getCflowNumber)
                .last("LIMIT 1");
        Cashflow last = cashflowMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getCflowNumber() != null
                && last.getCflowNumber().length() > prefix.length()) {
            String lastSeq = last.getCflowNumber().substring(prefix.length());
            try {
                seq = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private String generateImageNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "IMG" + dateStr;
        LambdaQueryWrapper<AtDealImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(AtDealImage::getImageNumber, prefix)
                .orderByDesc(AtDealImage::getImageNumber)
                .last("LIMIT 1");
        AtDealImage last = atDealImageMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getImageNumber() != null
                && last.getImageNumber().length() > prefix.length()) {
            String lastSeq = last.getImageNumber().substring(prefix.length());
            try {
                seq = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    // ======================== Converters ========================

    private AtDealVO convertToVO(Deal deal, AtDeal atDeal) {
        AtDealVO vo = new AtDealVO();
        BeanUtils.copyProperties(deal, vo);
        if (atDeal == null) {
            atDeal = getAtDealByNumber(deal.getDealNumber());
        }
        if (atDeal != null) {
            vo.setTransferType(atDeal.getTransferType());
            vo.setManagementEntity(atDeal.getManagementEntity());
            vo.setSourceAccountId(atDeal.getSourceAccountId());
            vo.setDestAccountId(atDeal.getDestAccountId());
            vo.setSourceAmount(atDeal.getSourceAmount());
            vo.setDestAmount(atDeal.getDestAmount());
            vo.setSourceCurrency(atDeal.getSourceCurrency());
            vo.setDestCurrency(atDeal.getDestCurrency());
            vo.setExchangeRate(atDeal.getExchangeRate());
            vo.setValueDate(atDeal.getValueDate());
            vo.setPaymentMethod(atDeal.getPaymentMethod());
            vo.setPurpose(atDeal.getPurpose());
            vo.setStatus(atDeal.getStatus());
        }
        // 附加双腿 DealMap
        vo.setLegs(listDealMapsByDeal(deal.getDealNumber()));

        // ===== 2026-07-05: 详情响应补全关联实体名称 (前端 BaseDataPicker preloadRow 需求) =====
        populateEntityNames(vo, atDeal);

        return vo;
    }

    /**
     * AT 详情响应补全 *Name 字段 (失败容错: 单个 lookup 失败不影响其他字段)
     */
    private void populateEntityNames(AtDealVO vo, AtDeal atDeal) {
        String mgmtEntity = vo.getManagementEntity();
        // managementEntity (code 字符串反查)
        if (StringUtils.hasText(mgmtEntity)) {
            try {
                Map<String, Object> me = entityNameLookup.findManagementEntityByCode(mgmtEntity);
                vo.setManagementEntityName(formatCodeName(me, "code", "name"));
            } catch (Exception ignore) {
            }
        }
        if (atDeal != null) {
            try {
                Map<String, Object> src = entityNameLookup.findBankAccount(atDeal.getSourceAccountId());
                vo.setSourceAccountName(formatAccountName(src));
            } catch (Exception ignore) {
            }
            try {
                Map<String, Object> dst = entityNameLookup.findBankAccount(atDeal.getDestAccountId());
                vo.setDestAccountName(formatAccountName(dst));
            } catch (Exception ignore) {
            }
        }
    }

    private static String formatCodeName(Map<String, Object> row, String codeKey, String nameKey) {
        if (row == null) return null;
        Object code = row.get(codeKey);
        Object name = row.get(nameKey);
        StringBuilder sb = new StringBuilder();
        if (code != null && !code.toString().isEmpty()) sb.append(code);
        if (name != null && !name.toString().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("(").append(name).append(")");
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private static String formatAccountName(Map<String, Object> row) {
        if (row == null) return null;
        Object no = row.get("accountNo");
        Object name = row.get("accountName");
        StringBuilder sb = new StringBuilder();
        if (no != null && !no.toString().isEmpty()) sb.append(no);
        if (name != null && !name.toString().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("(").append(name).append(")");
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private DealMapVO convertDealMapToVO(DealMap dm) {
        DealMapVO vo = new DealMapVO();
        BeanUtils.copyProperties(dm, vo);
        return vo;
    }

    private CashflowVO convertCashflowToVO(Cashflow cf) {
        CashflowVO vo = new CashflowVO();
        BeanUtils.copyProperties(cf, vo);
        return vo;
    }

    private ActionVO convertActionToVO(Action a) {
        ActionVO vo = new ActionVO();
        BeanUtils.copyProperties(a, vo);
        return vo;
    }

    private AtDealImageVO convertAtImageToVO(AtDealImage img) {
        AtDealImageVO vo = new AtDealImageVO();
        BeanUtils.copyProperties(img, vo);
        return vo;
    }
}
