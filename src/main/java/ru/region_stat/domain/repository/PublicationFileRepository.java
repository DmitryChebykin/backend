package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.dto.file.PublicationFileExtendedResultDto;
import ru.region_stat.domain.entity.publicationFile.PublicationFileEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PublicationFileRepository extends JpaRepository<PublicationFileEntity, UUID> {
    @Query(value = "SELECT * from  " + PublicationFileEntity.TABLE + " WHERE publication_id = :id", nativeQuery = true)
    Optional<List<PublicationFileEntity>> getByStatisticalPublicationId(@Param("id") UUID id);

    @Query(value = "SELECT * from  " + PublicationFileEntity.TABLE + " WHERE publication_id in :statisticalPublicationIds", nativeQuery = true)
    List<PublicationFileEntity> findAllByStatisticalPublicationIn(ArrayList<UUID> statisticalPublicationIds);

    @Query(value = "SELECT CAST\n" +
            "	( rs_pub_files.ID AS VARCHAR ) as id,\n" +
            "	rs_pub_files.file_name as fileName,\n" +
            "	rs_pub_files.file_extension as fileExtension,\n" +
            "	rs_pub_files.size as fileSize,\n" +
            "	CAST(rs_pub_files.publication_id AS VARCHAR) as publicationId,\n" +
            "	rs_pub_files.day,\n" +
            "	rs_pub_files.month,\n" +
            "	rs_pub_files.year,\n" +
            "	rs_publications.NAME AS publicationName,\n" +
            "	rs_publications.complex_name AS publicationComplexName,\n" +
            "	rs_publications.is_archive AS publicationIsArchive,\n" +
            "	rs_publications.created_time AS publicationCreatedAt,\n" +
            "	rs_publications.last_modified_time AS publicationModifiedAt,\n" +
            "	rs_publications.created_by_user AS publicationCreatedByUser,\n" +
            "	rs_publications.modified_by_user AS publicationModifiedByUser,\n" +
            "	CAST ( rs_publications.type_id AS VARCHAR ) AS publicationTypeId,\n" +
            "	rs_pub_types.NAME AS publicationTypeName,\n" +
            "	rs_pub_types.code AS publicationTypeCode,\n" +
            "	rs_pub_types.period AS publicationTypePeriod,\n" +
            "	rs_pub_types.submission_time AS publicationTypeSubmissionTime,\n" +
            "	CAST ( rs_publications.format_id AS VARCHAR ) AS formatId,\n" +
            "	rs_pub_formats.NAME AS formatName, \n" +
            "	U1.full_name AS fullNameCreator, \n" +
            "	U2.full_name AS fullNameModifier \n" +
            "FROM\n" +
            "	rs_pub_files\n" +
            "	LEFT JOIN rs_publications ON rs_pub_files.publication_id = rs_publications.\"id\"\n" +
            "	LEFT JOIN rs_pub_types ON rs_pub_types.\"id\" = rs_publications.type_id\n" +
            "	LEFT JOIN rs_pub_formats ON rs_pub_formats.\"id\" = rs_publications.format_id \n" +
            "   LEFT JOIN rs_users AS U1 ON rs_publications.created_by_user = CAST ( U1.ID AS VARCHAR )\n" +
            "	LEFT JOIN rs_users AS U2 ON rs_publications.modified_by_user = CAST ( U2.ID AS VARCHAR )\n" +
            "WHERE\n" +
            "	rs_publications.ID IN ( SELECT rs_rubric_publication_link.publication_id FROM rs_rubric_publication_link WHERE rs_rubric_publication_link.rubric_id = :rubricId ) \n" +
            "	AND rs_publications.is_archive = :isArchive", nativeQuery = true)
    List<PublicationFileExtendedResultDto> getArchiveFilteredFilesByRubricId(Boolean isArchive, UUID rubricId);


    @Query(value = "SELECT CAST\n" +
            "	( rs_pub_files.ID AS VARCHAR ) as id,\n" +
            "	rs_pub_files.file_name as fileName,\n" +
            "	rs_pub_files.file_extension as fileExtension,\n" +
            "	rs_pub_files.size as fileSize,\n" +
            "	CAST(rs_pub_files.publication_id AS VARCHAR) as publicationId,\n" +
            "	rs_pub_files.day,\n" +
            "	rs_pub_files.month,\n" +
            "	rs_pub_files.year,\n" +
            "	rs_publications.NAME AS publicationName,\n" +
            "	rs_publications.complex_name AS publicationComplexName,\n" +
            "	rs_publications.is_archive AS publicationIsArchive,\n" +
            "	rs_publications.created_time AS publicationCreatedAt,\n" +
            "	rs_publications.last_modified_time AS publicationModifiedAt,\n" +
            "	rs_publications.created_by_user AS publicationCreatedByUser,\n" +
            "	rs_publications.modified_by_user AS publicationModifiedByUser,\n" +
            "	CAST ( rs_publications.type_id AS VARCHAR ) AS publicationTypeId,\n" +
            "	rs_pub_types.NAME AS publicationTypeName,\n" +
            "	rs_pub_types.code AS publicationTypeCode,\n" +
            "	rs_pub_types.period AS publicationTypePeriod,\n" +
            "	rs_pub_types.submission_time AS publicationTypeSubmissionTime,\n" +
            "	CAST ( rs_publications.format_id AS VARCHAR ) AS formatId,\n" +
            "	rs_pub_formats.NAME AS formatName, \n" +
            "	U1.full_name AS fullNameCreator, \n" +
            "	U2.full_name AS fullNameModifier \n" +
            "FROM\n" +
            "	rs_pub_files\n" +
            "	LEFT JOIN rs_publications ON rs_pub_files.publication_id = rs_publications.\"id\"\n" +
            "	LEFT JOIN rs_pub_types ON rs_pub_types.\"id\" = rs_publications.type_id\n" +
            "	LEFT JOIN rs_pub_formats ON rs_pub_formats.\"id\" = rs_publications.format_id \n" +
            "   LEFT JOIN rs_users AS U1 ON rs_publications.created_by_user = CAST ( U1.ID AS VARCHAR )\n" +
            "	LEFT JOIN rs_users AS U2 ON rs_publications.modified_by_user = CAST ( U2.ID AS VARCHAR )\n" +
            "WHERE\n" +
            "	rs_pub_files.ID IN (:id)", nativeQuery = true)
    List<PublicationFileExtendedResultDto> getByFileIdIn(List<UUID> id);
}