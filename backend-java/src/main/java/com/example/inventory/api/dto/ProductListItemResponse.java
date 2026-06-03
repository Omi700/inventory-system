package com.example.inventory.api.dto;

// 商品列表中每一行返回给前端的数据。
//
// 注意它不是数据库实体类，而是“接口响应 DTO”：
// 1. 字段名使用前端喜欢的驼峰命名，比如 productCode。
// 2. 不暴露数据库里不需要给前端看的字段。
// 3. 可以放一些通过关联查询得到的字段，比如 mainImageUrl。
public class ProductListItemResponse {

    private final Long id;
    private final String productCode;
    private final String productName;
    private final String category;
    private final String unit;
    private final Integer safeStock;
    private final Integer status;
    private final String mainImageUrl;
    private final String createdAt;

    public ProductListItemResponse(
        Long id,
        String productCode,
        String productName,
        String category,
        String unit,
        Integer safeStock,
        Integer status,
        String mainImageUrl,
        String createdAt
    ) {
        this.id = id;
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.unit = unit;
        this.safeStock = safeStock;
        this.status = status;
        this.mainImageUrl = mainImageUrl;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public String getUnit() {
        return unit;
    }

    public Integer getSafeStock() {
        return safeStock;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
