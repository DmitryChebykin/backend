package ru.region_stat.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

@Service
public class GotenbergService {

    public static final String PROTOCOL_HTTP_PREFIX = "http://";
    public static final String CONVERT_OFFICE_ENDPOINT = "/forms/libreoffice/convert";
    @Value("${file-converter-url}")
    private String gotenbertServiceUrl;

    public byte[] convertOfficeFile(byte[] officeFile, String fileName) {
        try {
            String endpointUrl = PROTOCOL_HTTP_PREFIX + gotenbertServiceUrl + CONVERT_OFFICE_ENDPOINT;
            HttpPost post = new HttpPost(endpointUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("file", officeFile, ContentType.APPLICATION_OCTET_STREAM, fileName);

            HttpEntity entity = builder.build();
            post.setEntity(entity);

            CloseableHttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(post);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            response.getEntity().writeTo(byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}