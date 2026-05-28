package com.opentms.basedata.service.impl;

import com.opentms.basedata.entity.Country;
import com.opentms.basedata.service.CountryService;
import com.opentms.basedata.vo.CountryVO;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Service
public class CountryServiceImpl implements CountryService {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/opentms";
    private static final String JDBC_USER = "opentms";
    private static final String JDBC_PASS = "opentms123";

    @Override
    public List<CountryVO> listAll() {
        try {
            java.sql.DriverManager.registerDriver(new org.postgresql.Driver());
            java.sql.Connection conn = java.sql.DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, code, name, en_name, timezone, country_no, status FROM tms_country_t WHERE deleted = '0' AND status = '1' LIMIT 100");
            ResultSet rs = ps.executeQuery();
            List<CountryVO> result = new ArrayList<>();
            while (rs.next()) {
                CountryVO vo = new CountryVO();
                vo.setId(rs.getLong("id"));
                vo.setCode(rs.getString("code"));
                vo.setName(rs.getString("name"));
                vo.setEnName(rs.getString("en_name"));
                vo.setTimezone(rs.getString("timezone"));
                vo.setCountryNo(rs.getString("country_no"));
                vo.setStatus(rs.getString("status"));
                result.add(vo);
            }
            rs.close();
            ps.close();
            conn.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list countries: " + e.getMessage(), e);
        }
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<CountryVO> queryPage(String keyword, String status, int pageNum, int pageSize) {
        return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
    }

    @Override
    public CountryVO getCountryById(Long id) {
        try {
            java.sql.DriverManager.registerDriver(new org.postgresql.Driver());
            java.sql.Connection conn = java.sql.DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, code, name, en_name, timezone, country_no, status FROM tms_country_t WHERE id = ? AND deleted = '0'");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            CountryVO vo = null;
            if (rs.next()) {
                vo = new CountryVO();
                vo.setId(rs.getLong("id"));
                vo.setCode(rs.getString("code"));
                vo.setName(rs.getString("name"));
                vo.setEnName(rs.getString("en_name"));
                vo.setTimezone(rs.getString("timezone"));
                vo.setCountryNo(rs.getString("country_no"));
                vo.setStatus(rs.getString("status"));
            }
            rs.close();
            ps.close();
            conn.close();
            return vo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get country: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean saveCountry(Country country) {
        try {
            java.sql.DriverManager.registerDriver(new org.postgresql.Driver());
            java.sql.Connection conn = java.sql.DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO tms_country_t (code, name, en_name, timezone, country_no, status, created_by, created_at, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, '0')");
            ps.setString(1, country.getCode());
            ps.setString(2, country.getName());
            ps.setString(3, country.getEnName());
            ps.setString(4, country.getTimezone());
            ps.setString(5, country.getCountryNo());
            ps.setString(6, country.getStatus() != null ? country.getStatus() : "1");
            ps.setString(7, "system");
            ps.setObject(8, java.time.LocalDateTime.now());
            int rows = ps.executeUpdate();
            ps.close();
            conn.close();
            return rows > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save country: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateCountry(Country country) {
        if (country.getId() == null) {
            throw new RuntimeException("Country ID cannot be null");
        }
        try {
            java.sql.DriverManager.registerDriver(new org.postgresql.Driver());
            java.sql.Connection conn = java.sql.DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE tms_country_t SET name = ?, en_name = ?, timezone = ?, country_no = ?, status = ?, updated_by = ?, updated_at = ? WHERE id = ? AND deleted = '0'");
            ps.setString(1, country.getName());
            ps.setString(2, country.getEnName());
            ps.setString(3, country.getTimezone());
            ps.setString(4, country.getCountryNo());
            ps.setString(5, country.getStatus());
            ps.setString(6, "system");
            ps.setObject(7, java.time.LocalDateTime.now());
            ps.setLong(8, country.getId());
            int rows = ps.executeUpdate();
            ps.close();
            conn.close();
            return rows > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update country: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteCountry(Long id) {
        try {
            java.sql.DriverManager.registerDriver(new org.postgresql.Driver());
            java.sql.Connection conn = java.sql.DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE tms_country_t SET deleted = '1', updated_by = ?, updated_at = ? WHERE id = ? AND deleted = '0'");
            ps.setString(1, "system");
            ps.setObject(2, java.time.LocalDateTime.now());
            ps.setLong(3, id);
            int rows = ps.executeUpdate();
            ps.close();
            conn.close();
            if (rows == 0) {
                throw new RuntimeException("记录不存在");
            }
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete country: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        return false;
    }
}