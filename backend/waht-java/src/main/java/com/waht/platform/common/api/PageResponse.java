package com.waht.platform.common.api;

import java.util.List;

/**
 * 列表分页结果，统一向前端提供数据、页码和是否可继续翻页等元数据。
 *
 * @param <T> 列表元素类型
 */
public class PageResponse<T> {

    private final List<T> items;
    private final long page;
    private final long pageSize;
    private final long total;
    private final long totalPages;
    private final boolean hasPrevious;
    private final boolean hasNext;

    private PageResponse(List<T> items, long page, long pageSize, long total) {
        this.items = List.copyOf(items);
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = total == 0 ? 0 : (total - 1) / pageSize + 1;
        this.hasPrevious = page > 1;
        this.hasNext = page < totalPages;
    }

    public static <T> PageResponse<T> of(List<T> items, long page, long pageSize, long total) {
        return new PageResponse<>(items, page, pageSize, total);
    }

    public List<T> getItems() {
        return items;
    }

    public long getPage() {
        return page;
    }

    public long getPageSize() {
        return pageSize;
    }

    public long getTotal() {
        return total;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
