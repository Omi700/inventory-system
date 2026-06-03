package com.example.inventory.api.dto;

import java.util.List;

// 通用分页响应对象。
// 接口文档里的分页格式是：
// {
//   "records": [],
//   "total": 100,
//   "page": 1,
//   "pageSize": 10
// }
//
// 这里用泛型 <T>，表示 records 里面可以放任意类型：
// - 商品列表时是 ProductListItemResponse
// - 仓库列表时可以是 WarehouseResponse
// - 用户列表时可以是 UserResponse
public class PageResponse<T> {

    // 当前页的数据列表
    private final List<T> records;

    // 符合查询条件的总记录数，不只是当前页数量
    private final long total;

    // 当前页码，从 1 开始
    private final int page;

    // 每页条数
    private final int pageSize;

    public PageResponse(List<T> records, long total, int page, int pageSize) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<T> getRecords() {
        return records;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }
}
