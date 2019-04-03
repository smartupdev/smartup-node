package global.smartup.node.util;

import java.util.ArrayList;
import java.util.List;

public class Pagination<T>{

    private List<T> list;
    private Long rowCount;
    private Integer pageSize;
    private Integer pageNumb;
    private Integer pageCount;
    private Boolean hasNextPage;

    public Pagination() {
    }

    public static Pagination blank() {
        return new Pagination<>(0L, 0, 0, new ArrayList<>());
    }

    public static <T> Pagination<T> init(Long rowCount, Integer pageNumb, Integer pageSize, List<T> list) {
        return new Pagination<>(rowCount, pageNumb, pageSize, list);
    }

    public static int start(Long rowCount, int pageNumb, int pageSize) {
        int pageCount = pageCount(rowCount, pageSize);
        if (pageNumb > pageCount) {
            pageNumb = pageCount;
        }
        int start = (pageNumb - 1) * pageSize;
        if (start < 0) {
            start = 0;
        }
        return start;
    }

    public static Integer handlePageNumb(Integer pageNumb) {
        if (pageNumb == null || pageNumb <= 0) {
            return 1;
        }
        return pageNumb;
    }

    public static Integer handlePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            return 10;
        }
        return pageSize;
    }

    public boolean hasNextPage() {
        return hasNextPage;
    }

    private static int pageCount(Long rowCount, int pageSize) {
        int pageCount = rowCount > pageSize ? (int) (rowCount / pageSize + (rowCount % pageSize == 0 ? 0 : 1)) : 1;
        return pageCount;
    }

    private Pagination(Long rowCount, Integer pageNumb, Integer pageSize, List<T> list) {
        this.rowCount = rowCount;
        this.pageSize = pageSize;
        this.pageNumb = pageNumb;
        this.list = list;
        this.pageCount = pageCount(rowCount, pageSize);
        this.hasNextPage = pageNumb < pageCount ? true : false;
    }

    public Long getRowCount() {
        return rowCount;
    }

    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNumb() {
        return pageNumb;
    }

    public void setPageNumb(Integer pageNumb) {
        this.pageNumb = pageNumb;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Boolean getHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(Boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }
}
