package ru.region_stat.domain.dto.file;

import java.util.Date;

public interface PublicationFileExtendedResultDto {
    String getId();

    String getFileName();

    String getFileExtension();

    Long getFileSize();

    String getPublicationId();

    Integer getDay();

    Integer getMonth();

    Integer getYear();

    String getPublicationName();

    String getPublicationComplexName();

    Boolean getPublicationIsArchive();

    Date getPublicationCreatedAt();

    Date getPublicationModifiedAt();

    String getPublicationCreatedByUser();

    String getPublicationModifiedByUser();

    String getPublicationTypeId();

    String getPublicationTypeName();

    String getPublicationTypeCode();

    String getPublicationTypePeriod();

    String getPublicationTypeSubmissionTime();

    String getFormatId();

    String getFormatName();

    String getFullNameCreator();

    String getFullNameModifier();
}