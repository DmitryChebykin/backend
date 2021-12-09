package ru.region_stat.dataloader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
@Profile("import")
public class SharepointConfig {
    public final static String TRUNCATE_TABLE_TEMPLATE = "CREATE TABLE IF NOT EXISTS %s (\"id\" SERIAL primary key); TRUNCATE  %<s CASCADE;";
    public static String SP_LIST_DATA_URI;
    public static String SP_CONST_REQ_REGISTRY_URI;
    public static String SP_ARCHIVE_LIST_DATA_URI;
    public static String SP_RUBRICS_URI;
    public static String SP_STATISTICAL_EDITIONS_URI;
    public static String SP_ONE_TIME_REQUESTS_ATTACHMENTS_URI;
    public static String SP_USERS_URI;
    public static String SP_ONE_TIME_REQUESTS_URI;
    public static String SP_GOVERNMENT_DEPARTMENTS_URI;
    public static String SP_ONE_TIME_REQUESTS_VERSIONS_URI;

    @Value("${sharepoint.domain}")
    private String domain;

    @PostConstruct
    public SharepointConfig init() {
        SP_USERS_URI = "http://" + domain + "/Ask/_vti_bin/ListData.svc/СписокСведенийОПользователях?$expand=КемСоздано,КемИзменено,Вложения";
        SP_LIST_DATA_URI = "http://" + domain + "/_vti_bin/ListData.svc/";
        SP_CONST_REQ_REGISTRY_URI = "РеестрПостоянныхЗапросов?$expand=КемСоздано,КемИзменено,Рубрики,Вложения";
        SP_ARCHIVE_LIST_DATA_URI = "http://" + domain + "/arhive/_vti_bin/ListData.svc/";
        SP_RUBRICS_URI = "http://" + domain + "/_vti_bin/ListData.svc/Рубрики?$expand=КемСоздано,КемИзменено";
        SP_STATISTICAL_EDITIONS_URI = "http://" + domain + "/_vti_bin/ListData.svc/СтатистическиеИздания?$expand=КемСоздано,КемИзменено,Рубрики,Рубрики0";
        SP_ONE_TIME_REQUESTS_ATTACHMENTS_URI = "http://" + domain + "/Ask/_vti_bin/ListData.svc/ЖурналЗапросов?$expand=Вложения";
        SP_ONE_TIME_REQUESTS_URI = "http://" + domain + "/Ask/_vti_bin/ListData.svc/ЖурналЗапросов?$expand=КемИзменено,КемСоздано,ТекущийСтатус,ОрганГосударственнойВласти";
        SP_GOVERNMENT_DEPARTMENTS_URI = "http://" + domain + "/Ask/_vti_bin/ListData.svc/ОрганыГосударственнойВласти?&$expand=КемИзменено,КемСоздано,Вложения";
        SP_ONE_TIME_REQUESTS_VERSIONS_URI = "http://" + domain + "/Ask/_vti_bin/owssvr.dll?XMLDATA=true&IncludeVersions=TRUE&List=%7BFF0B7379-1C17-4193-9046-F0CAD4D2B49D%7D&RowLimit=0";
        return this;
    }
}