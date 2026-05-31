package com.opentms.dealing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.opentms.dealing.entity.AcDealImage;
import com.opentms.dealing.entity.DealImage;
import com.opentms.dealing.mapper.AcDealImageMapper;
import com.opentms.dealing.mapper.DealImageMapper;
import com.opentms.dealing.service.ImageService;
import com.opentms.dealing.vo.DealImageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {

    private final DealImageMapper dealImageMapper;
    private final AcDealImageMapper acDealImageMapper;

    public ImageServiceImpl(DealImageMapper dealImageMapper, AcDealImageMapper acDealImageMapper) {
        this.dealImageMapper = dealImageMapper;
        this.acDealImageMapper = acDealImageMapper;
    }

    @Override
    public List<DealImageVO> getImagesByDealNumber(String dealNumber) {
        LambdaQueryWrapper<DealImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealImage::getDealNumber, dealNumber);
        wrapper.orderByDesc(DealImage::getVersion);
        List<DealImage> dealImages = dealImageMapper.selectList(wrapper);

        List<DealImageVO> result = new ArrayList<>();
        for (DealImage dealImage : dealImages) {
            DealImageVO vo = new DealImageVO();
            BeanUtils.copyProperties(dealImage, vo);

            // Get corresponding AC deal image
            LambdaQueryWrapper<AcDealImage> acWrapper = new LambdaQueryWrapper<>();
            acWrapper.eq(AcDealImage::getDealNumber, dealNumber)
                    .eq(AcDealImage::getVersion, dealImage.getVersion());
            AcDealImage acDealImage = acDealImageMapper.selectOne(acWrapper);

            if (acDealImage != null) {
                vo.setBankAccountId(acDealImage.getBankAccountId());
                vo.setCounterpartyAccountId(acDealImage.getCounterpartyAccountId());
                vo.setPaymentMethod(acDealImage.getPaymentMethod());
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public DealImageVO getImageByDealNumberAndVersion(String dealNumber, Integer version) {
        LambdaQueryWrapper<DealImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealImage::getDealNumber, dealNumber)
               .eq(DealImage::getVersion, version);
        DealImage dealImage = dealImageMapper.selectOne(wrapper);

        if (dealImage == null) {
            return null;
        }

        DealImageVO vo = new DealImageVO();
        BeanUtils.copyProperties(dealImage, vo);

        // Get corresponding AC deal image
        LambdaQueryWrapper<AcDealImage> acWrapper = new LambdaQueryWrapper<>();
        acWrapper.eq(AcDealImage::getDealNumber, dealNumber)
                .eq(AcDealImage::getVersion, version);
        AcDealImage acDealImage = acDealImageMapper.selectOne(acWrapper);

        if (acDealImage != null) {
            vo.setBankAccountId(acDealImage.getBankAccountId());
            vo.setCounterpartyAccountId(acDealImage.getCounterpartyAccountId());
            vo.setPaymentMethod(acDealImage.getPaymentMethod());
        }

        return vo;
    }
}