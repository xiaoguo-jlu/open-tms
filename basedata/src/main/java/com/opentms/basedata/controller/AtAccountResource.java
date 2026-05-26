package com.opentms.basedata.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Path("/api/v1/transfer/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AtAccountResource {

    @GET
    public Object list() {
        List<Map<String, String>> accounts = Arrays.asList(
            createAccount("ACC-001", "CNY", "中国银行-深圳分行", "660208888880000001"),
            createAccount("ACC-002", "USD", "中国银行-纽约分行", "660208888880000002"),
            createAccount("ACC-003", "EUR", "汇丰银行-法兰克福分行", "660208888880000003"),
            createAccount("ACC-004", "CNY", "工商银行-深圳分行", "660208888880000004"),
            createAccount("ACC-005", "USD", "花旗银行-纽约分行", "660208888880000005")
        );
        return accounts;
    }

    private Map<String, String> createAccount(String code, String currency, String bankName, String accountNo) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("code", code);
        map.put("currency", currency);
        map.put("bankName", bankName);
        map.put("accountNo", accountNo);
        return map;
    }
}
