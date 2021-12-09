package ru.region_stat.configuration;

import org.apache.tika.Tika;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class BeanDefinition {
    private final String[] includeFields = new String[]{"fileId", "publicationId", "name" };
    private final String[] excludeFields = new String[]{"documentText" };
    private final int searchResultSize = 20;
    @Value("${elastic.host}")
    private String elasticHost;
    @Value("${elastic.preTag}")
    private String preTag;
    @Value("${elastic.postTag}")
    private String postTag;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private String port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public Tika tika() {
        Tika tika = new Tika();
        tika.setMaxStringLength(-1);
        return tika;
    }

    @Bean
    @Profile("!import")
    public RestHighLevelClient restHighLevelClient() {
        ClientConfiguration clientConfiguration
                = ClientConfiguration.builder()
                .connectedTo(elasticHost) //90.189.158.7:9200
                .withConnectTimeout(10000)
                .withSocketTimeout(60000)
                .build();

        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    @Profile("!import")
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(restHighLevelClient());
    }

    @Bean
    @Profile("!import")
    public HighlightBuilder.Field highlightTitle() {
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("documentText");

        return highlightTitle;
    }

    @Bean
    @Profile("!import")
    public AcknowledgedResponse updateSettingsResponse() {
        UpdateSettingsRequest request = new UpdateSettingsRequest("documents");
        String settingKey = "index.highlight.max_analyzed_offset";
        int settingValue = 20000000;
        Settings settings =
                Settings.builder()
                        .put(settingKey, settingValue)
                        .build();

        request.settings(settings);

        AcknowledgedResponse updateSettingsResponse = null;

        try {
            updateSettingsResponse =
                    restHighLevelClient().indices().putSettings(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return updateSettingsResponse;
    }

    @Bean
    @Profile("!import")
    public HighlightBuilder highlightBuilder() {
        HighlightBuilder highlightBuilder = new HighlightBuilder();

        HighlightBuilder.Field highlightTitle = highlightTitle();
        highlightBuilder.field(highlightTitle);
        highlightBuilder.numOfFragments(20);

        highlightBuilder.preTags(preTag);
        highlightBuilder.postTags(postTag);

        return highlightBuilder;
    }

    @Bean
    @Profile("!import")
    public SearchSourceBuilder searchSourceBuilder() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(searchResultSize);
        searchSourceBuilder.highlighter(highlightBuilder());
        searchSourceBuilder.fetchSource(includeFields, excludeFields);
        return searchSourceBuilder;
    }

    @Bean
    @Profile("!import")
    public SearchRequest searchRequest() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("documents");
        return searchRequest;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setHost(host);
        javaMailSender.setPort(Integer.parseInt(port));

        javaMailSender.setUsername(username);
        javaMailSender.setPassword(password);

        Properties props = javaMailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtps.ssl.checkserveridentity", true);
        props.put("mail.smtps.ssl.trust", "*");
        props.put("mail.smtp.ssl.enable", "true");

        return javaMailSender;
    }
}