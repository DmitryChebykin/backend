package ru.region_stat.dataloader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.olingo.client.api.domain.*;
import org.apache.olingo.client.api.serialization.ODataDeserializerException;
import org.apache.olingo.client.api.serialization.ODataReader;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.format.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static ru.region_stat.dataloader.SharepointConfig.*;

@Component
@Profile("import")
public class SharepointTableLoader {
    private static final String CREATE_REQUESTS_VERSIONS_RUS_TABLE = "CREATE TABLE IF NOT EXISTS sharepoint_one_time_requests_versions (" +
            " id serial PRIMARY KEY, " +
            " \"Текст_запроса_в_Петростат\" TEXT," +
            " \"Идентификатор_запроса\" TEXT," +
            " \"Важность\" TEXT," +
            " \"Должность_подписавшего\" TEXT," +
            " \"кем_изменено\" TEXT," +
            " \"Кем_создано\" TEXT," +
            " \"Электронная_почта_исполнителя\" TEXT," +
            " \"Орган_государственной_власти\" TEXT," +
            " \"Подписавший_запрос\" TEXT," +
            " \"Создан\" TEXT," +
            " \"Резолюция\" TEXT," +
            " \"Тема_запроса\" TEXT," +
            " \"Текущий_статус\" TEXT," +
            " \"Должность_исполнителя\" TEXT," +
            " \"Вложения\" TEXT," +
            " \"Версия\" TEXT," +
            " \"Исполнитель\" TEXT," +
            " \"Номер_запроса_в_Петростат\" TEXT," +
            " \"Контактный_телефон_исполнителя\" TEXT," +
            " \"Изменен\" TEXT," +
            " \"Автор_запроса\" TEXT" +
            ")";
    @Resource
    private HttpClient httpClient;
    @Resource
    private HttpGet httpGetRequest;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private DataSource dataSource;

    @Value("${sharepoint.rubric}")
    private List<String> rubricList;

    public void run() {
        String tableName;

        tableName = "sharepoint_statistical_publication";
        importStatPublications(tableName, rubricList);

        tableName = "sharepoint_regular_request_registry";
        importFromLink(tableName, SP_LIST_DATA_URI + SP_CONST_REQ_REGISTRY_URI);

        String getLink = SP_ONE_TIME_REQUESTS_ATTACHMENTS_URI;
        tableName = "sharepoint_one_time_requests_attachments";
        importOneTimeRequestsAttachments(getLink, tableName);

        tableName = "sharepoint_statistical_publication_reference";
        importFromLink(tableName, SP_STATISTICAL_EDITIONS_URI);

        tableName = "sharepoint_users";
        importFromLink(tableName, SP_USERS_URI);

        tableName = "sharepoint_one_time_requests";
        importFromLink(tableName, SP_ONE_TIME_REQUESTS_URI);

        tableName = "sharepoint_departments";
        importFromLink(tableName, SP_GOVERNMENT_DEPARTMENTS_URI);

        tableName = "sharepoint_one_time_requests_versions";

        jdbcTemplate.execute(CREATE_REQUESTS_VERSIONS_RUS_TABLE);
        SimpleJdbcInsert simpleJdbcInsert =
                new SimpleJdbcInsert(dataSource).withTableName(tableName).usingGeneratedKeyColumns("id");

        httpGetRequest.setURI(URI.create(SP_ONE_TIME_REQUESTS_VERSIONS_URI));
        HttpResponse response;

        try {
            response = httpClient.execute(httpGetRequest);
            HttpEntity httpEntity = response.getEntity();

            String xmlString = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);

            JSONObject jsonObj = XML.toJSONObject(xmlString);

            JSONArray xmlSchema = jsonObj.getJSONObject("xml").getJSONObject("s:Schema").getJSONObject("s:ElementType").getJSONArray("s:AttributeType");

            int lengthSchema = xmlSchema.length();

            List<HashMap<String, String>> names = IntStream.range(0, lengthSchema).mapToObj(i -> {
                JSONObject jsonObject = (JSONObject) xmlSchema.get(i);
                String name = jsonObject.getString("name");
                String rsName = jsonObject.getString("rs:name");
                return new HashMap<String, String>() {{
                    put(name, rsName);
                }};
            }).collect(Collectors.toList());

            Map<String, String> result = new HashMap<>();

            names.forEach(map -> result.putAll(map.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));

            JSONArray jsonArray = jsonObj.getJSONObject("xml").getJSONObject("rs:data").getJSONArray("z:row");

            int length = jsonArray.length();

            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> stringObjectMap = jsonObject.toMap();
                Map<String, Object> rusKeyMap = new HashMap<>();
                stringObjectMap.forEach((key, value) -> {
                    String rusKey = result.get(key);
                    rusKeyMap.put(rusKey.replaceAll(" ", "_"), value);
                });

                int execute = simpleJdbcInsert.execute(rusKeyMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpGetRequest.releaseConnection();
        }
    }

