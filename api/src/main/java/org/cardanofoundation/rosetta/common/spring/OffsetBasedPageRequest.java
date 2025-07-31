package org.cardanofoundation.rosetta.common.spring;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Custom Pageable implementation for offset-based pagination
 * instead of Spring's default page-based pagination.
 * 
 * This handles the semantic difference between:
 * - API offset/limit: "skip N records, return M records"
 * - Spring page/size: "get page N with M records per page"
 */
public class OffsetBasedPageRequest implements Pageable {
    
    private final long offset;
    private final int limit;
    private final Sort sort;
    
    public OffsetBasedPageRequest(long offset, int limit) {
        this(offset, limit, Sort.unsorted());
    }
    
    public OffsetBasedPageRequest(long offset, int limit, Sort sort) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be less than zero");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }
        if (sort == null) {
            throw new IllegalArgumentException("Sort must not be null");
        }
        
        this.offset = offset;
        this.limit = limit;
        this.sort = sort;
    }
    
    @Override
    public int getPageNumber() {
        // Calculate which page this offset falls into
        return (int) (offset / limit);
    }
    
    @Override
    public int getPageSize() {
        return limit;
    }
    
    @Override
    public long getOffset() {
        return offset;
    }
    
    @Override
    public Sort getSort() {
        return sort;
    }
    
    @Override
    public Pageable next() {
        return new OffsetBasedPageRequest(getOffset() + getPageSize(), getPageSize(), getSort());
    }
    
    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? 
            new OffsetBasedPageRequest(Math.max(0, getOffset() - getPageSize()), getPageSize(), getSort()) : 
            first();
    }
    
    @Override
    public Pageable first() {
        return new OffsetBasedPageRequest(0, getPageSize(), getSort());
    }
    
    // Note: withPage method may not be available in all Spring Data versions
    public Pageable withPage(int pageNumber) {
        return new OffsetBasedPageRequest((long) pageNumber * getPageSize(), getPageSize(), getSort());
    }
    
    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OffsetBasedPageRequest other)) return false;
        
        return this.offset == other.offset &&
               this.limit == other.limit &&
               this.sort.equals(other.sort);
    }
    
    @Override
    public int hashCode() {
        int result = Long.hashCode(offset);
        result = 31 * result + limit;
        result = 31 * result + sort.hashCode();

        return result;
    }
    
    @Override
    public String toString() {
        return String.format("OffsetBasedPageRequest[offset=%d, limit=%d, sort=%s]", 
                           offset, limit, sort);
    }
}