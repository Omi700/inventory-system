package com.example.inventory.api.dto;

import java.util.List;

// 商品详情响应 DTO
// 对应接口文档 GET /api/products/{id}
public class ProductDetailResponse {

    private final Long id;
    private final String productCode;
    private final String productName;
    private final String category;
    private final String unit;
    private final Integer safeStock;
    private final Integer status;

    // 商品图片列表
    // 一个商品可以有多张图片，所以这里是 List。
    private final List<ImageInfo> images;

    public ProductDetailResponse(
            Long id,
            String productCode,
            String productName,
            String category,
            String unit,
            Integer safeStock,
            Integer status,
            List<ImageInfo> images
    ) {
        this.id = id;
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.unit = unit;
        this.safeStock = safeStock;
        this.status = status;
        this.images = images;
    }

    public Long getId() { return id; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public Integer getSafeStock() { return safeStock; }
    public Integer getStatus() { return status; }
    public List<ImageInfo> getImages() { return images; }

    // 商品图片响应对象
    public static class ImageInfo {
        private final Long id;
        private final String imageUrl;
        private final Integer isMain;
        private final Integer sortOrder;

        public ImageInfo(Long id, String imageUrl, Integer isMain, Integer sortOrder) {
            this.id = id;
            this.imageUrl = imageUrl;
            this.isMain = isMain;
            this.sortOrder = sortOrder;
        }

        public Long getId() { return id; }
        public String getImageUrl() { return imageUrl; }
        public Integer getIsMain() { return isMain; }
        public Integer getSortOrder() { return sortOrder; }
    }
}