package ru.region_stat.domain.entity.publicationFormat;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "rs_pub_formats")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicationFormatEntity extends BaseEntity {

    @Column(name = "name")
    private String name;
}