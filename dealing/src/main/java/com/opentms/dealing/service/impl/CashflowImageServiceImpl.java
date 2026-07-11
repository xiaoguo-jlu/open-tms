package com.opentms.dealing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.dealing.entity.Cashflow;
import com.opentms.dealing.entity.CashflowImage;
import com.opentms.dealing.mapper.CashflowImageMapper;
import com.opentms.dealing.service.CashflowImageService;
import com.opentms.dealing.vo.CashflowImageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 现金流镜像 Service 实现（v1.0 - 2026-07-11）
 *
 * <p>所有写操作必须由调用方在 @Transactional 中调用，
 * 以保证镜像失败时整体回滚（PRD §5.2 镜像写入时机）。</p>
 *
 * @author Open-TMS Backend Developer
 * @since 2026-07-11
 */
@Service
public class CashflowImageServiceImpl implements CashflowImageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String DELETED_NORMAL = "0";

    private final CashflowImageMapper cashflowImageMapper;

    public CashflowImageServiceImpl(CashflowImageMapper cashflowImageMapper) {
        this.cashflowImageMapper = cashflowImageMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String append(Cashflow cf, String imageType) {
        if (cf == null) {
            throw new IllegalArgumentException("cashflow 不能为空");
        }
        if (!StringUtils.hasText(imageType)) {
            throw new IllegalArgumentException("imageType 不能为空");
        }
        if (!StringUtils.hasText(cf.getCflowNumber())) {
            throw new IllegalArgumentException("cflowNumber 不能为空（无法定位被镜像的现金流）");
        }

        LocalDateTime now = LocalDateTime.now();
        String operator = StringUtils.hasText(cf.getUpdatedBy()) ? cf.getUpdatedBy()
                : (StringUtils.hasText(cf.getCreatedBy()) ? cf.getCreatedBy() : "system");

        CashflowImage img = new CashflowImage();
        BeanUtils.copyProperties(cf, img);
        img.setId(null); // 让 DB 自增
        img.setImageNumber(generateImageNumber());
        img.setImageType(imageType);
        img.setOperator(operator);
        img.setOperateAt(now);
        if (!StringUtils.hasText(img.getCreatedBy())) {
            img.setCreatedBy(operator);
        }
        if (img.getCreatedAt() == null) {
            img.setCreatedAt(now);
        }
        if (!StringUtils.hasText(img.getDeleted())) {
            img.setDeleted(DELETED_NORMAL);
        }
        if (img.getVersion() == null) {
            img.setVersion(1);
        }

        cashflowImageMapper.insert(img);
        return img.getImageNumber();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CashflowImageVO> listByDealNumber(String dealNumber, String imageType, int pageNum, int pageSize) {
        if (!StringUtils.hasText(dealNumber)) {
            throw new IllegalArgumentException("dealNumber 不能为空");
        }

        LambdaQueryWrapper<CashflowImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashflowImage::getDealNumber, dealNumber);
        if (StringUtils.hasText(imageType)) {
            wrapper.eq(CashflowImage::getImageType, imageType);
        }
        wrapper.orderByDesc(CashflowImage::getVersion);
        wrapper.orderByDesc(CashflowImage::getOperateAt);

        Page<CashflowImage> page = cashflowImageMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<CashflowImageVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashflowImageVO> listByDealNumberAndVersion(String dealNumber, Integer version) {
        if (!StringUtils.hasText(dealNumber) || version == null) {
            return List.of();
        }
        LambdaQueryWrapper<CashflowImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashflowImage::getDealNumber, dealNumber)
                .eq(CashflowImage::getVersion, version)
                .orderByAsc(CashflowImage::getId);
        return cashflowImageMapper.selectList(wrapper).stream().map(this::convertToVO).toList();
    }

    private CashflowImageVO convertToVO(CashflowImage img) {
        CashflowImageVO vo = new CashflowImageVO();
        BeanUtils.copyProperties(img, vo);
        return vo;
    }

    /**
     * 生成 imageNumber：IMG + yyyyMMdd + 4 位流水（与 tms_deals_image_t / tms_at_deals_image_t 一致）。
     */
    private String generateImageNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "IMG" + dateStr;
        LambdaQueryWrapper<CashflowImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(CashflowImage::getImageNumber, prefix)
                .orderByDesc(CashflowImage::getImageNumber)
                .last("LIMIT 1");
        CashflowImage last = cashflowImageMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getImageNumber() != null
                && last.getImageNumber().length() > prefix.length()) {
            try {
                String lastSeqStr = last.getImageNumber().substring(prefix.length());
                seq = Integer.parseInt(lastSeqStr) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%04d", seq);
    }
}