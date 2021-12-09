package ru.region_stat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseResultDto {

    private UUID id;

    private Date createAt;

    private Date modifiedAt;

    private String createdByUser;

    private String modifiedByUser;
}