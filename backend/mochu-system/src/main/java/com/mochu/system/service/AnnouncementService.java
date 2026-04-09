package com.mochu.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.common.security.SecurityUtils;
import com.mochu.system.dto.AnnouncementCommentDTO;
import com.mochu.system.dto.AnnouncementDTO;
import com.mochu.system.dto.AnnouncementQueryDTO;
import com.mochu.system.entity.SysAnnouncement;
import com.mochu.system.entity.SysAnnouncementComment;
import com.mochu.system.entity.SysUser;
import com.mochu.system.mapper.SysAnnouncementCommentMapper;
import com.mochu.system.mapper.SysAnnouncementMapper;
import com.mochu.system.mapper.SysUserMapper;
import com.mochu.system.vo.AnnouncementCommentVO;
import com.mochu.system.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务 — 对照 V3.2 公告管理
 */
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final SysAnnouncementMapper announcementMapper;
    private final SysAnnouncementCommentMapper commentMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 公告分页查询
     */
    public PageResult<AnnouncementVO> list(AnnouncementQueryDTO dto) {
        int page = (dto.getPage() != null && dto.getPage() > 0) ? dto.getPage() : Constants.DEFAULT_PAGE;
        int size = (dto.getSize() != null && dto.getSize() > 0) ? dto.getSize() : Constants.DEFAULT_SIZE;

        Page<SysAnnouncement> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysAnnouncement> wrapper = new LambdaQueryWrapper<>();

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            wrapper.like(SysAnnouncement::getTitle, dto.getTitle());
        }
        if (dto.getType() != null && !dto.getType().isBlank()) {
            wrapper.eq(SysAnnouncement::getType, dto.getType());
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            wrapper.eq(SysAnnouncement::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(SysAnnouncement::getIsTop).orderByDesc(SysAnnouncement::getCreatedAt);

        announcementMapper.selectPage(pageParam, wrapper);
        List<AnnouncementVO> voList = pageParam.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageParam.getTotal(), page, size);
    }

    /**
     * 公告详情（管理端）
     */
    public AnnouncementVO getById(Integer id) {
        SysAnnouncement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "公告不存在");
        }
        return toVO(entity);
    }

    /**
     * 公告详情（前台，仅已发布）
     */
    public AnnouncementVO getPublishedById(Integer id) {
        SysAnnouncement entity = announcementMapper.selectById(id);
        if (entity == null || !"published".equals(entity.getStatus())) {
            throw new BusinessException(404, "公告不存在");
        }
        AnnouncementVO vo = toVO(entity);
        // 附带评论数
        Long count = commentMapper.selectCount(
                new LambdaQueryWrapper<SysAnnouncementComment>()
                        .eq(SysAnnouncementComment::getAnnouncementId, id));
        vo.setCommentCount(count.intValue());
        return vo;
    }

    /**
     * 创建公告（草稿）
     */
    public Integer create(AnnouncementDTO dto) {
        SysAnnouncement entity = new SysAnnouncement();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus("draft");
        entity.setPublisherId(SecurityUtils.getCurrentUserId());
        if (entity.getIsTop() == null) {
            entity.setIsTop(0);
        }
        if (entity.getScope() == null || entity.getScope().isBlank()) {
            entity.setScope("all");
        }
        announcementMapper.insert(entity);
        return entity.getId();
    }

    /**
     * 更新公告
     */
    public void update(AnnouncementDTO dto) {
        SysAnnouncement entity = announcementMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException(404, "公告不存在");
        }
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setType(dto.getType());
        entity.setExpireTime(dto.getExpireTime());
        entity.setIsTop(dto.getIsTop());
        entity.setScope(dto.getScope());
        entity.setImages(dto.getImages());
        announcementMapper.updateById(entity);
    }

    /**
     * 提交审批
     */
    public void submitApproval(Integer id) {
        SysAnnouncement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "公告不存在");
        }
        if (!"draft".equals(entity.getStatus()) && !"rejected".equals(entity.getStatus())) {
            throw new BusinessException(400, "仅草稿或驳回状态可提交审批");
        }
        entity.setStatus("pending_approval");
        entity.setApproverId(null);
        entity.setApproveTime(null);
        entity.setApproveRemark(null);
        announcementMapper.updateById(entity);
    }

    /**
     * 审批通过（自动发布）
     */
    public void approve(Integer id, String remark) {
        SysAnnouncement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "公告不存在");
        }
        if (!"pending_approval".equals(entity.getStatus())) {
            throw new BusinessException(400, "仅待审批状态可审批");
        }
        entity.setStatus("published");
        entity.setApproverId(SecurityUtils.getCurrentUserId());
        entity.setApproveTime(LocalDateTime.now());
        entity.setApproveRemark(remark);
        entity.setPublishTime(LocalDateTime.now());
        entity.setPublisherId(SecurityUtils.getCurrentUserId());
        announcementMapper.updateById(entity);
    }

    /**
     * 审批驳回
     */
    public void reject(Integer id, String remark) {
        SysAnnouncement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "公告不存在");
        }
        if (!"pending_approval".equals(entity.getStatus())) {
            throw new BusinessException(400, "仅待审批状态可驳回");
        }
        entity.setStatus("rejected");
        entity.setApproverId(SecurityUtils.getCurrentUserId());
        entity.setApproveTime(LocalDateTime.now());
        entity.setApproveRemark(remark);
        announcementMapper.updateById(entity);
    }

    /**
     * 发布公告（兼容旧流程:草稿直接发布）
     */
    public void publish(Integer id) {
        SysAnnouncement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "公告不存在");
        }
        entity.setStatus("published");
        entity.setPublishTime(LocalDateTime.now());
        entity.setPublisherId(SecurityUtils.getCurrentUserId());
        announcementMapper.updateById(entity);
    }

    /**
     * 下线公告
     */
    public void offline(Integer id) {
        SysAnnouncement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "公告不存在");
        }
        entity.setStatus("offline");
        announcementMapper.updateById(entity);
    }

    /**
     * 置顶/取消置顶
     */
    public void toggleTop(Integer id) {
        SysAnnouncement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "公告不存在");
        }
        entity.setIsTop(entity.getIsTop() == 1 ? 0 : 1);
        announcementMapper.updateById(entity);
    }

    /**
     * 删除公告
     */
    public void delete(Integer id) {
        announcementMapper.deleteById(id);
    }

    /**
     * 前台已发布公告列表（首页调用）
     */
    public List<AnnouncementVO> listPublished(int limit) {
        LambdaQueryWrapper<SysAnnouncement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAnnouncement::getStatus, "published")
               .orderByDesc(SysAnnouncement::getIsTop)
               .orderByDesc(SysAnnouncement::getPublishTime)
               .last("LIMIT " + limit);
        return announcementMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    // ==================== 评论相关 ====================

    /**
     * 获取公告评论列表
     */
    public List<AnnouncementCommentVO> listComments(Integer announcementId) {
        LambdaQueryWrapper<SysAnnouncementComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAnnouncementComment::getAnnouncementId, announcementId)
               .orderByDesc(SysAnnouncementComment::getCreatedAt);
        return commentMapper.selectList(wrapper).stream().map(this::toCommentVO).collect(Collectors.toList());
    }

    /**
     * 发表评论
     */
    public Integer addComment(Integer announcementId, AnnouncementCommentDTO dto) {
        // 校验公告存在且已发布
        SysAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null || !"published".equals(announcement.getStatus())) {
            throw new BusinessException(400, "公告不存在或未发布");
        }
        SysAnnouncementComment comment = new SysAnnouncementComment();
        comment.setAnnouncementId(announcementId);
        comment.setContent(dto.getContent());
        comment.setUserId(SecurityUtils.getCurrentUserId());
        commentMapper.insert(comment);
        return comment.getId();
    }

    /**
     * 删除评论（仅本人或管理员）
     */
    public void deleteComment(Integer commentId) {
        SysAnnouncementComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        if (!comment.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "无权删除他人评论");
        }
        commentMapper.deleteById(commentId);
    }

    // ==================== 转换方法 ====================

    private AnnouncementVO toVO(SysAnnouncement entity) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getPublisherId() != null) {
            SysUser publisher = sysUserMapper.selectById(entity.getPublisherId());
            if (publisher != null) {
                vo.setPublisherName(publisher.getRealName());
            }
        }
        if (entity.getApproverId() != null) {
            SysUser approver = sysUserMapper.selectById(entity.getApproverId());
            if (approver != null) {
                vo.setApproverName(approver.getRealName());
            }
        }
        // 查询评论数
        Long count = commentMapper.selectCount(
                new LambdaQueryWrapper<SysAnnouncementComment>()
                        .eq(SysAnnouncementComment::getAnnouncementId, entity.getId()));
        vo.setCommentCount(count.intValue());
        return vo;
    }

    private AnnouncementCommentVO toCommentVO(SysAnnouncementComment comment) {
        AnnouncementCommentVO vo = new AnnouncementCommentVO();
        BeanUtils.copyProperties(comment, vo);
        if (comment.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setUserName(user.getRealName());
            }
        }
        return vo;
    }
}
