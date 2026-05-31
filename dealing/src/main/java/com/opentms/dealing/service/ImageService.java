package com.opentms.dealing.service;

import com.opentms.dealing.vo.DealImageVO;

import java.util.List;

public interface ImageService {

    List<DealImageVO> getImagesByDealNumber(String dealNumber);

    DealImageVO getImageByDealNumberAndVersion(String dealNumber, Integer version);
}