package com.mochu.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统待办表
 */
@Data
@TableName("sys_todo")
public class SysTodo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    /** 待办关联业务类型 */
    private String bizType;

    /** 待办关联业务单据ID */
    private Integer bizId;

    private String title;

    private String content;

    /** 0待处理/1已处理 */
    private Integer status;

    /** 优先级: 0普通 1紧急 2特急 */
    private Integer priority;

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 业务跳转路径(前端路由) */
    private String linkUrl;

    /** 关联业务单据标题 */
    private String bizTitle;

    /** 处理时间 */
    private LocalDateTime handledAt;

    /** 处理人ID(委托处理场景) */
    private Integer handlerId;

    /** 催办次数 */
    private Integer remindCount;

    /** 最近催办时间 */
    private LocalDateTime lastRemindAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
