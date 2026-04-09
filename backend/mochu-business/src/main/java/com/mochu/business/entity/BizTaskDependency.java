package com.mochu.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务依赖关系表
 */
@Data
@TableName("biz_task_dependency")
public class BizTaskDependency {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 当前任务ID(后继) */
    private Integer taskId;

    /** 前置任务ID(前驱) */
    private Integer depTaskId;

    /** 依赖类型:FS/SS/FF/SF */
    private String depType;

    private LocalDateTime createdAt;
}
