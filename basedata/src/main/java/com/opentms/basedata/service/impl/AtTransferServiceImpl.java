package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.AtTransferDTO;
import com.opentms.basedata.entity.AtTransfer;
import com.opentms.basedata.mapper.AtTransferMapper;
import com.opentms.basedata.service.AtTransferService;
import com.opentms.basedata.vo.AtTransferVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class AtTransferServiceImpl extends ServiceImpl<AtTransferMapper, AtTransfer> implements AtTransferService {

    @Override
    public IPage<AtTransferVO> queryPage(AtTransferDTO dto, int pageNum, int pageSize) {
        LambdaQueryWrapper<AtTransfer> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(AtTransfer::getTransferNo, dto.getKeyword())
                   .or()
                   .like(AtTransfer::getFromAccount, dto.getKeyword())
                   .or()
                   .like(AtTransfer::getToAccount, dto.getKeyword())
                   .or()
                   .like(AtTransfer::getApplicant, dto.getKeyword());
        }

        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(AtTransfer::getStatus, dto.getStatus());
        }

        wrapper.orderByDesc(AtTransfer::getCreatedAt);

        Page<AtTransfer> page = new Page<>(pageNum, pageSize);
        IPage<AtTransfer> result = this.page(page, wrapper);

        return result.convert(this::toVO);
    }

    @Override
    public AtTransferVO getById(Long id) {
        AtTransfer entity = super.getById(id);
        return entity != null ? toVO(entity) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AtTransferVO save(AtTransferDTO dto) {
        AtTransfer entity = toEntity(dto);
        entity.setTransferNo(generateTransferNo());
        entity.setStatus("New");
        this.save(entity);
        log.info("AT created id={} transferNo={}", entity.getId(), entity.getTransferNo());
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AtTransferVO update(AtTransferDTO dto) {
        AtTransfer entity = super.getById(dto.getId());
        if (entity == null) {
            throw new RuntimeException("AT记录不存在");
        }
        if (!"New".equals(entity.getStatus())) {
            throw new RuntimeException("仅New状态的记录可修改");
        }
        BeanUtils.copyProperties(dto, entity, "id", "transferNo", "status", "createdBy", "createdAt", "version", "deleted");
        this.updateById(entity);
        log.info("AT updated id={}", entity.getId());
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        AtTransfer entity = super.getById(id);
        if (entity == null) {
            throw new RuntimeException("AT记录不存在");
        }
        if (!"New".equals(entity.getStatus())) {
            throw new RuntimeException("仅New状态的记录可删除");
        }
        this.removeById(id);
        log.info("AT deleted id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        AtTransfer entity = super.getById(id);
        if (entity == null) {
            throw new RuntimeException("AT记录不存在");
        }
        if (!"New".equals(entity.getStatus())) {
            throw new RuntimeException("仅New状态的记录可提交");
        }
        entity.setStatus("Validated");
        this.updateById(entity);
        log.info("AT submitted id={} transferNo={}", id, entity.getTransferNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(Long id) {
        AtTransfer entity = super.getById(id);
        if (entity == null) {
            throw new RuntimeException("AT记录不存在");
        }
        if (!"Validated".equals(entity.getStatus())) {
            throw new RuntimeException("仅Validated状态的记录可执行");
        }
        entity.setStatus("SettlementInProcess");
        this.updateById(entity);
        log.info("AT executed id={} transferNo={}", id, entity.getTransferNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        AtTransfer entity = super.getById(id);
        if (entity == null) {
            throw new RuntimeException("AT记录不存在");
        }
        if ("Settled".equals(entity.getStatus()) || "Failed".equals(entity.getStatus()) || "Canceled".equals(entity.getStatus())) {
            throw new RuntimeException("当前状态不可取消");
        }
        entity.setStatus("Canceled");
        this.updateById(entity);
        log.info("AT canceled id={} transferNo={}", id, entity.getTransferNo());
    }

    private String generateTransferNo() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<AtTransfer> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(AtTransfer::getTransferNo, "TR" + today);
        long count = this.count(wrapper);
        return "TR" + today + String.format("%04d", count + 1);
    }

    private AtTransfer toEntity(AtTransferDTO dto) {
        AtTransfer entity = new AtTransfer();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    private AtTransferVO toVO(AtTransfer entity) {
        AtTransferVO vo = new AtTransferVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
