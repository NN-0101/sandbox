package com.sandbox.demo.mysql;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 主从复制 + 读写分离 测试
 *
 * @author 0101
 * @since 2026-05-02
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReadWriteSplittingTest {

    @Autowired
    private DataSource dataSource;

    /**
     * 在每个从库建 t_user 表
     * 注意：只建从库，主库在主从复制启动前已建或通过复制同步
     */
    @Test
    @Order(1)
    void test01_createTable() throws Exception {
        // 直接 jdbc 连从库建表，这里为了测试方便就用 ShardingSphere 数据源
        // 写操作会自动路由到主库
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 先删后建
            stmt.execute("DROP TABLE IF EXISTS t_user");

            String sql = """
                    CREATE TABLE t_user (
                        id INT NOT NULL,
                        name VARCHAR(32),
                        phone VARCHAR(255),
                        PRIMARY KEY (id)
                    )
                    """;
            stmt.execute(sql);
            log.info("✅ 建表成功（写操作 → 主库）");
        }
    }

    /**
     * 插入测试数据
     */
    @Test
    @Order(2)
    void test02_insert() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO t_user (id, name, phone) VALUES (?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, 1);
                ps.setString(2, "张三");
                ps.setString(3, "13800138001");
                ps.executeUpdate();

                ps.setInt(1, 2);
                ps.setString(2, "李四");
                ps.setString(3, "13800138002");
                ps.executeUpdate();
            }
            log.info("✅ 插入 2 条数据成功（写操作 → 主库）");
        }
    }

    /**
     * 查询验证（会看到日志中路由到从库）
     * 如果从库还没同步完成，这里可能查不到，需要等一下
     */
    @Test
    @Order(3)
    void test03_select() throws Exception {
        // 给主从同步一点时间（本机几乎瞬间同步，加 100ms 足够）
        Thread.sleep(100);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM t_user ORDER BY id")) {

            int count = 0;
            while (rs.next()) {
                count++;
                log.info("查询结果 → id:{}, name:{}, phone:{}",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"));
            }
            assertEquals(2, count, "应该查到 2 条数据");
            log.info("✅ 查询成功，共 {} 条数据（读操作 → 从库）", count);
        }
    }

    /**
     * 更新数据
     */
    @Test
    @Order(4)
    void test04_update() throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE t_user SET name = ? WHERE id = ?")) {
            ps.setString(1, "张三丰");
            ps.setInt(2, 1);
            int rows = ps.executeUpdate();
            assertEquals(1, rows);
            log.info("✅ 更新成功（写操作 → 主库）");
        }

        // 等同步
        Thread.sleep(100);

        // 从库验证
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT name FROM t_user WHERE id = 1")) {
            assertTrue(rs.next());
            assertEquals("张三丰", rs.getString("name"));
            log.info("✅ 从库数据已同步，name: {}", rs.getString("name"));
        }
    }

    /**
     * 删除数据
     */
    @Test
    @Order(5)
    void test05_delete() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate("DELETE FROM t_user WHERE id = 2");
            assertEquals(1, rows);
            log.info("✅ 删除成功（写操作 → 主库）");
        }

        // 等同步
        Thread.sleep(100);

        // 从库验证
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM t_user")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
            log.info("✅ 从库剩余 1 条数据");
        }
    }
}