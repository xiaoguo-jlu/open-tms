package com.opentms.dealing.controller;

import com.opentms.dealing.service.ImageService;
import com.opentms.dealing.vo.DealImageVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dealing/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/by-deal/{dealNumber}")
    public Result<List<DealImageVO>> getByDealNumber(@PathVariable String dealNumber) {
        List<DealImageVO> images = imageService.getImagesByDealNumber(dealNumber);
        return Result.success(images);
    }

    @GetMapping("/{dealNumber}/{version}")
    public Result<DealImageVO> getByDealNumberAndVersion(
            @PathVariable String dealNumber,
            @PathVariable Integer version) {
        DealImageVO image = imageService.getImageByDealNumberAndVersion(dealNumber, version);
        if (image == null) {
            return Result.notFound("Image not found");
        }
        return Result.success(image);
    }
}