package com.opentms.basedata.controller;

import com.opentms.basedata.entity.Country;
import com.opentms.basedata.service.CountryService;
import com.opentms.basedata.vo.CountryVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/api/v1/countries")
@Produces(MediaType.APPLICATION_JSON)
public class CountryResource {

    @Autowired
    private CountryService countryService;

    @GET
    public Object list() {
        try {
            return Result.success(countryService.listAll());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Error: " + e.getMessage());
        }
    }

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return Result.success(countryService.queryPage(keyword, status, pageNum, pageSize));
    }

    @GET
    @Path("/testpage")
    public Object testPage() {
        return Result.success(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") String idStr) {
        try {
            long id = Long.parseLong(idStr);
            if (id <= 0) {
                return Result.badRequest("ID must be positive");
            }
            CountryVO country = countryService.getCountryById(id);
            if (country == null) {
                return Result.notFound("Country not found");
            }
            return Result.success(country);
        } catch (NumberFormatException e) {
            return Result.badRequest("Invalid ID format");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Object save(Country country) {
        try {
            countryService.saveCountry(country);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object update(Country country) {
        try {
            countryService.updateCountry(country);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/delete/{id}")
    public Object delete(@PathParam("id") String idStr) {
        try {
            long id = Long.parseLong(idStr);
            if (id <= 0) {
                return Result.badRequest("ID must be positive");
            }
            countryService.deleteCountry(id);
            return Result.success();
        } catch (NumberFormatException e) {
            return Result.badRequest("Invalid ID format");
        } catch (RuntimeException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}