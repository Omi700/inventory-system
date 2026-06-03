package com.example.inventory.repository;

import com.example.inventory.api.dto.ProductListItemResponse;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 查询商品列表。
    //
    // Repository 层只负责“怎么查数据库”，不负责权限、不负责业务判断。
    //
    // 参数说明：
    // - keyword：按商品编号或商品名称模糊搜索。
    // - category：按商品分类精确查询。
    // - status：按商品状态查询，1 启用，0 停用。
    // - page/pageSize：分页参数。
    public List<ProductListItemResponse> findPage(
        String keyword,
        String category,
        Integer status,
        int page,
        int pageSize
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT
              p.id,
              p.product_code,
              p.product_name,
              p.category,
              p.unit,
              p.safe_stock,
              p.status,
              p.created_at,
              pi.image_url AS main_image_url
            FROM product p
            LEFT JOIN product_image pi
              ON pi.product_id = p.id
             AND pi.is_main = 1
            WHERE 1 = 1
            """);

        // 用 List 保存 SQL 参数。
        // 这样可以继续使用 ? 占位符，避免把用户输入直接拼进 SQL，降低 SQL 注入风险。
        List<Object> params = new ArrayList<>();

        appendSearchConditions(sql, params, keyword, category, status);

        // 稳定排序：按 id 倒序，让新创建的商品排在前面。
        sql.append(" ORDER BY p.id DESC LIMIT ? OFFSET ?");

        // MySQL 分页：
        // LIMIT 表示取多少条。
        // OFFSET 表示跳过多少条。
        // 第 1 页 offset = 0；第 2 页 offset = pageSize；第 3 页 offset = pageSize * 2。
        int offset = (page - 1) * pageSize;
        params.add(pageSize);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            Timestamp createdAt = rs.getTimestamp("created_at");

            return new ProductListItemResponse(
                rs.getLong("id"),
                rs.getString("product_code"),
                rs.getString("product_name"),
                rs.getString("category"),
                rs.getString("unit"),
                rs.getInt("safe_stock"),
                rs.getInt("status"),
                rs.getString("main_image_url"),
                createdAt.toLocalDateTime().format(DATE_TIME_FORMATTER)
            );
        }, params.toArray());
    }

    // 查询符合筛选条件的商品总数。
    //
    // 前端分页时需要 total：
    // - records 是当前页数据。
    // - total 是所有符合条件的数据总量。
    // 前端拿 total 和 pageSize 才能算出总页数。
    public long count(
        String keyword,
        String category,
        Integer status
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM product p
            WHERE 1 = 1
            """);

        List<Object> params = new ArrayList<>();

        appendSearchConditions(sql, params, keyword, category, status);

        Long total = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return total == null ? 0 : total;
    }

    // 这个私有方法专门负责拼接查询条件。
    //
    // 为什么抽出来？
    // 因为 findPage() 和 count() 都需要同一套筛选条件。
    // 如果复制两遍，后面你修改筛选逻辑时很容易漏改其中一个。
    private void appendSearchConditions(
        StringBuilder sql,
        List<Object> params,
        String keyword,
        String category,
        Integer status
    ) {
        // keyword 为空字符串时，不应该参与查询。
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.product_code LIKE ? OR p.product_name LIKE ?)");

            // 模糊查询需要在关键字前后加 %
            // 例如 keyword = 咖啡，实际参数是 %咖啡%
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        if (category != null && !category.isBlank()) {
            sql.append(" AND p.category = ?");
            params.add(category.trim());
        }

        if (status != null) {
            sql.append(" AND p.status = ?");
            params.add(status);
        }
    }
}
