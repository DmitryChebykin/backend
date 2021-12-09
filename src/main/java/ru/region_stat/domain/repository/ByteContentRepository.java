package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.byteContent.ByteContentEntity;
import java.util.UUID;

@Repository
public interface ByteContentRepository extends JpaRepository<ByteContentEntity, UUID> {
    @Query(value = "SELECT content FROM " + ByteContentEntity.TABLE + " WHERE id = :id", nativeQuery = true)
    byte[] getFileContent(@Param("id") UUID id);

    @Query(value = "SELECT CAST(id AS VARCHAR) from " + ByteContentEntity.TABLE + " WHERE publication_file_id = :publicationFileId", nativeQuery = true)
    String getContentId(@Param("publicationFileId") UUID publicationFileId);

    @Query(value = "SELECT * from " + ByteContentEntity.TABLE + " WHERE publication_file_id = :publicationFileId", nativeQuery = true)
    ByteContentEntity getContentByFileId(@Param("publicationFileId") UUID publicationFileId);
}