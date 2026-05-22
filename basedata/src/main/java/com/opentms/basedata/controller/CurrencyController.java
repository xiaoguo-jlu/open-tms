package com.opentms.basedata.controller;

import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.service.CurrencyService;
import com.opentms.basedata.vo.CurrencyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
public class CurrencyController extends BasedataController<CurrencyService, CurrencyDTO, CurrencyVO> {

    private final CurrencyService currencyService;

    @Override
    protected CurrencyService getService() {
        return currencyService;
    }

    @Override
    protected String getEntityName() {
        return "币种";
    }

    @Override
    protected CurrencyDTO createDTO() {
        return new CurrencyDTO();
    }
}