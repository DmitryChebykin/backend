package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.department.DepartmentCreateDto;
import ru.region_stat.domain.dto.department.DepartmentResultDto;
import ru.region_stat.domain.dto.department.DepartmentUpdateDto;
import ru.region_stat.service.DepartmentService;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/department")
@RestController
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllDepartments", nickname = "getAllDepartments")
    public ResponseEntity<List<DepartmentResultDto>> getAll() {

        return ResponseEntity.ok(departmentService.getAll());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getByIdDepartment", nickname = "getByIdDepartment")
    public ResponseEntity<DepartmentResultDto> getById(@PathVariable("id") UUID id) {

        return ResponseEntity.ok(departmentService.getDepartmentResultDtoById(id));
    }

    @PostMapping
    @ApiOperation(value = "createDepartment", nickname = "createDepartment")
    public ResponseEntity<DepartmentResultDto> create(@RequestBody DepartmentCreateDto departmentCreateDto) {

        return new ResponseEntity<>(departmentService.create(departmentCreateDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "updateDepartment", nickname = "updateDepartment")
    public ResponseEntity<DepartmentResultDto> update(@RequestBody DepartmentUpdateDto departmentUpdateDto, @PathVariable("id") UUID id) {

        return ResponseEntity.ok(departmentService.update(departmentUpdateDto, id));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deleteDepartment", nickname = "deleteDepartment")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {

        departmentService.deleteById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}