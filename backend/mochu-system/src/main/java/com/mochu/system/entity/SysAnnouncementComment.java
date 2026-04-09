package com.mochu.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告评论表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_announcement_comment")
public class SysAnnouncementComment extends BaseEntity {

    /** 公告ID */
    private Integer announcementId;

    /** 评论内容 */
    private String content;

    /** 评论人ID */
    private Integer userId;
}
