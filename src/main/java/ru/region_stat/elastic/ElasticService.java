package ru.region_stat.elastic;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.domain.dto.file.PublicationFileExtendedResultDto;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationEntityIdInfo;
import ru.region_stat.domain.repository.PublicationFileRepository;
import ru.region_stat.domain.repository.StatisticalPublicationRepository;
import ru.region_stat.service.StatisticalPublicationService;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Profile("!import")
public class ElasticService {

    private static final String DOCUMENT_INDEX = "documents";

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Resource
    private PublicationFileRepository publicationFileRepository;

    @Resource
    private ElasticRepository elasticRepository;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private StatisticalPublicationRepository statisticalPublicationRepository;

    @Resource
    private StatisticalPublicationService statisticalPublicationService;

    @Resource
    private SearchSourceBuilder searchSourceBuilder;

    @Resource
    private SearchRequest searchRequest;

    @Transactional
    public void addDocumentsToIndex(ElasticPublicationEntity elasticPublicationEntity) {
        elasticRepository.save(elasticPublicationEntity);
    }

    public void checkNotIndexedPublications() {

        String[] includeFields = new String[]{"publicationId"};
        String[] excludeFields = new String[]{"documentText"};

        SourceFilter sourceFilter = new FetchSourceFilter(includeFields, excludeFields);
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery())
                .withSourceFilter(sourceFilter).build();
        SearchHits<ElasticPublicationEntity> searchHits = elasticsearchOperations.search(query, ElasticPublicationEntity.class, IndexCoordinates.of(DOCUMENT_INDEX));

        List<UUID> elasticPublicationId = searchHits.getSearchHits().stream().map(s -> {
            return UUID.fromString(s.getContent().getPublicationId());
        }).collect(Collectors.toList());

        List<StatisticalPublicationEntityIdInfo> publicationEntitiesIds = new ArrayList<>();

        if (elasticPublicationId.size() == 0) {
            publicationEntitiesIds = statisticalPublicationRepository.getAllId();
        } else {

            publicationEntitiesIds = statisticalPublicationRepository.getAllIdNotInList(elasticPublicationId);
        }

        for (StatisticalPublicationEntityIdInfo id : publicationEntitiesIds) {
            statisticalPublicationService.getMapForRabbitAspect(id.getId());
        }
    }

    public List<Object> search(String word) {
        String source = "{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"multi_match\":{\"query\":\"" + word + "\",\"fields\":[\"documentText^3\",\"name^3\",\"documentText.raw^3\",\"name.raw^3\",\"documentText.search^1\",\"name.search^1\",\"documentText.autosuggest^1\",\"name.autosuggest^1\",\"documentText.english^1\",\"name.english^1\",\"publicationId.english^1\"],\"type\":\"cross_fields\",\"operator\":\"and\"}},{\"multi_match\":{\"query\":\"" + word + "\",\"fields\":[\"documentText^3\",\"name^3\",\"documentText.raw^3\",\"name.raw^3\",\"documentText.search^1\",\"name.search^1\",\"documentText.autosuggest^1\",\"name.autosuggest^1\",\"documentText.english^1\",\"name.english^1\"],\"type\":\"phrase_prefix\",\"operator\":\"and\"}}],\"minimum_should_match\":\"1\"}}]}}]}},\"highlight\":{\"pre_tags\":[\"<mark>\"],\"post_tags\":[\"</mark>\"],\"fields\":{\"documentText\":{},\"name\":{},\"name.raw\":{},\"documentText.search\":{},\"documentText.autosuggest\":{},\"name.autosuggest\":{},\"documentText.english\":{},\"name.english\":{}}},\"size\":50,\"_source\":{\"includes\":[\"*\"],\"excludes\":[\"documentText\", \"_class\"]},\"from\":0,\"sort\":[{\"_score\":{\"order\":\"desc\"}}]}";

        Request request = new Request("POST", "/documents/_search");

        request.setJsonEntity(source);

        Response response = null;
        try {
            response = restHighLevelClient.getLowLevelClient().performRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String responseBody = "{\"hits\":{\"hits\": []}}";

        if (response != null) {

            HttpEntity entity = response.getEntity();

            try {
                responseBody = EntityUtils.toString(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JSONObject jsonObject = new JSONObject(responseBody);

        JSONArray hits = jsonObject.getJSONObject("hits").getJSONArray("hits");

        List<Object> objects = hits.toList();

        return objects;
    }

    public ElasticResultSearchDto getElasticPublicationsByWords(String word, int from) {

        String[] words = word.toLowerCase().split(" ");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        Arrays.stream(words).map(s -> QueryBuilders.wildcardQuery("documentText", '*' + s + '*')).forEach(boolQueryBuilder::must);

        searchSourceBuilder.from(from);
        searchSourceBuilder.query(boolQueryBuilder);

        searchRequest.source(searchSourceBuilder);

        SearchResponse response = null;

        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        org.elasticsearch.search.SearchHits hits = response.getHits();

        List<Map<String, Object>> result = new ArrayList<>();
        List<UUID> ids = new ArrayList<>();

        for (SearchHit hit : hits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Text[] documentTextFragments = hit.getHighlightFields().get("documentText").getFragments();
            List<String> highlightFragments = Arrays.stream(documentTextFragments).map(Text::string).collect(Collectors.toList());
            sourceAsMap.put("highLight", highlightFragments);
            result.add(sourceAsMap);
            ids.add(UUID.fromString((String) sourceAsMap.get("fileId")));
        }

        List<PublicationFileExtendedResultDto> byFileIdIn = publicationFileRepository.getByFileIdIn(ids);

        long value = hits.getTotalHits().value;

        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put("totalResult", value);
        resultMap.put("from", from);
        resultMap.put("resultList", result);
        resultMap.put("resultPageSize", searchSourceBuilder.size());
        resultMap.put("currentPageResultSize", hits.getHits().length);

        result.forEach(m -> m.put("publicationFileExtendedResultDto", byFileIdIn.stream().filter(e -> {
            String id = e.getId();

            String fileId = (String) m.get("fileId");

            return id.equals(fileId);
        }).findFirst().orElse(null)));

        List<ElasticDocumentResultDto> resultDtos = result.stream().map(e -> ElasticDocumentResultDto.builder()
                .fileId((String) e.get("fileId"))
                .highLight((List<String>) e.get("highLight"))
                .name((String) e.get("name"))
                .publicationId((String) e.get("publicationId"))
                .publicationFileExtendedResultDto((PublicationFileExtendedResultDto) e.get("publicationFileExtendedResultDto"))
                .build()).collect(Collectors.toList());

        ElasticResultSearchDto elasticResultSearchDto = ElasticResultSearchDto.builder()
                .resultList(resultDtos)
                .currentPageResultSize((Integer) resultMap.get("currentPageResultSize"))
                .totalResult((Long) resultMap.get("totalResult"))
                .resultPageSize((Integer) resultMap.get("resultPageSize"))
                .from((Integer) resultMap.get("from"))
                .build();

        return elasticResultSearchDto;
    }
}