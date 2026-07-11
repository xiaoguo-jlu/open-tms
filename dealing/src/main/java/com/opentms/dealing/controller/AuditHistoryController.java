package com.opentms.dealing.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.dealing.entity.AcDealImage;
import com.opentms.dealing.entity.AtDealImage;
import com.opentms.dealing.entity.DealImage;
import com.opentms.dealing.mapper.AcDealImageMapper;
import com.opentms.dealing.mapper.AtDealImageMapper;
import com.opentms.dealing.mapper.DealImageMapper;
import com.opentms.dealing.service.CashflowImageService;
import com.opentms.dealing.vo.AuditHistoryVersionDetailVO;
import com.opentms.dealing.vo.AuditHistoryVersionSummaryVO;
import com.opentms.dealing.vo.CashflowImageVO;
import com.opentms.dealing.vo.DealImageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 审计历史 Controller（v1.0 - 2026-07-11）
 *
 * <p>路径前缀：/api/v1/dealing/deals</p>
 *
 * <p>2 个端点：
 * <ul>
 *   <li>GET /{dealNumber}/versions — 列出所有版本摘要（分页）</li>
 *   <li>GET /{dealNumber}/versions/{version} — 详情（3 段式 LEFT JOIN）</li>
 * </ul>
 *
 * @author Open-TMS Backend Developer
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/api/v1/dealing/deals")
@RequiredArgsConstructor
public class AuditHistoryController {

    private final DealImageMapper dealImageMapper;
    private final AcDealImageMapper acDealImageMapper;
    private final AtDealImageMapper atDealImageMapper;
    private final CashflowImageService cashflowImageService;

    /**
     * 版本列表 — 按 dealNumber 分页查询 tms_deals_image_t。
     */
    @GetMapping("/{dealNumber}/versions")
    public Result<Page<AuditHistoryVersionSummaryVO>> listVersions(
            @PathVariable String dealNumber,
            @RequestParam(required = false) String imageType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        if (!StringUtils.hasText(dealNumber)) {
            return Result.badRequest("dealNumber 不能为空");
        }

        // 2026-07-11 BUG-001 修复:合并 dealImage(主表镜像) + cashflowImage(现金流镜像)
        // 仅查 dealImage 一边会让新建交易(只有 cashflow_image 有 CREATE 镜像)显示空
        LambdaQueryWrapper<DealImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealImage::getDealNumber, dealNumber);
        if (StringUtils.hasText(imageType)) {
            wrapper.eq(DealImage::getImageType, imageType);
        }
        wrapper.orderByDesc(DealImage::getVersion);
        wrapper.orderByDesc(DealImage::getOperateAt);

        Page<DealImage> page = dealImageMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AuditHistoryVersionSummaryVO> records = new ArrayList<>(page.getRecords().stream()
                .map(this::convertToSummary).toList());

        // 补 cashflow_image 来源的版本(可能 dealImage 没有,但 cashflow_image 有 CREATE)
        if (pageNum == 1 && pageSize >= 20) {
            List<AuditHistoryVersionSummaryVO> cfRecords = cashflowImageService
                    .listByDealNumber(dealNumber, imageType, 1, pageSize)
                    .getRecords().stream()
                    .map(cf -> {
                        AuditHistoryVersionSummaryVO vo = new AuditHistoryVersionSummaryVO();
                        vo.setId(cf.getId());
                        vo.setVersion(cf.getVersion() != null ? cf.getVersion() : 1);
                        vo.setImageType(cf.getImageType());
                        vo.setImageNumber(cf.getImageNumber());
                        vo.setOperator(cf.getOperator());
                        vo.setOperateAt(cf.getOperateAt());
                        vo.setChangeSummary("现金流 " + cf.getImageType() + " · " + cf.getCflowNumber());
                        return vo;
                    })
                    .filter(cf -> records.stream().noneMatch(r -> r.getVersion() != null && r.getVersion().equals(cf.getVersion())))
                    .toList();
            records.addAll(cfRecords);
        }

