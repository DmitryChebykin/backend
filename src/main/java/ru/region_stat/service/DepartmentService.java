package ru.region_stat.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.domain.dto.department.DepartmentCreateDto;
import ru.region_stat.domain.dto.department.DepartmentResultDto;
import ru.region_stat.domain.dto.department.DepartmentUpdateDto;
import ru.region_stat.domain.entity.department.DepartmentEntity;
import ru.region_stat.domain.repository.DepartmentRepository;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Resource
    private DepartmentRepository departmentRepository;

    @Resource
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<DepartmentResultDto> getAll() {

        List<DepartmentEntity> departmentEntityList = departmentRepository.findAll();

        return departmentEntityList.stream()
                .map(departmentEntity -> modelMapper.map(departmentEntity, DepartmentResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResultDto getDepartmentResultDtoById(UUID id) {

        return modelMapper.map(departmentRepository.findById(id)
                .orElseThrow(RuntimeException::new), DepartmentResultDto.class);
    }

    @Transactional
    public DepartmentResultDto create(DepartmentCreateDto departmentCreateDto) {

        DepartmentEntity departmentEntity = modelMapper.map(departmentCreateDto, DepartmentEntity.class);

        return modelMapper.map(departmentRepository.save(departmentEntity), DepartmentResultDto.class);
    }

    @Transactional
    public DepartmentResultDto update(DepartmentUpdateDto departmentUpdateDto, UUID id) {

        DepartmentEntity departmentEntity = departmentRepository.findById(id).orElseThrow(RuntimeException::new);

        modelMapper.map(departmentUpdateDto, departmentEntity);

        return modelMapper.map(departmentEntity, DepartmentResultDto.class);
    }

    @Transactional
    public void deleteById(UUID id) {

        departmentRepository.deleteById(id);
    }

    public Boolean existsByName(String name) {
        return departmentRepository.existsByName(name);
    }

    @Transactional
    public DepartmentEntity getByName(String name) {
        return departmentRepository.findByName(name).orElseThrow(RuntimeException::new);
    }

    @Transactional(readOnly = true)
    public DepartmentEntity getById(UUID id) {
        return departmentRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    public Boolean existsById(UUID id) {
        return departmentRepository.existsById(id);
    }
}