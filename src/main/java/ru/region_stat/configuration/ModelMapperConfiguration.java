package ru.region_stat.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.region_stat.domain.dto.user.UserResultDto;
import ru.region_stat.domain.entity.user.UserEntity;

@Configuration
public class ModelMapperConfiguration {

    @Bean
    public ModelMapper modelmapper() {

        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(UserEntity.class, UserResultDto.class)
                .addMapping(UserEntity::getName, UserResultDto::setName)
                .addMapping(UserEntity::getFullName, UserResultDto::setFamilyName)
                .addMappings(mapper -> mapper.map(UserEntity::getDepartment, UserResultDto::setDepartmentResultDto));
        
        return modelMapper;
    }
}