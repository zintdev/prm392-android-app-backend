package com.example.backend.dto;

import java.util.List;

public class BlogList {
    private List<BlogSummary> items;

    // paging meta
    private long totalElements;
    private int totalPages;
    private int page;       // current page index (0-based)
    private int size;       // page size
    private boolean first;
    private boolean last;

    public BlogList() {}

    public BlogList(List<BlogSummary> items, long totalElements, int totalPages,
                    int page, int size, boolean first, boolean last) {
        this.items = items;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
        this.first = first;
        this.last = last;
    }

    public List<BlogSummary> getItems() { return items; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }

    public void setItems(List<BlogSummary> items) { this.items = items; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public void setPage(int page) { this.page = page; }
    public void setSize(int size) { this.size = size; }
    public void setFirst(boolean first) { this.first = first; }
    public void setLast(boolean last) { this.last = last; }
}
