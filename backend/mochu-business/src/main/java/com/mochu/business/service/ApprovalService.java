package com.mochu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mochu.business.dto.ApprovalActionDTO;
import com.mochu.business.dto.FlowDefDTO;
import com.mochu.business.entity.BizApprovalInstance;
import com.mochu.business.entity.BizApprovalRecord;
import com.mochu.business.entity.SysFlowDef;
import com.mochu.business.mapper.BizApprovalInstanceMapper;
import com.mochu.business.mapper.BizApprovalRecordMapper;
import com.mochu.business.mapper.SysFlowDefMapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.system.entity.SysUser;
import com.mochu.system.entity.SysUserRole;
import com.mochu.system.mapper.SysUserMapper;
import com.mochu.system.mapper.SysUserRoleMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批服务 — 对照 V3.2 审批流程引擎
 */
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final SysFlowDefMapper flowDefMapper;
    private final BizApprovalInstanceMapper instanceMapper;
    private final BizApprovalRecordMapper recordMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final ObjectMapper objectMapper;

    // ===================== 流程定义 CRUD =====================

    public PageResult<SysFlowDef> listFlowDefs(String bizType, Integer status, Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        Page<SysFlowDef> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<SysFlowDef> wrapper = new LambdaQueryWrapper<>();
        if (bizType != null && !bizType.isBlank()) {
            wrapper.eq(SysFlowDef::getBizType, bizType);
        }
        if (status != null) {
            wrapper.eq(SysFlowDef::getStatus, status);
        }
        wrapper.orderByDesc(SysFlowDef::getId);

        flowDefMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(pageParam.getRecords(), pageParam.getTotal(), p, s);
    }

    public SysFlowDef getFlowDefById(Integer id) {
        return flowDefMapper.selectById(id);
    }

    public void createFlowDef(FlowDefDTO dto) {
        SysFlowDef entity = new SysFlowDef();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getStatus() == null) entity.setStatus(1);
        if (entity.getVersion() == null) entity.setVersion(1);
        flowDefMapper.insert(entity);
    }

    public void updateFlowDef(Integer id, FlowDefDTO dto) {
        SysFlowDef entity = flowDefMapper.selectById(id);
        if (entity == null) throw new BusinessException("流程定义不存在");
        entity.setFlowName(dto.getFlowName());
        entity.setNodesJson(dto.getNodesJson());
        entity.setConditionJson(dto.getConditionJson());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getVersion() != null) entity.setVersion(dto.getVersion());
        flowDefMapper.updateById(entity);
    }

    public void deleteFlowDef(Integer id) {
        flowDefMapper.deleteById(id);
    }

    // ===================== 审批操作 =====================

    /**
     * 提交审批 — 创建审批实例
     */
    @Transactional
    public void submitForApproval(String bizType, Integer bizId, Integer initiatorId) {
        // 查找该业务类型的启用流程定义
        SysFlowDef flowDef = flowDefMapper.selectOne(
                new LambdaQueryWrapper<SysFlowDef>()
                        .eq(SysFlowDef::getBizType, bizType)
                        .eq(SysFlowDef::getStatus, 1)
                        .orderByDesc(SysFlowDef::getVersion)
                        .last("LIMIT 1"));
        if (flowDef == null) {
            throw new BusinessException("该业务类型未配置审批流程");
        }

        // 检查是否已有进行中的审批
        Long existCount = instanceMapper.selectCount(
                new LambdaQueryWrapper<BizApprovalInstance>()
                        .eq(BizApprovalInstance::getBizType, bizType)
                        .eq(BizApprovalInstance::getBizId, bizId)
                        .eq(BizApprovalInstance::getStatus, "pending"));
        if (existCount > 0) {
            throw new BusinessException("该单据已有进行中的审批");
        }

        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setBizType(bizType);
        instance.setBizId(bizId);
        instance.setFlowDefId(flowDef.getId());
        instance.setCurrentNode(1);
        instance.setStatus("pending");
        instance.setInitiatorId(initiatorId);
        instance.setCreatedAt(LocalDateTime.now());
        instanceMapper.insert(instance);
    }

    /**
     * 审批通过
     */
    @Transactional
    public void approve(Integer instanceId, Integer approverId, String opinion) {
        BizApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) throw new BusinessException("审批实例不存在");
        if (!"pending".equals(instance.getStatus())) throw new BusinessException("该审批已结束");

        // 检查审批人权限
        SysFlowDef flowDef = flowDefMapper.selectById(instance.getFlowDefId());
        List<FlowNode> nodes = parseNodes(flowDef.getNodesJson());
        int currentIdx = instance.getCurrentNode() - 1;
        if (currentIdx < 0 || currentIdx >= nodes.size()) {
            throw new BusinessException("审批节点配置异常");
        }
        FlowNode currentNode = nodes.get(currentIdx);
        checkApproverPermission(currentNode, approverId);

        // 写入审批记录
        BizApprovalRecord record = new BizApprovalRecord();
        record.setInstanceId(instanceId);
        record.setNodeOrder(instance.getCurrentNode());
        record.setNodeName(currentNode.getNodeName());
        record.setApproverId(approverId);
        record.setAction("approve");
        record.setOpinion(opinion);
        record.setCreatedAt(LocalDateTime.now());
        recordMapper.insert(record);

        // 判断是否为最后一个节点
        if (instance.getCurrentNode() >= nodes.size()) {
            instance.setStatus("approved");
        } else {
            instance.setCurrentNode(instance.getCurrentNode() + 1);
        }
        instance.setUpdatedAt(LocalDateTime.now());
        instanceMapper.updateById(instance);
    }

    /**
     * 审批驳回
     */
    @Transactional
    public void reject(Integer instanceId, Integer approverId, String opinion) {
        BizApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) throw new BusinessException("审批实例不存在");
        if (!"pending".equals(instance.getStatus())) throw new BusinessException("该审批已结束");

        SysFlowDef flowDef = flowDefMapper.selectById(instance.getFlowDefId());
        List<FlowNode> nodes = parseNodes(flowDef.getNodesJson());
        int currentIdx = instance.getCurrentNode() - 1;
        FlowNode currentNode = nodes.get(currentIdx);
        checkApproverPermission(currentNode, approverId);

        BizApprovalRecord record = new BizApprovalRecord();
        record.setInstanceId(instanceId);
        record.setNodeOrder(instance.getCurrentNode());
        record.setNodeName(currentNode.getNodeName());
        record.setApproverId(approverId);
        record.setAction("reject");
        record.setOpinion(opinion);
        record.setCreatedAt(LocalDateTime.now());
        recordMapper.insert(record);

        instance.setStatus("rejected");
        instance.setUpdatedAt(LocalDateTime.now());
        instanceMapper.updateById(instance);
    }

    // ===================== 查询 =====================

    /**
     * 我的待审批列表
     */
    public PageResult<Map<String, Object>> getMyPending(Integer userId, Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        // 查询用户角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        Set<Integer> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());

        // 查询所有 pending 的实例（不分页，需全量过滤审批人）
        List<BizApprovalInstance> allPending = instanceMapper.selectList(
                new LambdaQueryWrapper<BizApprovalInstance>()
                        .eq(BizApprovalInstance::getStatus, "pending")
                        .orderByDesc(BizApprovalInstance::getCreatedAt));

        List<Map<String, Object>> matched = new ArrayList<>();
        for (BizApprovalInstance inst : allPending) {
            SysFlowDef flowDef = flowDefMapper.selectById(inst.getFlowDefId());
            if (flowDef == null) continue;
            List<FlowNode> nodes = parseNodes(flowDef.getNodesJson());
            int idx = inst.getCurrentNode() - 1;
            if (idx < 0 || idx >= nodes.size()) continue;
            FlowNode node = nodes.get(idx);

            boolean isApprover = false;
            if ("user".equals(node.getApproverType()) && userId.equals(node.getApproverId())) {
                isApprover = true;
            } else if ("role".equals(node.getApproverType()) && roleIds.contains(node.getApproverId())) {
                isApprover = true;
            }
            if (!isApprover) continue;

            Map<String, Object> item = buildInstanceMap(inst, flowDef, node);
            matched.add(item);
        }

        // 手动分页
        int total = matched.size();
        int from = Math.min((p - 1) * s, total);
        int to = Math.min(from + s, total);
        List<Map<String, Object>> result = matched.subList(from, to);
        return new PageResult<>(result, total, p, s);
    }

    /**
     * 我发起的审批列表
     */
    public PageResult<Map<String, Object>> getMyInitiated(Integer userId, Integer page, Integer size) {
        int p = (page == null || page < 1) ? Constants.DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? Constants.DEFAULT_SIZE : size;

        Page<BizApprovalInstance> pageParam = new Page<>(p, s);
        instanceMapper.selectPage(pageParam, new LambdaQueryWrapper<BizApprovalInstance>()
                .eq(BizApprovalInstance::getInitiatorId, userId)
                .orderByDesc(BizApprovalInstance::getCreatedAt));

        List<Map<String, Object>> result = new ArrayList<>();
        for (BizApprovalInstance inst : pageParam.getRecords()) {
            SysFlowDef flowDef = flowDefMapper.selectById(inst.getFlowDefId());
            List<FlowNode> nodes = parseNodes(flowDef != null ? flowDef.getNodesJson() : "[]");
            int idx = inst.getCurrentNode() - 1;
            FlowNode node = (idx >= 0 && idx < nodes.size()) ? nodes.get(idx) : null;
            Map<String, Object> item = buildInstanceMap(inst, flowDef, node);
            result.add(item);
        }
        return new PageResult<>(result, pageParam.getTotal(), p, s);
    }

    /**
     * 审批实例详情（含记录）
     */
    public Map<String, Object> getInstanceDetail(Integer instanceId) {
        BizApprovalInstance inst = instanceMapper.selectById(instanceId);
        if (inst == null) throw new BusinessException("审批实例不存在");

        SysFlowDef flowDef = flowDefMapper.selectById(inst.getFlowDefId());
        List<FlowNode> nodes = parseNodes(flowDef != null ? flowDef.getNodesJson() : "[]");
        int idx = inst.getCurrentNode() - 1;
        FlowNode node = (idx >= 0 && idx < nodes.size()) ? nodes.get(idx) : null;

        Map<String, Object> result = buildInstanceMap(inst, flowDef, node);

        // 审批记录
        List<BizApprovalRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<BizApprovalRecord>()
                        .eq(BizApprovalRecord::getInstanceId, instanceId)
                        .orderByAsc(BizApprovalRecord::getNodeOrder)
                        .orderByAsc(BizApprovalRecord::getId));
        List<Map<String, Object>> recordList = new ArrayList<>();
        for (BizApprovalRecord r : records) {
            Map<String, Object> rm = new LinkedHashMap<>();
            rm.put("id", r.getId());
            rm.put("node_order", r.getNodeOrder());
            rm.put("node_name", r.getNodeName());
            rm.put("approver_id", r.getApproverId());
            rm.put("approver_name", resolveUserName(r.getApproverId()));
            rm.put("action", r.getAction());
            rm.put("opinion", r.getOpinion());
            rm.put("created_at", r.getCreatedAt());
            recordList.add(rm);
        }
        result.put("records", recordList);

        // 流程节点列表
        List<Map<String, Object>> nodeList = new ArrayList<>();
        for (FlowNode n : nodes) {
            Map<String, Object> nm = new LinkedHashMap<>();
            nm.put("node_order", n.getNodeOrder());
            nm.put("node_name", n.getNodeName());
            nm.put("approver_type", n.getApproverType());
            nm.put("approver_id", n.getApproverId());
            nodeList.add(nm);
        }
        result.put("nodes", nodeList);

        return result;
    }

    // ===================== 内部方法 =====================

    private Map<String, Object> buildInstanceMap(BizApprovalInstance inst, SysFlowDef flowDef, FlowNode currentNode) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", inst.getId());
        item.put("biz_type", inst.getBizType());
        item.put("biz_id", inst.getBizId());
        item.put("flow_name", flowDef != null ? flowDef.getFlowName() : "");
        item.put("current_node", inst.getCurrentNode());
        item.put("current_node_name", currentNode != null ? currentNode.getNodeName() : "");
        item.put("status", inst.getStatus());
        item.put("initiator_id", inst.getInitiatorId());
        item.put("initiator_name", resolveUserName(inst.getInitiatorId()));
        item.put("created_at", inst.getCreatedAt());
        item.put("updated_at", inst.getUpdatedAt());
        return item;
    }

    private String resolveUserName(Integer userId) {
        if (userId == null) return "";
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getRealName() : "";
    }

    private void checkApproverPermission(FlowNode node, Integer approverId) {
        if ("user".equals(node.getApproverType())) {
            if (!approverId.equals(node.getApproverId())) {
                throw new BusinessException("您不是当前节点的审批人");
            }
        } else if ("role".equals(node.getApproverType())) {
            List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getUserId, approverId)
                            .eq(SysUserRole::getRoleId, node.getApproverId()));
            if (userRoles.isEmpty()) {
                throw new BusinessException("您不是当前节点的审批人");
            }
        }
    }

    private List<FlowNode> parseNodes(String nodesJson) {
        if (nodesJson == null || nodesJson.isBlank()) return List.of();
        try {
            return objectMapper.readValue(nodesJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    /**
     * 审批流程节点
     */
    @Data
    public static class FlowNode {
        private Integer nodeOrder;
        private String nodeName;
        /** user / role */
        private String approverType;
        private Integer approverId;
    }
}
