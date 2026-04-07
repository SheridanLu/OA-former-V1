package com.mochu.business.controller;

import com.mochu.business.dto.ProjectDTO;
import com.mochu.business.dto.ProjectQueryDTO;
import com.mochu.business.entity.BizProject;
import com.mochu.business.service.ProjectService;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import com.mochu.common.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目管理接口
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasAuthority('project:view')")
    public R<PageResult<BizProject>> list(ProjectQueryDTO query) {
        return R.ok(projectService.list(query));
    }

    @GetMapping("/all")
    public R<List<BizProject>> listAll() {
        return R.ok(projectService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('project:view')")
    public R<BizProject> getById(@PathVariable Integer id) {
        BizProject project = projectService.getById(id);
        if (project == null) {
            return R.fail(404, "项目不存在");
        }
        return R.ok(project);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('project:edit')")
    public R<Void> create(@Valid @RequestBody ProjectDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        projectService.create(dto, userId);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('project:edit')")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody ProjectDTO dto) {
        projectService.update(id, dto);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('project:edit')")
    public R<Void> updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        projectService.updateStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('project:edit')")
    public R<Void> delete(@PathVariable Integer id) {
        projectService.delete(id);
        return R.ok();
    }
}
