package ru.region_stat.domain.entity.MoFiles;

import lombok.*;
import org.hibernate.annotations.Type;
import ru.region_stat.domain.entity.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "rs_munic_files_content")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MunicFileContentEntity extends BaseEntity {

    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "content")
    private byte[] content;

    @OneToOne
    @JoinColumn(name = "file_id")
    private MunicFileEntity municFileEntity;
}