package com.mochu.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告VO
 */
@Data
public class AnnouncementVO {

    private Integer id;

    private String title;

    private String content;

    private String type;

    private LocalDateTime publishTime;

    private LocalDateTime expireTime;

    private Integer publisherId;

    private String publisherName;

    private String status;

    private Integer isTop;

    private String scope;

    /** 公告图片URL,JSON数组 */
    private String images;

    /** 审批人ID */
    private Integer approverId;

    /** 审批人姓名 */
    private String approverName;

    /** 审批时间 */
    private LocalDateTime approveTime;

    /** 审批意见 */
    private String approveRemark;

    /** 评论数 */
    private Integer commentCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
