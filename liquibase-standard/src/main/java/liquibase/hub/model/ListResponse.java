package liquibase.hub.model;

import java.util.List;

public class ListResponse<ContentType> {
    private Boolean first;
    private Boolean last;
    private Boolean empty;
    private Integer totalPages;
    private Integer totalElements;
    private Integer size;

    private List<ContentType> content;

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public List<ContentType> getContent() {
        return content;
    }

    public void setContent(List<ContentType> content) {
        this.content = content;
    }
}