    private void importOneTimeRequestsAttachments(String getLink, String tableName) {
        jdbcTemplate.execute(String.format(SharepointConfig.TRUNCATE_TABLE_TEMPLATE, tableName));

        List<ClientCollectionValue<ClientValue>> clientCollectionValues = null;
        try {
            clientCollectionValues = getClientCollectionValues(getLink);
        } catch (ODataDeserializerException | IOException e) {
            e.printStackTrace();
        }

        for (ClientCollectionValue<ClientValue> clientCollectionValue : Objects.requireNonNull(clientCollectionValues)) {
            for (ClientValue next : clientCollectionValue) {
                ClientCollectionValue<ClientValue> collectionValue = next.asComplex().get("Вложения").getComplexValue().get("results").getCollectionValue();
                for (ClientValue attachClientValue : collectionValue) {
                    insertRowFromClientValue(tableName, getLink, true, attachClientValue);
                }
                System.out.println(collectionValue);
            }
        }
    }

    private void importFromLink(String tableName, String getLink) {
        jdbcTemplate.execute(String.format(SharepointConfig.TRUNCATE_TABLE_TEMPLATE, tableName));
        importTable(tableName, getLink, false);
    }

    private void importStatPublications(String tableName, List<String> rubricList) {
        jdbcTemplate.execute(String.format(SharepointConfig.TRUNCATE_TABLE_TEMPLATE, tableName));

        for (String list : Arrays.asList(SP_LIST_DATA_URI, SP_ARCHIVE_LIST_DATA_URI)) {
            for (String rubric : rubricList) {
                String getLink = list + rubric + "?$expand=КемСоздано,КемИзменено";

                if (!rubric.equals("Сводные")) {
                    getLink = getLink + ",Рубрики";
                }

                importTable(tableName, getLink, true);
            }
        }
    }

