package com.pig4cloud.pig.eh.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.eh.dto.ApprovalActionDTO;
import com.pig4cloud.pig.eh.dto.ReturnSignDTO;
import com.pig4cloud.pig.eh.dto.VisitRecordUpsertDTO;
import com.pig4cloud.pig.eh.entity.EhApprovalNode;
import com.pig4cloud.pig.eh.entity.EhApply;
import com.pig4cloud.pig.eh.entity.EhApplyVisitor;
import com.pig4cloud.pig.eh.entity.EhVisitRecord;
import com.pig4cloud.pig.eh.mapper.EhApprovalNodeMapper;
import com.pig4cloud.pig.eh.mapper.EhApplyMapper;
import com.pig4cloud.pig.eh.mapper.EhApplyVisitorMapper;
import com.pig4cloud.pig.eh.mapper.EhChangeLogMapper;
import com.pig4cloud.pig.eh.mapper.EhOpportunityMapper;
import com.pig4cloud.pig.eh.mapper.EhVisitPhotoMapper;
import com.pig4cloud.pig.eh.mapper.EhVisitRecordMapper;
import com.pig4cloud.pig.eh.service.EhAdminAggregateService;
import com.pig4cloud.pig.eh.vo.AdminApplyPageVO;
import com.pig4cloud.pig.eh.vo.AdminChangeLogVO;
import com.pig4cloud.pig.eh.vo.AdminOpportunityVO;
import com.pig4cloud.pig.eh.vo.AdminVisitAggregateVO;
import com.pig4cloud.pig.eh.vo.AdminVisitPhotoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EhAdminAggregateServiceImpl implements EhAdminAggregateService {

	private final EhApplyMapper ehApplyMapper;
	private final EhApplyVisitorMapper ehApplyVisitorMapper;
	private final EhApprovalNodeMapper ehApprovalNodeMapper;
	private final EhVisitRecordMapper ehVisitRecordMapper;
	private final EhVisitPhotoMapper ehVisitPhotoMapper;
	private final EhOpportunityMapper ehOpportunityMapper;
	private final EhChangeLogMapper ehChangeLogMapper;

	@Override
	public List<AdminApplyPageVO> queryApplyAggregateList(String status) {
		List<AdminApplyPageVO> list = ehApplyMapper.selectAdminApplyBaseList(status);
		fillApplyChildren(list);
		return list;
	}

	@Override
	public List<AdminApplyPageVO> queryApprovalTodoList() {
		List<AdminApplyPageVO> list = ehApplyMapper.selectAdminApplyBaseList("1");
		fillApplyChildren(list);
		return list;
	}

	@Override
	public List<AdminVisitAggregateVO> queryVisitAggregateList() {
		return ehVisitRecordMapper.selectAdminVisitAggregateList();
	}

	@Override
	public List<AdminVisitPhotoVO> queryVisitPhotoAggregateList() {
		return ehVisitPhotoMapper.selectAdminVisitPhotoList();
	}

	@Override
	public List<AdminOpportunityVO> queryOpportunityAggregateList() {
		return ehOpportunityMapper.selectAdminOpportunityList();
	}

	@Override
	public List<AdminChangeLogVO> queryChangeLogAggregateList() {
		return ehChangeLogMapper.selectAdminChangeLogList();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean upsertVisitRecord(VisitRecordUpsertDTO dto) {
		EhVisitRecord entity;
		if (dto.getId() != null) {
			entity = ehVisitRecordMapper.selectById(dto.getId());
			if (entity == null) {
				return false;
			}
		} else {
			entity = ehVisitRecordMapper.selectOne(
				Wrappers.<EhVisitRecord>lambdaQuery()
					.eq(EhVisitRecord::getApplyId, dto.getApplyId())
					.last("limit 1")
			);
			if (entity == null) {
				entity = new EhVisitRecord();
				entity.setApplyId(dto.getApplyId());
			}
		}
		entity.setActualVisitTime(dto.getActualVisitTime());
		entity.setActualVisitCount(dto.getActualVisitCount());
		entity.setOpportunityCode(dto.getOpportunityCode());
		entity.setOurLeaderLevel(dto.getOurLeaderLevel());
		entity.setStatus("1");
		boolean ok = entity.getId() == null ? ehVisitRecordMapper.insert(entity) > 0 : ehVisitRecordMapper.updateById(entity) > 0;
		if (ok) {
			EhApply apply = new EhApply();
			apply.setId(dto.getApplyId());
			apply.setActualCount(dto.getActualVisitCount());
			apply.setRemark(dto.getRemark());
			ehApplyMapper.updateById(apply);
		}
		return ok;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean signVisitReturn(ReturnSignDTO dto) {
		EhVisitRecord entity = ehVisitRecordMapper.selectById(dto.getVisitRecordId());
		if (entity == null) {
			return false;
		}
		entity.setReturnSigner(dto.getReturnSigner());
		entity.setReturnTime(dto.getReturnTime());
		entity.setStatus("2");
		boolean ok = ehVisitRecordMapper.updateById(entity) > 0;
		if (ok) {
			EhApply apply = new EhApply();
			apply.setId(entity.getApplyId());
			apply.setReturnSigner(dto.getReturnSigner());
			apply.setReturnTime(dto.getReturnTime());
			ehApplyMapper.updateById(apply);
		}
		return ok;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean approveOrRejectOrTransfer(ApprovalActionDTO dto) {
		if (dto == null || !StringUtils.hasText(dto.getAction())) {
			throw new IllegalArgumentException("审批参数缺失：action 不能为空");
		}
		String action = dto.getAction().trim().toLowerCase();
		if (!"approve".equals(action) && !"reject".equals(action) && !"forward".equals(action)) {
			throw new IllegalArgumentException("审批动作不合法，仅支持 approve/reject/forward");
		}

		EhApprovalNode node = null;
		if (dto.getNodeId() != null) {
			node = ehApprovalNodeMapper.selectById(dto.getNodeId());
		}
		if (node == null && dto.getApplyId() != null) {
			List<EhApprovalNode> currentNodes = ehApprovalNodeMapper.selectList(
				Wrappers.<EhApprovalNode>lambdaQuery()
					.eq(EhApprovalNode::getApplyId, dto.getApplyId())
					.orderByAsc(EhApprovalNode::getNodeLevel)
			);
			node = currentNodes.stream()
				.filter(n -> !"approved".equals(n.getStatus()) && !"rejected".equals(n.getStatus()) && !"transferred".equals(n.getStatus()))
				.findFirst()
				.orElse(null);
		}
		if (node == null) {
			// Compatibility fallback: some historical records only keep apply status without pending nodes.
			if (dto.getApplyId() != null && ("approve".equals(action) || "reject".equals(action))) {
				EhApply apply = ehApplyMapper.selectById(dto.getApplyId());
				if (apply == null) {
					throw new IllegalStateException("未找到申请单，applyId=" + dto.getApplyId());
				}
				String targetStatus = "approve".equals(action) ? "2" : "3";
				if (targetStatus.equals(apply.getStatus())) {
					return true;
				}
				EhApply patch = new EhApply();
				patch.setId(apply.getId());
				patch.setStatus(targetStatus);
				boolean updated = ehApplyMapper.updateById(patch) > 0;
				if (!updated) {
					throw new IllegalStateException("节点缺失且申请状态更新失败，applyId=" + dto.getApplyId());
				}
				return true;
			}
			throw new IllegalStateException("未找到可处理的审批节点，applyId=" + dto.getApplyId() + ", nodeId=" + dto.getNodeId());
		}
		if ("approved".equals(node.getStatus()) || "rejected".equals(node.getStatus())) {
			throw new IllegalStateException("当前审批节点已处理，请刷新后重试");
		}

		node.setOpinion(dto.getComment());
		node.setActionTime(LocalDateTime.now());
		if ("approve".equals(action)) {
			node.setAction("agree");
			node.setStatus("approved");
		} else if ("reject".equals(action)) {
			node.setAction("reject");
			node.setStatus("rejected");
		} else {
			node.setAction("transfer");
			node.setStatus("transferred");
		}
		boolean ok = ehApprovalNodeMapper.updateById(node) > 0;
		if (!ok) {
			throw new IllegalStateException("审批节点更新失败");
		}
		final Long currentNodeId = node.getId();

		Long applyId = dto.getApplyId() != null ? dto.getApplyId() : node.getApplyId();
		if (applyId == null) {
			return true;
		}

		String targetApplyStatus = null;
		if ("approve".equals(action)) {
			List<EhApprovalNode> remainNodes = ehApprovalNodeMapper.selectList(
				Wrappers.<EhApprovalNode>lambdaQuery()
					.eq(EhApprovalNode::getApplyId, applyId)
					.orderByAsc(EhApprovalNode::getNodeLevel)
			);
			EhApprovalNode nextWaitingNode = remainNodes.stream()
				.filter(n -> !n.getId().equals(currentNodeId))
				.filter(n -> !"approved".equals(n.getStatus()) && !"rejected".equals(n.getStatus()) && !"transferred".equals(n.getStatus()))
				.findFirst()
				.orElse(null);
			if (nextWaitingNode != null) {
				nextWaitingNode.setStatus("pending");
				nextWaitingNode.setAction("");
				ehApprovalNodeMapper.updateById(nextWaitingNode);
			}
			long unfinishedCount = ehApprovalNodeMapper.selectCount(
				Wrappers.<EhApprovalNode>lambdaQuery()
					.eq(EhApprovalNode::getApplyId, applyId)
					.notIn(EhApprovalNode::getStatus, "approved", "rejected", "transferred")
			);
			targetApplyStatus = unfinishedCount == 0 ? "2" : "1";
		} else if ("reject".equals(action)) {
			targetApplyStatus = "3";
		} else {
			targetApplyStatus = "1";
		}

		EhApply currentApply = ehApplyMapper.selectById(applyId);
		if (currentApply == null) {
			throw new IllegalStateException("申请单不存在或已删除");
		}
		// Node update has completed; if apply status remains unchanged, treat as success.
		if (targetApplyStatus.equals(currentApply.getStatus())) {
			return true;
		}

		EhApply apply = new EhApply();
		apply.setId(applyId);
		apply.setStatus(targetApplyStatus);
		boolean updated = ehApplyMapper.updateById(apply) > 0;
		if (!updated) {
			throw new IllegalStateException("申请状态更新失败");
		}
		return true;
	}

	private void fillApplyChildren(List<AdminApplyPageVO> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		List<Long> applyIds = list.stream().map(AdminApplyPageVO::getId).collect(Collectors.toList());
		List<EhApplyVisitor> visitors = ehApplyVisitorMapper.selectList(
			Wrappers.<EhApplyVisitor>lambdaQuery().in(EhApplyVisitor::getApplyId, applyIds)
		);
		List<EhApprovalNode> nodes = ehApprovalNodeMapper.selectList(
			Wrappers.<EhApprovalNode>lambdaQuery().in(EhApprovalNode::getApplyId, applyIds)
		);
		Map<Long, List<EhApplyVisitor>> visitorMap = visitors.stream().collect(Collectors.groupingBy(EhApplyVisitor::getApplyId));
		Map<Long, List<EhApprovalNode>> nodeMap = nodes.stream()
			.sorted(Comparator.comparing(EhApprovalNode::getNodeLevel))
			.collect(Collectors.groupingBy(EhApprovalNode::getApplyId));

		Map<Integer, String> roleMap = Map.of(
			1, "部门领导",
			2, "展厅主管",
			3, "分管领导"
		);

		for (AdminApplyPageVO vo : list) {
			List<AdminApplyPageVO.VisitorVO> visitorVOList = new ArrayList<>();
			for (EhApplyVisitor v : visitorMap.getOrDefault(vo.getId(), List.of())) {
				AdminApplyPageVO.VisitorVO item = new AdminApplyPageVO.VisitorVO();
				item.setId(v.getId());
				item.setName(v.getVisitorCompany());
				item.setTitle(v.getIndustry());
				item.setStrategic("1".equals(v.getIsKeyCustomer()));
				item.setStrategicLevel(v.getKeyCustomerLevel());
				visitorVOList.add(item);
			}
			vo.setVisitors(visitorVOList);

			List<AdminApplyPageVO.ApprovalNodeVO> nodeVOList = new ArrayList<>();
			for (EhApprovalNode n : nodeMap.getOrDefault(vo.getId(), List.of())) {
				AdminApplyPageVO.ApprovalNodeVO nodeVO = new AdminApplyPageVO.ApprovalNodeVO();
				nodeVO.setId(n.getId());
				nodeVO.setLevel(n.getNodeLevel());
				nodeVO.setRole(roleMap.getOrDefault(n.getNodeLevel(), "审批节点"));
				nodeVO.setName(n.getApprover());
				nodeVO.setAction(convertNodeAction(n.getAction(), n.getStatus()));
				nodeVO.setComment(n.getOpinion());
				nodeVO.setTime(n.getActionTime());
				nodeVO.setStatus(n.getStatus());
				nodeVOList.add(nodeVO);
			}
			vo.setApprovalNodes(nodeVOList);
		}
	}

	private String convertNodeAction(String action, String status) {
		if ("approved".equals(status) || "agree".equals(action)) {
			return "approved";
		}
		if ("rejected".equals(status) || "reject".equals(action)) {
			return "rejected";
		}
		if ("pending".equals(status)) {
			return "pending";
		}
		return "waiting";
	}
}
