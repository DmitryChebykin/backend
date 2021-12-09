package ru.region_stat.domain.entity.oneTimeRequest;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "rs_one_time_request_files")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OneTimeRequestFileEntity extends BaseEntity {

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "size")
    private Long fileSize;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private OneTimeRequestEntity oneTimeRequestEntity;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, mappedBy = "oneTimeRequestFileEntity")
    private OneTimeRequestFileContentEntity requestFileContent;
}