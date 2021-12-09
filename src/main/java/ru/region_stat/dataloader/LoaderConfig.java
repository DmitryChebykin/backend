package ru.region_stat.dataloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import java.io.IOException;

@Configuration
@Profile("import")
public class LoaderConfig {

    public static final String APPLICATION_JSON_ODATA_VERBOSE = "application/json;odata=verbose";
    @Value("${sharepoint.username}")
    private String username;
    @Value("${sharepoint.password}")
    private char[] password;
    @Value("${sharepoint.domain}")
    private String domain;

    @Bean
    public HttpClient httpClient() {

        String authUri = "http://" + domain + "/Lists/List2/AllItems.aspx";

        NTCredentials credentials = new NTCredentials(username, String.valueOf(password), null, null);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();

        credsProvider.setCredentials(AuthScope.ANY, credentials);

        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setExpectContinueEnabled(false)
                                .build())
                .build();

        HttpGet request = new HttpGet(authUri);

        try {
            httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return httpClient;
    }

    @Bean
    public HttpGet httpGetRequest() {
        HttpGet request = new HttpGet();
        request.setHeader(HttpHeaders.ACCEPT, "application/xml");
        return request;
    }

    @Bean
    public HttpPost httpPostRequest() {
        HttpPost request = new HttpPost();
        request.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_ODATA_VERBOSE);
        request.setHeader(HttpHeaders.ACCEPT, APPLICATION_JSON_ODATA_VERBOSE);
        return request;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}