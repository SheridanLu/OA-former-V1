package com.mochu.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 公告评论DTO
 */
@Data
public class AnnouncementCommentDTO {

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
