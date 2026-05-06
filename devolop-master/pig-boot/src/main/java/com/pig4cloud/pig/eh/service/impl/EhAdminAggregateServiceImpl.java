package com.pig4cloud.pig.eh.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.common.file.core.FileTemplate;
import com.pig4cloud.pig.eh.dto.ApprovalActionDTO;
import com.pig4cloud.pig.eh.dto.ReturnSignDTO;
import com.pig4cloud.pig.eh.dto.VisitPhotoBatchDTO;
import com.pig4cloud.pig.eh.dto.VisitRecordUpsertDTO;
import com.pig4cloud.pig.eh.entity.EhApprovalNode;
import com.pig4cloud.pig.eh.entity.EhApply;
import com.pig4cloud.pig.eh.entity.EhApplyVisitor;
import com.pig4cloud.pig.eh.entity.EhOpportunity;
import com.pig4cloud.pig.eh.entity.EhVisitPhoto;
import com.pig4cloud.pig.eh.entity.EhVisitRecord;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.eh.mapper.EhApprovalNodeMapper;
import com.pig4cloud.pig.eh.mapper.EhApplyMapper;
import com.pig4cloud.pig.eh.mapper.EhApplyVisitorMapper;
import com.pig4cloud.pig.eh.mapper.EhChangeLogMapper;
import com.pig4cloud.pig.eh.mapper.EhOpportunityMapper;
import com.pig4cloud.pig.eh.mapper.EhVisitPhotoMapper;
import com.pig4cloud.pig.eh.mapper.EhVisitRecordMapper;
import com.pig4cloud.pig.eh.service.impl.EhApplyServiceImpl;
import com.pig4cloud.pig.eh.service.EhAdminAggregateService;
import com.pig4cloud.pig.eh.service.EhNotificationService;
import com.pig4cloud.pig.eh.vo.AdminApplyPageVO;
import com.pig4cloud.pig.eh.vo.AdminChangeLogVO;
import com.pig4cloud.pig.eh.vo.AdminOpportunityVO;
import com.pig4cloud.pig.eh.vo.AdminVisitAggregateVO;
import com.pig4cloud.pig.eh.vo.AdminVisitPhotoVO;
import com.pig4cloud.pig.eh.vo.EhVisitRecordExportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
	private final EhApplyServiceImpl ehApplyService;
	private final FileTemplate fileTemplate;

	private static final DateTimeFormatter EXPORT_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy年-M月");

	@Override
	public List<AdminApplyPageVO> queryApplyAggregateList(String status) {
		List<AdminApplyPageVO> list = ehApplyMapper.selectAdminApplyBaseList(status);
		fillApplyChildren(list);
		return list;
	}

	@Override
	public List<AdminApplyPageVO> queryApprovalTodoList() {
		List<AdminApplyPageVO> list = ehApplyMapper.selectAdminApplyBaseListIn(java.util.List.of("1", "5"));
		fillApplyChildren(list);
		return list;
	}

	@Override
	public List<AdminVisitAggregateVO> queryVisitAggregateList() {
		return ehVisitRecordMapper.selectAdminVisitAggregateList();
	}

	@Override
	public List<EhVisitRecordExportVO> exportVisitRecordList() {
		List<EhVisitRecordExportVO> list = ehVisitRecordMapper.selectVisitExportList();
		for (int i = 0; i < list.size(); i++) {
			EhVisitRecordExportVO row = list.get(i);
			row.setRowNo(i + 1);
			row.setVisitStatus(convertVisitStatus(row.getVisitStatus()));
		}
		return list;
	}

	private String convertVisitStatus(String code) {
		if (code == null) return "待录入";
		return switch (code) {
			case "1" -> "已录入";
			case "2" -> "已归还";
			default -> "待录入";
		};
	}

	@Override
	public List<AdminVisitPhotoVO> queryVisitPhotoAggregateList() {
		List<AdminVisitPhotoVO> list = ehVisitPhotoMapper.selectAdminVisitPhotoList();
		if (list.isEmpty()) {
			return list;
		}
		List<Long> applyIds = list.stream()
			.map(AdminVisitPhotoVO::getApplyId)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		if (applyIds.isEmpty()) {
			return list;
		}
		Map<Long, Long> visitApplyMap = ehVisitRecordMapper.selectList(
			Wrappers.<EhVisitRecord>lambdaQuery().in(EhVisitRecord::getApplyId, applyIds)
		).stream().collect(Collectors.toMap(EhVisitRecord::getId, EhVisitRecord::getApplyId, (left, right) -> left));
		List<EhVisitPhoto> photoList = visitApplyMap.isEmpty()
			? ehVisitPhotoMapper.selectList(
				Wrappers.<EhVisitPhoto>lambdaQuery()
					.in(EhVisitPhoto::getApplyId, applyIds)
					.orderByDesc(EhVisitPhoto::getCreateTime, EhVisitPhoto::getId))
			: ehVisitPhotoMapper.selectList(
				Wrappers.<EhVisitPhoto>lambdaQuery()
					.and(wrapper -> wrapper.in(EhVisitPhoto::getApplyId, applyIds)
						.or()
						.in(EhVisitPhoto::getVisitId, visitApplyMap.keySet()))
					.orderByDesc(EhVisitPhoto::getCreateTime, EhVisitPhoto::getId));
		Map<Long, List<EhVisitPhoto>> photoMap = photoList.stream()
			.filter(photo -> photo.getApplyId() != null || visitApplyMap.get(photo.getVisitId()) != null)
			.collect(Collectors.groupingBy(photo -> photo.getApplyId() != null ? photo.getApplyId() : visitApplyMap.get(photo.getVisitId())));
		for (AdminVisitPhotoVO item : list) {
			List<AdminVisitPhotoVO.PhotoVO> photos = photoMap.getOrDefault(item.getApplyId(), List.of()).stream().map(photo -> {
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
	public void exportVisitPhotosZip(Long applyId, HttpServletResponse response) {
		List<AdminVisitPhotoVO> list = queryVisitPhotoAggregateList().stream()
			.filter(item -> applyId == null || Objects.equals(item.getApplyId(), applyId))
			.filter(item -> item.getPhotos() != null && !item.getPhotos().isEmpty())
			.collect(Collectors.toList());
		String fileName = applyId == null ? "展厅现场照片.zip" : "展厅现场照片-" + applyId + ".zip";
		response.setContentType("application/zip");
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodeFileName(fileName));
		Set<String> entryNames = new HashSet<>();
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream(), StandardCharsets.UTF_8)) {
			int written = 0;
			for (AdminVisitPhotoVO item : list) {
				written += writePhotoZipEntries(zipOutputStream, entryNames, item);
			}
			if (written == 0) {
				ZipEntry entry = new ZipEntry("暂无可导出的照片.txt");
				zipOutputStream.putNextEntry(entry);
				zipOutputStream.write("暂无可导出的照片".getBytes(StandardCharsets.UTF_8));
				zipOutputStream.closeEntry();
			}
		}
		catch (IOException e) {
			throw new IllegalStateException("导出现场照片失败", e);
		}
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
		if (dto == null || dto.getApplyId() == null) {
			return false;
		}
		EhApply sourceApply = ehApplyMapper.selectById(dto.getApplyId());
		if (sourceApply == null) {
			return false;
		}
		if (!"2".equals(sourceApply.getStatus()) && !"5".equals(sourceApply.getStatus())) {
			throw new IllegalStateException("仅已通过或已改期申请允许录入参观留存");
		}
		EhVisitRecord entity;
		if (dto.getId() != null) {
			entity = ehVisitRecordMapper.selectById(dto.getId());
			if (entity == null) {
				return false;
			}
			if (!dto.getApplyId().equals(entity.getApplyId())) {
				throw new IllegalArgumentException("留存记录与申请单不匹配");
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
			upsertOpportunity(entity, dto.getRemark());
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
		if (dto.getApplyId() != null && !dto.getApplyId().equals(node.getApplyId())) {
			throw new IllegalArgumentException("审批节点与申请单不匹配");
		}
		if ("approved".equals(node.getStatus()) || "rejected".equals(node.getStatus()) || "transferred".equals(node.getStatus())) {
			throw new IllegalStateException("当前审批节点已处理，请刷新后重试");
		}
		if (!"pending".equals(node.getStatus())) {
			throw new IllegalStateException("只能处理当前待审批节点");
		}
		if ("forward".equals(action) && !StringUtils.hasText(dto.getTargetApprover())) {
			throw new IllegalArgumentException("转交审批时必须选择目标审批人");
		}

		Long applyId = dto.getApplyId() != null ? dto.getApplyId() : node.getApplyId();
		EhApply currentApply = ehApplyMapper.selectById(applyId);
		if (currentApply == null) {
			throw new IllegalStateException("申请单不存在或已删除");
		}
		if (!"1".equals(currentApply.getStatus()) && !"5".equals(currentApply.getStatus())) {
			throw new IllegalStateException("仅待审批申请允许审批处理");
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

		String adminOperator = currentUsername();
		String targetApplyStatus;
		if ("approve".equals(action)) {
			activateNextNode(applyId, node.getId(), null);
			long unfinishedCount = ehApprovalNodeMapper.selectCount(
				Wrappers.<EhApprovalNode>lambdaQuery()
					.eq(EhApprovalNode::getApplyId, applyId)
					.notIn(EhApprovalNode::getStatus, "approved", "rejected", "transferred")
			);
			targetApplyStatus = unfinishedCount == 0 ? "2" : "1";
			ehApplyService.recordHistory(applyId, "approve", "审批通过", adminOperator, dto.getComment());
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
			ehApplyService.recordHistory(applyId, "reject", "审批驳回", adminOperator, dto.getComment());
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
		if (dto == null || (dto.getApplyId() == null && dto.getVisitId() == null)) {
			return false;
		}
		Long applyId = dto.getApplyId();
		if (applyId == null) {
			EhVisitRecord visitRecord = ehVisitRecordMapper.selectById(dto.getVisitId());
			if (visitRecord == null) {
				return false;
			}
			applyId = visitRecord.getApplyId();
		}
		if (ehApplyMapper.selectById(applyId) == null) {
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
			entity.setApplyId(applyId);
			entity.setFileUrl(photo.getFileUrl());
			entity.setFileName(photo.getFileName());
			entity.setFileSize(photo.getFileSize());
			ehVisitPhotoMapper.insert(entity);
		}
		return true;
	}

	@Override
	public void exportVisitPhotoDataTable(HttpServletResponse response) {
		try (Workbook workbook = WorkbookFactory.create(openExportTemplate())) {
			Sheet sheet = workbook.getSheet("Sheet2");
			if (sheet == null) {
				sheet = workbook.getSheetAt(0);
			}
			List<EhApply> applies = ehApplyMapper.selectList(
				Wrappers.<EhApply>lambdaQuery()
					.eq(EhApply::getDelFlag, "0")
					.orderByAsc(EhApply::getStartTime)
			);
			Map<Long, EhApplyVisitor> visitorMap = firstVisitorMap(applies);
			Map<Long, EhVisitRecord> visitRecordMap = firstVisitRecordMap(applies);
			Map<Long, EhVisitPhoto> photoMap = firstPhotoMap(applies, visitRecordMap);
			clearTemplateRows(sheet);
			Row templateRow = sheet.getRow(2);
			int rowIndex = 2;
			int serialNo = 1;
			for (EhApply apply : applies) {
				Row row = sheet.getRow(rowIndex);
				if (row == null) {
					row = sheet.createRow(rowIndex);
				}
				copyRowStyle(templateRow, row);
				fillExportRow(workbook, sheet, row, serialNo++, apply, visitorMap.get(apply.getId()),
					visitRecordMap.get(apply.getId()), photoMap.get(apply.getId()));
				rowIndex++;
			}
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			String fileName = URLEncoder.encode("展厅参观数据表.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
			response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
			workbook.write(response.getOutputStream());
		}
		catch (Exception e) {
			throw new IllegalStateException("导出展厅参观数据表失败", e);
		}
	}

	private void upsertOpportunity(EhVisitRecord visitRecord, String remark) {
		if (visitRecord == null || !StringUtils.hasText(visitRecord.getOpportunityCode())) {
			return;
		}
		String opportunityCode = visitRecord.getOpportunityCode().trim();
		if ("无".equals(opportunityCode)) {
			return;
		}
		EhOpportunity opportunity = ehOpportunityMapper.selectOne(
			Wrappers.<EhOpportunity>lambdaQuery()
				.eq(EhOpportunity::getOpportunityCode, opportunityCode)
				.last("limit 1")
		);
		if (opportunity == null) {
			opportunity = new EhOpportunity();
			opportunity.setOpportunityCode(opportunityCode);
			opportunity.setStatus("clue");
		}
		opportunity.setVisitId(visitRecord.getId());
		opportunity.setApplyId(visitRecord.getApplyId());
		if (StringUtils.hasText(remark)) {
			opportunity.setRemark(remark);
		}
		if (opportunity.getId() == null) {
			ehOpportunityMapper.insert(opportunity);
		} else {
			ehOpportunityMapper.updateById(opportunity);
		}
	}

	private InputStream openExportTemplate() throws IOException {
		ClassPathResource resource = new ClassPathResource("templates/eh/展厅参观数据表.xlsx");
		if (resource.exists()) {
			return resource.getInputStream();
		}
		Path localTemplate = Path.of(System.getProperty("user.dir")).resolve("../展厅参观数据表.xlsx").normalize();
		if (Files.exists(localTemplate)) {
			return Files.newInputStream(localTemplate);
		}
		return Files.newInputStream(Path.of(System.getProperty("user.dir")).resolve("展厅参观数据表.xlsx").normalize());
	}

	private Map<Long, EhApplyVisitor> firstVisitorMap(List<EhApply> applies) {
		List<Long> applyIds = applies.stream().map(EhApply::getId).filter(Objects::nonNull).collect(Collectors.toList());
		if (applyIds.isEmpty()) {
			return Map.of();
		}
		Map<Long, EhApplyVisitor> map = new HashMap<>();
		for (EhApplyVisitor visitor : ehApplyVisitorMapper.selectList(
			Wrappers.<EhApplyVisitor>lambdaQuery()
				.in(EhApplyVisitor::getApplyId, applyIds)
				.orderByAsc(EhApplyVisitor::getCreateTime, EhApplyVisitor::getId)
		)) {
			map.putIfAbsent(visitor.getApplyId(), visitor);
		}
		return map;
	}

	private Map<Long, EhVisitRecord> firstVisitRecordMap(List<EhApply> applies) {
		List<Long> applyIds = applies.stream().map(EhApply::getId).filter(Objects::nonNull).collect(Collectors.toList());
		if (applyIds.isEmpty()) {
			return Map.of();
		}
		Map<Long, EhVisitRecord> map = new HashMap<>();
		for (EhVisitRecord record : ehVisitRecordMapper.selectList(
			Wrappers.<EhVisitRecord>lambdaQuery()
				.in(EhVisitRecord::getApplyId, applyIds)
				.orderByAsc(EhVisitRecord::getCreateTime, EhVisitRecord::getId)
		)) {
			map.putIfAbsent(record.getApplyId(), record);
		}
		return map;
	}

	private Map<Long, EhVisitPhoto> firstPhotoMap(List<EhApply> applies, Map<Long, EhVisitRecord> visitRecordMap) {
		List<Long> applyIds = applies.stream().map(EhApply::getId).filter(Objects::nonNull).collect(Collectors.toList());
		if (applyIds.isEmpty()) {
			return Map.of();
		}
		Map<Long, Long> visitApplyMap = visitRecordMap.values().stream()
			.collect(Collectors.toMap(EhVisitRecord::getId, EhVisitRecord::getApplyId, (left, right) -> left));
		Map<Long, EhVisitPhoto> map = new HashMap<>();
		List<EhVisitPhoto> photos = visitApplyMap.isEmpty()
			? ehVisitPhotoMapper.selectList(
				Wrappers.<EhVisitPhoto>lambdaQuery()
					.in(EhVisitPhoto::getApplyId, applyIds)
					.orderByAsc(EhVisitPhoto::getCreateTime, EhVisitPhoto::getId))
			: ehVisitPhotoMapper.selectList(
				Wrappers.<EhVisitPhoto>lambdaQuery()
					.and(wrapper -> wrapper.in(EhVisitPhoto::getApplyId, applyIds)
						.or()
						.in(EhVisitPhoto::getVisitId, visitApplyMap.keySet()))
					.orderByAsc(EhVisitPhoto::getCreateTime, EhVisitPhoto::getId));
		for (EhVisitPhoto photo : photos) {
			Long applyId = photo.getApplyId() != null ? photo.getApplyId() : visitApplyMap.get(photo.getVisitId());
			if (applyId != null) {
				map.putIfAbsent(applyId, photo);
			}
		}
		return map;
	}

	private void clearTemplateRows(Sheet sheet) {
		for (int i = 2; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			for (int c = 0; c <= 12; c++) {
				Cell cell = row.getCell(c);
				if (cell != null) {
					cell.setBlank();
				}
			}
		}
	}

	private void copyRowStyle(Row templateRow, Row targetRow) {
		if (templateRow == null || targetRow == null) {
			return;
		}
		targetRow.setHeight(templateRow.getHeight());
		for (int i = 0; i <= 12; i++) {
			Cell sourceCell = templateRow.getCell(i);
			Cell targetCell = targetRow.getCell(i);
			if (targetCell == null) {
				targetCell = targetRow.createCell(i);
			}
			if (sourceCell != null) {
				CellStyle style = sourceCell.getCellStyle();
				targetCell.setCellStyle(style);
			}
		}
	}

	private void fillExportRow(Workbook workbook, Sheet sheet, Row row, Integer serialNo, EhApply apply,
			EhApplyVisitor visitor, EhVisitRecord visitRecord, EhVisitPhoto photo) {
		setCell(row, 0, serialNo);
		setCell(row, 1, apply.getStartTime() == null ? "" : apply.getStartTime().format(EXPORT_MONTH_FORMATTER));
		setCell(row, 2, defaultString(apply.getVisitorCompany(), ""));
		setCell(row, 3, visitor == null ? "" : defaultString(visitor.getCustomerCode(), ""));
		setCell(row, 4, defaultString(apply.getTopLeaderTitle(), ""));
		setCell(row, 5, visitor != null && "1".equals(visitor.getIsKeyCustomer()) ? "是" : "否");
		setCell(row, 6, visitor == null ? defaultString(apply.getIndustry(), "") : defaultString(visitor.getIndustry(), apply.getIndustry()));
		setCell(row, 7, visitor == null ? "" : defaultString(visitor.getKeyCustomerLevel(), ""));
		setCell(row, 8, apply.getCustomerCount() == null ? apply.getVisitorCount() : apply.getCustomerCount());
		setCell(row, 9, visitor != null && "1".equals(visitor.getIsKeyCustomer()) ? 1 : 0);
		setCell(row, 10, visitRecord == null ? "" : defaultString(visitRecord.getOpportunityCode(), ""));
		setCell(row, 12, defaultString(apply.getRemark(), ""));
		insertPhoto(workbook, sheet, row.getRowNum(), photo);
	}

	private void setCell(Row row, int columnIndex, Object value) {
		Cell cell = row.getCell(columnIndex);
		if (cell == null) {
			cell = row.createCell(columnIndex);
		}
		if (value instanceof Number number) {
			cell.setCellValue(number.doubleValue());
			return;
		}
		cell.setCellValue(value == null ? "" : String.valueOf(value));
	}

	private void insertPhoto(Workbook workbook, Sheet sheet, int rowIndex, EhVisitPhoto photo) {
		if (photo == null || !StringUtils.hasText(photo.getFileUrl())) {
			return;
		}
		byte[] bytes = readPhotoBytes(photo.getFileUrl());
		if (bytes.length == 0) {
			return;
		}
		int pictureType = photo.getFileName() != null && photo.getFileName().toLowerCase().endsWith(".png")
			? Workbook.PICTURE_TYPE_PNG : Workbook.PICTURE_TYPE_JPEG;
		int pictureIndex = workbook.addPicture(bytes, pictureType);
		CreationHelper helper = workbook.getCreationHelper();
		Drawing<?> drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setCol1(11);
		anchor.setCol2(12);
		anchor.setRow1(rowIndex);
		anchor.setRow2(rowIndex + 1);
		anchor.setDx1(2 * 9525);
		anchor.setDy1(2 * 9525);
		anchor.setDx2(0);
		anchor.setDy2(0);
		drawing.createPicture(anchor, pictureIndex);
	}

	private byte[] readPhotoBytes(String fileUrl) {
		String marker = "/sys-file/";
		int pos = fileUrl.indexOf(marker);
		if (pos < 0) {
			return new byte[0];
		}
		String path = fileUrl.substring(pos + marker.length());
		String[] parts = path.split("/", 2);
		if (parts.length != 2) {
			return new byte[0];
		}
		try (InputStream inputStream = (InputStream) fileTemplate.getObject(parts[0], parts[1])) {
			return inputStream.readAllBytes();
		}
		catch (Exception e) {
			return new byte[0];
		}
	}

	private int writePhotoZipEntries(ZipOutputStream zipOutputStream, Set<String> entryNames, AdminVisitPhotoVO item) throws IOException {
		int written = 0;
		String date = item.getStartTime() == null ? "未知日期" : item.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE);
		String folder = sanitizeZipName(date + "_" + defaultString(item.getTitle(), "未命名申请") + "_" + item.getApplyId());
		int index = 1;
		for (AdminVisitPhotoVO.PhotoVO photo : item.getPhotos()) {
			if (photo == null || !StringUtils.hasText(photo.getFileUrl())) {
				continue;
			}
			byte[] bytes = readPhotoBytes(photo.getFileUrl());
			if (bytes.length == 0) {
				continue;
			}
			String fileName = sanitizeZipName(defaultString(photo.getFileName(), "现场照片-" + photo.getId() + ".jpg"));
			String entryName = uniqueZipEntryName(entryNames, folder + "/" + String.format("%02d-", index++) + fileName);
			zipOutputStream.putNextEntry(new ZipEntry(entryName));
			zipOutputStream.write(bytes);
			zipOutputStream.closeEntry();
			written++;
		}
		return written;
	}

	private String uniqueZipEntryName(Set<String> entryNames, String name) {
		String normalizedName = name;
		int suffix = 1;
		while (!entryNames.add(normalizedName)) {
			int dot = name.lastIndexOf('.');
			normalizedName = dot > 0
				? name.substring(0, dot) + "-" + suffix++ + name.substring(dot)
				: name + "-" + suffix++;
		}
		return normalizedName;
	}

	private String sanitizeZipName(String name) {
		String sanitized = defaultString(name, "未命名").replaceAll("[\\\\/:*?\"<>|\\r\\n]+", "_").trim();
		return sanitized.isEmpty() ? "未命名" : sanitized;
	}

	private String encodeFileName(String fileName) {
		return URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
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
			1, "管理员"
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

	private String currentUsername() {
		try {
			return SecurityUtils.getUser().getUsername();
		} catch (Exception e) {
			return "";
		}
	}

}
