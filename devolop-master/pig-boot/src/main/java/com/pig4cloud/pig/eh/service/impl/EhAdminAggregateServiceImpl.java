package com.pig4cloud.pig.eh.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.eh.dto.ApprovalActionDTO;
import com.pig4cloud.pig.eh.dto.ReturnSignDTO;
import com.pig4cloud.pig.eh.dto.VisitPhotoBatchDTO;
import com.pig4cloud.pig.eh.dto.VisitRecordUpsertDTO;
import com.pig4cloud.pig.eh.entity.EhApprovalNode;
import com.pig4cloud.pig.eh.entity.EhApply;
import com.pig4cloud.pig.eh.entity.EhApplyVisitor;
import com.pig4cloud.pig.eh.entity.EhVisitPhoto;
import com.pig4cloud.pig.eh.entity.EhVisitRecord;
import com.pig4cloud.pig.eh.mapper.EhApprovalNodeMapper;
import com.pig4cloud.pig.eh.mapper.EhApplyMapper;
import com.pig4cloud.pig.eh.mapper.EhApplyVisitorMapper;
import com.pig4cloud.pig.eh.mapper.EhChangeLogMapper;
import com.pig4cloud.pig.eh.mapper.EhOpportunityMapper;
import com.pig4cloud.pig.eh.mapper.EhVisitPhotoMapper;
import com.pig4cloud.pig.eh.mapper.EhVisitRecordMapper;
import com.pig4cloud.pig.eh.service.EhAdminAggregateService;
import com.pig4cloud.pig.eh.service.EhNotificationService;
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
import java.util.Objects;
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
	private final EhNotificationService ehNotificationService;

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
		List<AdminVisitPhotoVO> list = ehVisitPhotoMapper.selectAdminVisitPhotoList();
		if (list.isEmpty()) {
			return list;
		}
		List<Long> visitIds = list.stream()
			.map(AdminVisitPhotoVO::getVisitId)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		if (visitIds.isEmpty()) {
			return list;
		}
		Map<Long, List<EhVisitPhoto>> photoMap = ehVisitPhotoMapper.selectList(
			Wrappers.<EhVisitPhoto>lambdaQuery()
				.in(EhVisitPhoto::getVisitId, visitIds)
				.orderByDesc(EhVisitPhoto::getCreateTime, EhVisitPhoto::getId)
		).stream().collect(Collectors.groupingBy(EhVisitPhoto::getVisitId));
		for (AdminVisitPhotoVO item : list) {
			List<AdminVisitPhotoVO.PhotoVO> photos = photoMap.getOrDefault(item.getVisitId(), List.of()).stream().map(photo -> {
				AdminVisitPhotoVO.PhotoVO row = new AdminVisitPhotoVO.PhotoVO();
				row.setId(photo.getId());
				row.setFileUrl(photo.getFileUrl());
				row.setFileName(photo.getFileName());
				row.setFileSize(photo.getFileSize());
				return row;
			}).collect(Collectors.toList());
			item.setPhotos(photos);
			item.setPhotoCount(photos.size());
		}
		return list;
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
			EhApply apply = ehApplyMapper.selectById(entity.getApplyId());
			EhApply patch = new EhApply();
			patch.setId(entity.getApplyId());
			patch.setReturnSigner(dto.getReturnSigner());
			patch.setReturnTime(dto.getReturnTime());
			ehApplyMapper.updateById(patch);
			if (apply != null) {
				notifyApplicant(
					apply,
					"system",
					"归还签收已完成",
					String.format("您的展厅申请《%s》已完成归还签收。", defaultString(apply.getSubject(), "未命名申请"))
				);
			}
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
			node = ehApprovalNodeMapper.selectList(
				Wrappers.<EhApprovalNode>lambdaQuery()
					.eq(EhApprovalNode::getApplyId, dto.getApplyId())
					.orderByAsc(EhApprovalNode::getNodeLevel, EhApprovalNode::getId)
			).stream()
				.filter(item -> !"approved".equals(item.getStatus()) && !"rejected".equals(item.getStatus()) && !"transferred".equals(item.getStatus()))
				.findFirst()
				.orElse(null);
		}
		if (node == null) {
			return handleLegacyApproval(dto, action);
		}
		if ("approved".equals(node.getStatus()) || "rejected".equals(node.getStatus())) {
			throw new IllegalStateException("当前审批节点已处理，请刷新后重试");
		}
		if ("forward".equals(action) && !StringUtils.hasText(dto.getTargetApprover())) {
			throw new IllegalArgumentException("转交审批时必须选择目标审批人");
		}

		Long applyId = dto.getApplyId() != null ? dto.getApplyId() : node.getApplyId();
		EhApply currentApply = ehApplyMapper.selectById(applyId);
		if (currentApply == null) {
			throw new IllegalStateException("申请单不存在或已删除");
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
		if (ehApprovalNodeMapper.updateById(node) <= 0) {
			throw new IllegalStateException("审批节点更新失败");
		}

		String targetApplyStatus;
		if ("approve".equals(action)) {
			activateNextNode(applyId, node.getId(), null);
			long unfinishedCount = ehApprovalNodeMapper.selectCount(
				Wrappers.<EhApprovalNode>lambdaQuery()
					.eq(EhApprovalNode::getApplyId, applyId)
					.notIn(EhApprovalNode::getStatus, "approved", "rejected", "transferred")
			);
			targetApplyStatus = unfinishedCount == 0 ? "2" : "1";
			notifyApplicant(
				currentApply,
				unfinishedCount == 0 ? "approved" : "approval",
				unfinishedCount == 0 ? "申请已通过" : "申请进入下一审批环节",
				unfinishedCount == 0
					? String.format("您的展厅申请《%s》已审批通过。", defaultString(currentApply.getSubject(), "未命名申请"))
					: String.format("您的展厅申请《%s》已进入下一审批环节。", defaultString(currentApply.getSubject(), "未命名申请"))
			);
		} else if ("reject".equals(action)) {
			targetApplyStatus = "3";
			notifyApplicant(
				currentApply,
				"rejected",
				"申请已驳回",
				String.format("您的展厅申请《%s》已被驳回。", defaultString(currentApply.getSubject(), "未命名申请"))
			);
		} else {
			activateNextNode(applyId, node.getId(), dto.getTargetApprover());
			targetApplyStatus = "1";
			ehNotificationService.createNotification(
				applyId,
				dto.getTargetApprover().trim(),
				"approval",
				"收到转交审批",
				String.format("您收到展厅申请《%s》的审批转交，请及时处理。", defaultString(currentApply.getSubject(), "未命名申请")),
				buildApplyRoute(applyId)
			);
		}

		if (!targetApplyStatus.equals(currentApply.getStatus())) {
			EhApply apply = new EhApply();
			apply.setId(applyId);
			apply.setStatus(targetApplyStatus);
			if (ehApplyMapper.updateById(apply) <= 0) {
				throw new IllegalStateException("申请状态更新失败");
			}
		}
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveVisitPhotos(VisitPhotoBatchDTO dto) {
		if (dto == null || dto.getVisitId() == null) {
			return false;
		}
		if (dto.getPhotos() == null || dto.getPhotos().isEmpty()) {
			return true;
		}
		for (VisitPhotoBatchDTO.PhotoDTO photo : dto.getPhotos()) {
			if (!StringUtils.hasText(photo.getFileUrl())) {
				continue;
			}
			EhVisitPhoto entity = new EhVisitPhoto();
			entity.setVisitId(dto.getVisitId());
			entity.setFileUrl(photo.getFileUrl());
			entity.setFileName(photo.getFileName());
			entity.setFileSize(photo.getFileSize());
			ehVisitPhotoMapper.insert(entity);
		}
		return true;
	}

	private boolean handleLegacyApproval(ApprovalActionDTO dto, String action) {
		if (dto.getApplyId() == null || "forward".equals(action)) {
			throw new IllegalStateException("未找到可处理的审批节点，applyId=" + dto.getApplyId() + ", nodeId=" + dto.getNodeId());
		}
		EhApply apply = ehApplyMapper.selectById(dto.getApplyId());
		if (apply == null) {
			throw new IllegalStateException("未找到申请单，applyId=" + dto.getApplyId());
		}
		String targetStatus = "approve".equals(action) ? "2" : "3";
		if (!targetStatus.equals(apply.getStatus())) {
			EhApply patch = new EhApply();
			patch.setId(apply.getId());
			patch.setStatus(targetStatus);
			if (ehApplyMapper.updateById(patch) <= 0) {
				throw new IllegalStateException("节点缺失且申请状态更新失败，applyId=" + dto.getApplyId());
			}
		}
		notifyApplicant(
			apply,
			"approve".equals(action) ? "approved" : "rejected",
			"approve".equals(action) ? "申请已通过" : "申请已驳回",
			String.format("您的展厅申请《%s》已%s。", defaultString(apply.getSubject(), "未命名申请"), "approve".equals(action) ? "审批通过" : "驳回")
		);
		return true;
	}

	private void activateNextNode(Long applyId, Long currentNodeId, String targetApprover) {
		EhApprovalNode nextNode = ehApprovalNodeMapper.selectList(
			Wrappers.<EhApprovalNode>lambdaQuery()
				.eq(EhApprovalNode::getApplyId, applyId)
				.orderByAsc(EhApprovalNode::getNodeLevel, EhApprovalNode::getId)
		).stream()
			.filter(item -> !item.getId().equals(currentNodeId))
			.filter(item -> !"approved".equals(item.getStatus()) && !"rejected".equals(item.getStatus()) && !"transferred".equals(item.getStatus()))
			.findFirst()
			.orElse(null);
		if (nextNode == null) {
			return;
		}
		nextNode.setStatus("pending");
		nextNode.setAction("");
		if (StringUtils.hasText(targetApprover)) {
			nextNode.setApprover(targetApprover.trim());
		}
		ehApprovalNodeMapper.updateById(nextNode);
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
			List<AdminApplyPageVO.VisitorVO> visitorVoList = new ArrayList<>();
			for (EhApplyVisitor visitor : visitorMap.getOrDefault(vo.getId(), List.of())) {
				AdminApplyPageVO.VisitorVO item = new AdminApplyPageVO.VisitorVO();
				item.setId(visitor.getId());
				item.setName(defaultString(visitor.getCustomerCode(), visitor.getVisitorCompany()));
				item.setTitle(visitor.getIndustry());
				item.setUnit(visitor.getVisitorCompany());
				item.setStrategic("1".equals(visitor.getIsKeyCustomer()));
				item.setStrategicLevel(visitor.getKeyCustomerLevel());
				visitorVoList.add(item);
			}
			vo.setVisitors(visitorVoList);

			List<AdminApplyPageVO.ApprovalNodeVO> nodeVoList = new ArrayList<>();
			for (EhApprovalNode node : nodeMap.getOrDefault(vo.getId(), List.of())) {
				AdminApplyPageVO.ApprovalNodeVO item = new AdminApplyPageVO.ApprovalNodeVO();
				item.setId(node.getId());
				item.setLevel(node.getNodeLevel());
				item.setRole(roleMap.getOrDefault(node.getNodeLevel(), "审批节点"));
				item.setName(node.getApprover());
				item.setAction(convertNodeAction(node.getAction(), node.getStatus()));
				item.setComment(node.getOpinion());
				item.setTime(node.getActionTime());
				item.setStatus(node.getStatus());
				nodeVoList.add(item);
			}
			vo.setApprovalNodes(nodeVoList);
		}
	}

	private String convertNodeAction(String action, String status) {
		if ("approved".equals(status) || "agree".equals(action)) {
			return "approved";
		}
		if ("rejected".equals(status) || "reject".equals(action)) {
			return "rejected";
		}
		if ("transferred".equals(status) || "transfer".equals(action)) {
			return "transferred";
		}
		if ("pending".equals(status)) {
			return "pending";
		}
		return "waiting";
	}

	private void notifyApplicant(EhApply apply, String type, String title, String content) {
		String recipient = defaultString(apply.getCreateBy(), apply.getApplicant());
		ehNotificationService.createNotification(apply.getId(), recipient, type, title, content, buildApplyRoute(apply.getId()));
	}

	private String buildApplyRoute(Long applyId) {
		return applyId == null ? "/mine" : "/apply/" + applyId;
	}

	private String defaultString(String primary, String fallback) {
		return StringUtils.hasText(primary) ? primary : fallback;
	}

}
