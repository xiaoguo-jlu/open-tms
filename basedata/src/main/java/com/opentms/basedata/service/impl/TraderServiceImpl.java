package com.opentms.basedata.service.impl;

import com.opentms.basedata.entity.Trader;
import com.opentms.basedata.service.TraderService;
import com.opentms.basedata.vo.TraderVO;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Service
public class TraderServiceImpl implements TraderService {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/opentms";
    private static final String JDBC_USER = "opentms";
    private static final String JDBC_PASS = "opentms123";

    @Override
    public List<TraderVO> listAll() {
        try {
            java.sql.DriverManager.registerDriver(new org.postgresql.Driver());
            java.sql.Connection conn = java.sql.DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, code, name, en_name, department, phone, email, status FROM tms_trader_t WHERE deleted = '0' AND status = '1' LIMIT 100");
            ResultSet rs = ps.executeQuery();
            List<TraderVO> result = new ArrayList<>();
            while (rs.next()) {
                TraderVO vo = new TraderVO();
                vo.setId(rs.getLong("id"));
                vo.setCode(rs.getString("code"));
                vo.setName(rs.getString("name"));
                vo.setEnName(rs.getString("en_name"));
                vo.setDepartment(rs.getString("department"));
                vo.setPhone(rs.getString("phone"));
                vo.setEmail(rs.getString("email"));
                vo.setStatus(rs.getString("status"));
                result.add(vo);
            }
            rs.close();
            ps.close();
            conn.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list traders: " + e.getMessage(), e);
        }
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<TraderVO> queryPage(String keyword, String status, int pageNum, int pageSize) {
        try {
            java.sql.DriverManager.registerDriver(new org.postgresql.Driver());
            java.sql.Connection conn = java.sql.DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);

            String countSql = "SELECT COUNT(*) FROM tms_trader_t WHERE deleted = '0'";
            String dataSql = "SELECT id, code, name, en_name, department, phone, email, status FROM tms_trader_t WHERE deleted = '0'";

            if (keyword != null && !keyword.isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                countSql += " AND (code LIKE '" + likeKeyword + "' OR name LIKE '" + likeKeyword + "')";
                dataSql += " AND (code LIKE '" + likeKeyword + "' OR name LIKE '" + likeKeyword + "')";
            }
            if (status != null && !status.isEmpty()) {
                countSql += " AND status = '" + status + "'";
                dataSql += " AND status = '" + status + "'";
            }

            PreparedStatement countPs = conn.prepareStatement(countSql);
            ResultSet countRs = countPs.executeQuery();
            long total = 0;
            if (countRs.next()) {
                total = countRs.getLong(1);
            }
            countRs.close();
            countPs.close();

            dataSql += " ORDER BY id DESC LIMIT " + pageSize + " OFFSET " + ((pageNum - 1) * pageSize);
            PreparedStatement dataPs = conn.prepareStatement(dataSql);
            ResultSet dataRs = dataPs.executeQuery();

            List<TraderVO> records = new ArrayList<>();
            while (dataRs.next()) {
                TraderVO vo = new TraderVO();
                vo.setId(dataRs.getLong("id"));
                vo.setCode(dataRs.getString("code"));
                vo.setName(dataRs.getString("name"));
                vo.setEnName(dataRs.getString("en_name"));
                vo.setDepartment(dataRs.getString("department"));
                vo.setPhone(dataRs.getString("phone"));
                vo.setEmail(dataRs.getString("email"));
                vo.setStatus(dataRs.getString("status"));
                records.add(vo);
            }
            dataRs.close();
            dataPs.close();
            conn.close();

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<TraderVO> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize, total);
            page.setRecords(records);
            return page;
        } catch (Exception e) {
            throw new RuntimeException("Failed to query traders: " + e.getMessage(), e);
        }
    }

    @Override
    public TraderVO getTraderById(Long id) {
        try {
            java.sql.DriverManager.registerDriver(new org.postgresql.Driver());
            java.sql.Connection conn = java.sql.DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, code, name, en_name, department, phone, email, status FROM tms_trader_t WHERE id = ? AND deleted = '0'");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            TraderVO vo = null;
            if (rs.next()) {
                vo = new TraderVO();
                vo.setId(rs.getLong("id"));
                vo.setCode(rs.getString("code"));
                vo.setName(rs.getString("name"));
                vo.setEnName(rs.getString("en_name"));
                vo.setDepartment(rs.getString("department"));
                vo.setPhone(rs.getString("phone"));
                vo.setEmail(rs.getString("email"));
                vo.setStatus(rs.getString("status"));
            }
            rs.close();
            ps.close();
            conn.close();
            return vo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get trader: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean saveTrader(Trader trader) {
        try {
            java.sql.DriverManager.registerDriver(new org.postgresql.Driver());
            java.sql.Connection conn = java.sql.DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO tms_trader_t (code, name, en_name, department, phone, email, status, created_by, created_at, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, '0')");
            ps.setString(1, trader.getCode());
            ps.setString(2, trader.getName());
            ps.setString(3, trader.getEnName());
            ps.setString(4, trader.getDepartment());
            ps.setString(5, trader.getPhone());
            ps.setString(6, trader.getEmail());
            ps.setString(7, trader.getStatus() != null ? trader.getStatus() : "1");
            ps.setString(8, "system");
            ps.setObject(9, java.time.LocalDateTime.now());
            int rows = ps.executeUpdate();
            ps.close();
            conn.close();
            return rows > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save trader: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateTrader(Trader trader) {
        if (trader.getId() == null) {
            throw new RuntimeException("Trader ID cannot be null");
        }
        try {
            java.sql.DriverManager.registerDriver(new org.postgresql.Driver());
            java.sql.Connection conn = java.sql.DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE tms_trader_t SET name = ?, en_name = ?, department = ?, phone = ?, email = ?, status = ?, updated_by = ?, updated_at = ? WHERE id = ? AND deleted = '0'");
            ps.setString(1, trader.getName());
            ps.setString(2, trader.getEnName());
            ps.setString(3, trader.getDepartment());
            ps.setString(4, trader.getPhone());
            ps.setString(5, trader.getEmail());
            ps.setString(6, trader.getStatus());
            ps.setString(7, "system");
            ps.setObject(8, java.time.LocalDateTime.now());
            ps.setLong(9, trader.getId());
            int rows = ps.executeUpdate();
            ps.close();
            conn.close();
            return rows > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update trader: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteTrader(Long id) {
        try {
            java.sql.DriverManager.registerDriver(new org.postgresql.Driver());
            java.sql.Connection conn = java.sql.DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE tms_trader_t SET deleted = '1', updated_by = ?, updated_at = ? WHERE id = ? AND deleted = '0'");
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
            throw new RuntimeException("Failed to delete trader: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        return false;
    }
}