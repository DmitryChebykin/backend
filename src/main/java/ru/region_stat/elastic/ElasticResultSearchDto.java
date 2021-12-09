package ru.region_stat.elastic;

import lombok.*;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElasticResultSearchDto {
    private Long totalResult;
    private Integer from;
    private Integer resultPageSize;
    private Integer currentPageResultSize;
    private List<ElasticDocumentResultDto> resultList;
}