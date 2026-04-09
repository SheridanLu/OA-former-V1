package com.mochu.system.controller;

import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import com.mochu.system.service.TodoService;
import com.mochu.system.vo.TodoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 待办中心接口
 */
@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    /**
     * 我的待办列表 — GET /api/v1/todos
     */
    @GetMapping
    public R<PageResult<TodoVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(todoService.listMyTodos(status, bizType, priority, page, size));
    }

    /**
     * 待办数量 — GET /api/v1/todos/count
     */
    @GetMapping("/count")
    public R<Long> count() {
        return R.ok(todoService.countPending());
    }

    /**
     * 按业务类型分组统计 — GET /api/v1/todos/stats
     */
    @GetMapping("/stats")
    public R<List<TodoService.TodoStatItem>> stats() {
        return R.ok(todoService.statByBizType());
    }

    /**
     * 标记已处理 — PATCH /api/v1/todos/{id}/done
     */
    @PatchMapping("/{id}/done")
    public R<Void> markDone(@PathVariable Integer id) {
        todoService.markDone(id);
        return R.ok();
    }

    /**
     * 批量标记已处理 — PATCH /api/v1/todos/batch-done
     */
    @PatchMapping("/batch-done")
    public R<Void> batchMarkDone(@RequestBody Map<String, List<Integer>> body) {
        todoService.batchMarkDone(body.get("ids"));
        return R.ok();
    }

    /**
     * 催办 — PATCH /api/v1/todos/{id}/remind
     */
    @PatchMapping("/{id}/remind")
    public R<Void> remind(@PathVariable Integer id) {
        todoService.remind(id);
        return R.ok();
    }
}
