package com.example.inventory.service;

import com.example.inventory.api.dto.PageResponse;
import com.example.inventory.api.dto.ProductDetailResponse;
import com.example.inventory.api.dto.ProductListItemResponse;
import com.example.inventory.repository.ProductRepository;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 商品分页查询的业务入口。
    //
    // Service 层负责处理“业务规则”：
    // - page 没传或传错时，修正成 1。
    // - pageSize 没传或传错时，修正成 10。
    // - pageSize 不能无限大，否则前端一次查几万条会拖垮数据库。
    public PageResponse<ProductListItemResponse> listProducts(
        String keyword,
        String category,
        Integer status,
        Integer page,
        Integer pageSize
    ) {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);

        long total = productRepository.count(keyword, category, status);

        List<ProductListItemResponse> records = productRepository.findPage(
            keyword,
            category,
            status,
            safePage,
            safePageSize
        );

        return new PageResponse<>(records, total, safePage, safePageSize);
    }

    // 查询商品详情
    public ProductDetailResponse getProductDetail(Long id) {
        ProductDetailResponse product = productRepository.findDetailById(id)
                .orElseThrow(() -> new NoSuchElementException("商品不存在"));
        List<ProductDetailResponse.ImageInfo> images = productRepository.findImagesByProductId((id));
        return new ProductDetailResponse(
                product.getId(),
                product.getProductCode(),
                product.getProductName(),
                product.getCategory(),
                product.getUnit(),
                product.getSafeStock(),
                product.getStatus(),
                images
        );
    }

    private int normalizePage(Integer page) {
        // 页码从 1 开始。
        // 如果前端没传、传 0、传负数，都按第 1 页处理。
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private int normalizePageSize(Integer pageSize) {
        // 默认每页 10 条。
        if (pageSize == null || pageSize < 1) {
            return 10;
        }

        // 限制最大每页 100 条。
        // 这是后端保护数据库的常见做法。
        return Math.min(pageSize, 100);
    }
}
