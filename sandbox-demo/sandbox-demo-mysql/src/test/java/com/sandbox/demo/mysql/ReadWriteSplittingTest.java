package com.sandbox.demo.mysql;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
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
 * 读写分离 + 分库分表（phone 分库 + id 分表）测试
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
     * 建表（会在每个分片数据源自动创建 t_user_0 和 t_user_1）
     */
    @Test
    @Order(1)
    void test01_createTable() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

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
            log.info("✅ 建表成功（逻辑表 t_user → 物理表 t_user_0 / t_user_1）");
        }
    }

    /**
     * 批量插入 20 条数据，观察分库+分表路由
     */
    @Test
    @Order(2)
    void test02_batchInsert() throws Exception {
        String[] names = {"张三", "李四", "王五", "赵六", "孙七", "周八", "吴九", "郑十",
                "钱一", "陈二", "刘三", "黄四", "林五", "杨六", "叶七", "马八",
                "朱九", "胡十", "高十一", "罗十二"};
        String[] phones = {
                "13800000001", "13800000002", "13800000003", "13800000004", "13800000005",
                "13800000006", "13800000007", "13800000008", "13800000009", "13800000010",
                "13800000011", "13800000012", "13800000013", "13800000014", "13800000015",
                "13800000016", "13800000017", "13800000018", "13800000019", "13800000020"
        };

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO t_user (id, name, phone) VALUES (?, ?, ?)")) {

            for (int i = 0; i < 20; i++) {
                ps.setInt(1, i + 1);
                ps.setString(2, names[i]);
                ps.setString(3, phones[i]);
                ps.executeUpdate();
                log.info("插入 id:{}  name:{}  phone:{}", i + 1, names[i], phones[i]);
            }
            log.info("✅ 插入 20 条数据成功");
        }
    }

    /**
     * 精确查询（带 phone + id，精确路由到单个分片）
     */
    @Test
    @Order(3)
    void test03_selectExact() throws Exception {
        Thread.sleep(200);

        // 同时指定 phone 和 id，精准命中一个分片
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM t_user WHERE phone = ? AND id = ?")) {

            ps.setString(1, "13800000001");
            ps.setInt(2, 1);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("张三", rs.getString("name"));
                log.info("✅ 精确命中 → name: {}", rs.getString("name"));
            }
        }
    }

    /**
     * 只按 phone 查询（分库键，查出该库下两张表的数据）
     */
    @Test
    @Order(4)
    void test04_selectByPhone() throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM t_user WHERE phone = ?")) {

            ps.setString(1, "13800000006");
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("周八", rs.getString("name"));
                log.info("✅ 按 phone 查询成功 → name: {}", rs.getString("name"));
            }
        }
    }

    /**
     * 只按 id 查询（分表键，会广播到所有库）
     */
    @Test
    @Order(5)
    void test05_selectById() throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM t_user WHERE id = ?")) {

            ps.setInt(1, 3);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("王五", rs.getString("name"));
                log.info("✅ 按 id 查询成功 → name: {}", rs.getString("name"));
            }
        }
    }

    /**
     * 全量查询（广播到所有分片）
     */
    @Test
    @Order(6)
    void test06_selectAll() throws Exception {
        Thread.sleep(100);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM t_user ORDER BY id")) {

            int count = 0;
            log.info("========== 全量查询 ==========");
            while (rs.next()) {
                count++;
                log.info("id:{}  name:{}  phone:{}",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"));
            }
            log.info("========== 共 {} 条 ==========", count);
            assertEquals(20, count);
        }
    }

    /**
     * 物理表数据分布验证
     */
    @Test
    @Order(7)
    void test07_distribution() throws Exception {
        log.info("");
        log.info("========== 物理表数据分布 ==========");
        log.info("理论上 20 条数据分布到 4 张物理表：");
        log.info("  datasource0.t_user_0");
        log.info("  datasource0.t_user_1");
        log.info("  datasource1.t_user_0");
        log.info("  datasource1.t_user_1");
        log.info("观测日志中 Actual SQL 的 insert 路由即可验证");
        log.info("====================================");
    }

    /**
     * 更新（带双分片键，精确路由）
     */
    @Test
    @Order(8)
    void test08_update() throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE t_user SET name = ? WHERE phone = ? AND id = ?")) {
            ps.setString(1, "张三丰");
            ps.setString(2, "13800000001");
            ps.setInt(3, 1);
            assertEquals(1, ps.executeUpdate());
            log.info("✅ 更新成功（精确路由）");
        }
    }

    /**
     * 删除（带双分片键，精确路由）
     */
    @Test
    @Order(9)
    void test09_delete() throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM t_user WHERE phone = ? AND id = ?")) {
            ps.setString(1, "13800000020");
            ps.setInt(2, 20);
            assertEquals(1, ps.executeUpdate());
            log.info("✅ 删除成功（精确路由）");
        }
    }

    /**
     * 最终验证总行数
     */
    @Test
    @Order(10)
    void test10_finalCount() throws Exception {
        Thread.sleep(200);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM t_user")) {

            assertTrue(rs.next());
            assertEquals(19, rs.getInt(1));
            log.info("✅ 最终剩余 19 条数据");
        }
    }
}