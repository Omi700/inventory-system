package com.example.inventory.api;

import com.example.inventory.api.dto.ApiResponse;
import com.example.inventory.api.dto.PageResponse;
import com.example.inventory.api.dto.ProductDetailResponse;
import com.example.inventory.api.dto.ProductListItemResponse;
import com.example.inventory.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/products
    //
    // 商品列表接口。
    //
    // 这个接口不需要在方法里手动判断 token，因为 SecurityConfig 已经配置了：
    // - /api/auth/login 放行
    // - /api/health 放行
    // - 其他接口都必须登录
    //
    // 所以请求进到这里时，说明 JWT 已经通过 JwtAuthenticationFilter 校验了。
    @GetMapping("/api/products")
    public ApiResponse<PageResponse<ProductListItemResponse>> listProducts(
        // required = false 表示这个参数不是必填。
        // 例如 /api/products 不传 keyword 也可以正常查询。
        @RequestParam(required = false) String keyword,

        // 商品分类，例如：咖啡豆、耗材、设备。
        @RequestParam(required = false) String category,

        // 商品状态：1 启用，0 停用。
        @RequestParam(required = false) Integer status,

        // 页码，从 1 开始。
        @RequestParam(required = false) Integer page,

        // 每页条数。
        @RequestParam(required = false) Integer pageSize
    ) {
        PageResponse<ProductListItemResponse> response = productService.listProducts(
            keyword,
            category,
            status,
            page,
            pageSize
        );

        return ApiResponse.success(response);
    }
    // GET /api/products/{id}
// 商品详情接口
    @GetMapping("/api/products/{id}")
    public ApiResponse<ProductDetailResponse> getProductDetail(@PathVariable Long id) {
        ProductDetailResponse response = productService.getProductDetail(id);
        return ApiResponse.success(response);
    }

    // 商品不存在时返回 40400
    @ExceptionHandler(NoSuchElementException.class)
    public ApiResponse<Void> handleNoSuchElementException(NoSuchElementException e) {
        return ApiResponse.fail(40400, e.getMessage());
    }
}
