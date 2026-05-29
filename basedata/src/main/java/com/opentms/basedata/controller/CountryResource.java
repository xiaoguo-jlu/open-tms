package com.opentms.basedata.controller;

import com.opentms.basedata.entity.Country;
import com.opentms.basedata.service.CountryService;
import com.opentms.basedata.vo.CountryVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/api/v1/countries")
public class CountryResource {

    @Autowired
    private CountryService countryService;

    @GET
    public Object list() {
        try {
            List<CountryVO> list = countryService.listAll();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        try {
            return Result.success(countryService.queryPage(keyword, status, pageNum, pageSize));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") Long id) {
        try {
            CountryVO country = countryService.getCountryById(id);
            if (country == null) {
                return Result.notFound("Country not found");
            }
            return Result.success(country);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    public Object save(Country country) {
        try {
            countryService.saveCountry(country);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PUT
    @Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
    public Object update(Country country) {
        try {
            countryService.updateCountry(country);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            countryService.deleteCountry(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}