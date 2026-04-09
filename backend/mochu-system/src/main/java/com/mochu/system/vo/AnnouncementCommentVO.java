package com.mochu.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告评论VO
 */
@Data
public class AnnouncementCommentVO {

    private Integer id;

    private Integer announcementId;

    private String content;

    private Integer userId;

    private String userName;

    private LocalDateTime createdAt;
}
