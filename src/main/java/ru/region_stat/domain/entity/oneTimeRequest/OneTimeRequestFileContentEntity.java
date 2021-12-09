package ru.region_stat.domain.entity.oneTimeRequest;

import lombok.*;
import org.hibernate.annotations.Type;
import ru.region_stat.domain.entity.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "rs_one_time_request_files_contents")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OneTimeRequestFileContentEntity extends BaseEntity {

    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "content")
    private byte[] content;

    @OneToOne
    @JoinColumn(name = "file_id")
    private OneTimeRequestFileEntity oneTimeRequestFileEntity;
}