    private byte[] getBytes(byte[] fileBytes, String media_src) {
        httpGetRequest.setURI(URI.create(media_src.replaceAll(" ", "%20")));
        HttpResponse response;

        try {
            response = httpClient.execute(httpGetRequest);
            fileBytes = IOUtils.toByteArray(response.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpGetRequest.releaseConnection();
        }
        return fileBytes;
    }

    private List<ClientCollectionValue<ClientValue>> getClientCollectionValues(String getLink) throws ODataDeserializerException, IOException {
        List<ClientCollectionValue<ClientValue>> clientCollectionValueList = new ArrayList<>();
        boolean isNext1000 = true;

        while (isNext1000) {
            isNext1000 = false;

            ClientEntity rubricEntityClient = getEntityClient(getLink, httpClient, httpGetRequest);
            ClientCollectionValue<ClientValue> itemCollection = getItemCollection(rubricEntityClient);
            clientCollectionValueList.add(itemCollection);

            ClientProperty nextProperty = rubricEntityClient.getProperty("d").getComplexValue().get("__next");

            if (nextProperty != null) {
                getLink = nextProperty.getPrimitiveValue().toString();
                isNext1000 = true;
            }
        }
        return clientCollectionValueList;
    }

    private ClientEntity getEntityClient(String uri, HttpClient httpClient, HttpGet httpGetRequest) throws ODataDeserializerException, IOException {
        httpGetRequest.setURI(URI.create(uri));
        httpGetRequest.setHeader(HttpHeaders.ACCEPT, "application/json;odata=verbose");
        HttpResponse httpResponse = httpClient.execute(httpGetRequest);

        InputStream content = httpResponse.getEntity().getContent();
        ODataReader reader = ODataClientFactory.getClient().getReader();

        ClientEntity clientEntity = reader.readEntity(content, ContentType.APPLICATION_JSON);
        httpGetRequest.releaseConnection();

        return clientEntity;
    }

    private ClientCollectionValue<ClientValue> getItemCollection(ClientEntity clientEntity) {
        ClientProperty clientProperty = clientEntity.getProperty("d");

        ClientComplexValue collectionValue = clientProperty.getComplexValue();

        Iterator<ClientProperty> iterator = collectionValue.iterator();

        ClientProperty next = iterator.next();

        return next.getCollectionValue();
    }

    private void importTable(String tableName, String getLink, boolean isByteLoad) {
        List<ClientCollectionValue<ClientValue>> clientCollectionValueList = null;

        try {
            System.out.println("getLink = " + getLink);
            clientCollectionValueList = getClientCollectionValues(getLink);
        } catch (ODataDeserializerException | IOException e) {
            e.printStackTrace();
        }

        for (ClientCollectionValue<ClientValue> clientValues : Objects.requireNonNull(clientCollectionValueList)) {
            for (ClientValue clientValue : clientValues) {
                insertRowFromClientValue(tableName, getLink, isByteLoad, clientValue);
            }
        }
    }

    private void insertRowFromClientValue(String tableName, String getLink, boolean isByteLoad, ClientValue clientValue) {
        byte[] fileBytes = null;

        ClientComplexValue clientProperties = clientValue.asComplex();

        Map<String, Object> sqlObjectMap = clientProperties.asJavaMap();

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (rest_link,");
        StringBuilder sqlBytea = new StringBuilder(String.format("ALTER TABLE %s add COLUMN IF NOT EXISTS file_content bytea;", tableName));
        StringBuilder createColumn = new StringBuilder("ALTER TABLE " + tableName + " ");
        StringBuilder placeholders = new StringBuilder();

        List<String> sqlObjectList = new ArrayList<>();
        sqlObjectList.add(getLink);
        placeholders.append("?,");
        createColumn.append("add COLUMN IF NOT EXISTS rest_link").append(" TEXT,");

        String media_src;

        for (Iterator<String> iter = sqlObjectMap.keySet().iterator(); iter.hasNext(); ) {
            String nextKey = iter.next();

            if (nextKey.equals("__metadata")) {
                Map<String, Object> metaDataMap = clientProperties.get("__metadata").getComplexValue().asJavaMap();

                if (isByteLoad) {
                    createColumn.append(" add COLUMN IF NOT EXISTS file_uri TEXT, ");
                    sql.append("file_uri,");
                    placeholders.append("?,");
                    media_src = (String) metaDataMap.get("media_src");
                    sqlObjectList.add(media_src);

                    fileBytes = getBytes(fileBytes, media_src);
                }

                continue;
            }

            createColumn.append(" add COLUMN IF NOT EXISTS ").append(nextKey).append(" TEXT, ");
            jdbcTemplate.execute(String.format("ALTER TABLE %s add COLUMN IF NOT EXISTS %s TEXT", tableName, nextKey));

            sql.append(nextKey);
            placeholders.append("?");

            if (iter.hasNext()) {
                sql.append(",");
                placeholders.append(",");
            }

            Object o = sqlObjectMap.get(nextKey);

            if (o == null) {
                sqlObjectList.add("null");
            } else {
                String jsonString = null;

                if (!(o instanceof LinkedHashMap)) {
                    sqlObjectList.add(o.toString());
                } else {
                    try {
                        jsonString = objectMapper.writeValueAsString(o);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    sqlObjectList.add(jsonString);
                }
            }
        }

        sql.append(") VALUES (").append(placeholders).append(")");

        createColumn.deleteCharAt(createColumn.length() - 2);
        createColumn.append(";");

        jdbcTemplate.execute(createColumn.toString());

        Object[] values = sqlObjectList.toArray(new String[]{});

        List<Map<String, Object>> mapsId = jdbcTemplate.queryForList(String.format("%sRETURNING id;", sql.toString()), values);
        int insertedRowIndex = (int) mapsId.get(0).get("id");

        if (isByteLoad) {
            jdbcTemplate.execute(sqlBytea.toString());

            jdbcTemplate.update(String.format("UPDATE %s SET file_content = ? WHERE id = ?", tableName), fileBytes, insertedRowIndex);
        }
    }
}