package ru.region_stat.elastic;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import javax.persistence.Id;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "documents")
public class ElasticPublicationEntity {
    @Id
    private String id;
    @Field(name = "publicationId", type = FieldType.Auto)
    private String publicationId;
    @Field(name = "fileId", type = FieldType.Auto)
    private String fileId;
    @Field(name = "name", type = FieldType.Text)
    private String name;
    @Field(name = "fileName", type = FieldType.Auto)
    private String fileName;
    @Field(name = "isArchive", type = FieldType.Boolean)
    private Boolean isArchive;
    @Field(name = "documentText", type = FieldType.Text)
    private String documentText;
}