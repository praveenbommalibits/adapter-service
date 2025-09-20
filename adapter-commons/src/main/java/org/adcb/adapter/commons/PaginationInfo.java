package org.adcb.adapter.commons;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PaginationInfo {
    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Long totalElements;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Integer numberOfElements;
    private Map<String,String> links;
}