        Page<AuditHistoryVersionSummaryVO> voPage = new Page<>(page.getCurrent(), page.getSize(),
                (long) records.size());
        voPage.setRecords(records);
        return Result.success(voPage);
    }

    /**
     * 版本详情 — 3 段式 LEFT JOIN。
     *
     * <ul>
     *   <li>dealImage（主表镜像）— 必有</li>
     *   <li>specificDealImage（AC/AT 镜像）— 按 dealType 分支,可空</li>
     *   <li>cashflowImages（v1.0 之后的现金流镜像）— 列表,可空</li>
     * </ul>
     *
     * <p>FX 当前没有独立 image 表（只有 dealImage 主表镜像）,所以 specificDealImage 在 FX 场景下为 null。</p>
     */
    @GetMapping("/{dealNumber}/versions/{version}")
    public Result<AuditHistoryVersionDetailVO> getVersionDetail(
            @PathVariable String dealNumber,
            @PathVariable Integer version) {
        if (!StringUtils.hasText(dealNumber) || version == null) {
            return Result.badRequest("dealNumber 和 version 不能为空");
        }

        // ① 主表镜像
        DealImage dealImage = selectDealImage(dealNumber, version);
        if (dealImage == null) {
            return Result.notFound("未找到该版本的 dealImage: dealNumber=" + dealNumber + ", version=" + version);
        }

        AuditHistoryVersionDetailVO vo = new AuditHistoryVersionDetailVO();
        vo.setDealImage(toDealImageVO(dealImage));
        vo.setImageNumber(dealImage.getImageNumber());
        vo.setActionNumber(dealImage.getLatestActionNumber());

        // ② 子镜像（按 dealType 分支）
        DealImageVO specific = findSpecificDealImage(dealNumber, version, dealImage.getDealType());
        vo.setSpecificDealImage(specific);

        // ③ cashflow 镜像（v1.0 之前可能为空）
        List<CashflowImageVO> cfImages = cashflowImageService.listByDealNumberAndVersion(dealNumber, version);
        vo.setCashflowImages(cfImages != null ? cfImages : new ArrayList<>());

        return Result.success(vo);
    }

    // ============== Helpers ==============

    private DealImage selectDealImage(String dealNumber, Integer version) {
        LambdaQueryWrapper<DealImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealImage::getDealNumber, dealNumber)
                .eq(DealImage::getVersion, version)
                .orderByDesc(DealImage::getOperateAt)
                .last("LIMIT 1");
        return dealImageMapper.selectOne(wrapper);
    }

    private DealImageVO findSpecificDealImage(String dealNumber, Integer version, String dealType) {
        if (!StringUtils.hasText(dealType)) return null;
        switch (dealType) {
            case "AC" -> {
                LambdaQueryWrapper<AcDealImage> w = new LambdaQueryWrapper<>();
                w.eq(AcDealImage::getDealNumber, dealNumber).eq(AcDealImage::getVersion, version)
                        .orderByDesc(AcDealImage::getOperateAt).last("LIMIT 1");
                AcDealImage img = acDealImageMapper.selectOne(w);
                return img == null ? null : convertAcToDealImageVO(img);
            }
            case "AT" -> {
                LambdaQueryWrapper<AtDealImage> w = new LambdaQueryWrapper<>();
                w.eq(AtDealImage::getDealNumber, dealNumber).eq(AtDealImage::getVersion, version)
                        .orderByDesc(AtDealImage::getOperateAt).last("LIMIT 1");
                AtDealImage img = atDealImageMapper.selectOne(w);
                return img == null ? null : convertAtToDealImageVO(img);
            }
            default -> {
                // FX 暂无独立 image 表
                return null;
            }
        }
    }

    private AuditHistoryVersionSummaryVO convertToSummary(DealImage dealImage) {
        AuditHistoryVersionSummaryVO vo = new AuditHistoryVersionSummaryVO();
        BeanUtils.copyProperties(dealImage, vo);
        return vo;
    }

    private DealImageVO toDealImageVO(DealImage dealImage) {
        DealImageVO vo = new DealImageVO();
        BeanUtils.copyProperties(dealImage, vo);
        return vo;
    }

    private DealImageVO convertAcToDealImageVO(AcDealImage src) {
        DealImageVO vo = new DealImageVO();
        BeanUtils.copyProperties(src, vo);
        return vo;
    }

    private DealImageVO convertAtToDealImageVO(AtDealImage src) {
        DealImageVO vo = new DealImageVO();
        BeanUtils.copyProperties(src, vo);
        return vo;
    }
}