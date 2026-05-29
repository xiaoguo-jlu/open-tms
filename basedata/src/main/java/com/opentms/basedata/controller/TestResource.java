package com.opentms.basedata.controller;

import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Path("/api/v1/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {

    @GET
    public Object testList() {
        return Result.success("test");
    }

    @GET
    @Path("/page")
    public Object testPage() {
        Map<String, Object> page = new HashMap<>();
        page.put("current", 1);
        page.put("size", 10);
        page.put("total", 0);
        page.put("records", new java.util.ArrayList());
        return Result.success(page);
    }

    @GET
    @Path("/string")
    public Object testString() {
        return "Hello World";
    }

    @GET
    @Path("/countries")
    public Object testCountries() {
        return Result.success(new java.util.ArrayList<>());
    }
}