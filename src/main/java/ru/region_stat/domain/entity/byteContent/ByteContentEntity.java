package ru.region_stat.domain.entity.byteContent;

import lombok.*;
import org.hibernate.annotations.Type;
import ru.region_stat.domain.entity.BaseEntity;
import ru.region_stat.domain.entity.publicationFile.PublicationFileEntity;
import javax.persistence.*;
import java.util.Date;
import static ru.region_stat.domain.entity.byteContent.ByteContentEntity.TABLE;

@Entity
@Table(name = TABLE)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ByteContentEntity extends BaseEntity {
    public static final String TABLE = "rs_contents";
    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "content")
    private byte[] content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_file_id")
    private PublicationFileEntity publicationFileEntity;

    @PrePersist
    public void prePersistCreatedAt() {
        super.setCreatedAt(new Date());
    }
}