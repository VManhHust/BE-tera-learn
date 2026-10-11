package vn.tera.learn.common.pagination;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetPageRequest implements Pageable {
    private final long offset;
    private final int pageSize;

    public OffsetPageRequest(long offset, int pageSize) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be negative");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
        this.offset = offset;
        this.pageSize = pageSize;
    }

    @Override
    public int getPageNumber() {
        return (int) Math.min(Integer.MAX_VALUE, offset / pageSize);
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted();
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest(offset + pageSize, pageSize);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious()
                ? new OffsetPageRequest(Math.max(0, offset - pageSize), pageSize)
                : first();
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(0, pageSize);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
        return new OffsetPageRequest((long) pageNumber * pageSize, pageSize);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
}
