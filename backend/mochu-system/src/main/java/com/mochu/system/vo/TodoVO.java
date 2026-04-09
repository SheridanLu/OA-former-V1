package com.mochu.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoVO {

    private Integer id;

    private Integer userId;

    private String bizType;

    private Integer bizId;

    private String title;

    private String content;

    /** 0待处理/1已处理 */
    private Integer status;

    /** 优先级: 0普通 1紧急 2特急 */
    private Integer priority;

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 业务跳转路径 */
    private String linkUrl;

    /** 关联业务单据标题 */
    private String bizTitle;

    /** 处理时间 */
    private LocalDateTime handledAt;

    /** 催办次数 */
    private Integer remindCount;

    /** 是否超期 */
    private Boolean overdue;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
