package com.pig4cloud.pig.eh.service;

import com.pig4cloud.pig.eh.dto.ApprovalActionDTO;
import com.pig4cloud.pig.eh.dto.ReturnSignDTO;
import com.pig4cloud.pig.eh.dto.VisitPhotoBatchDTO;
import com.pig4cloud.pig.eh.dto.VisitRecordUpsertDTO;
import com.pig4cloud.pig.eh.vo.AdminApplyPageVO;
import com.pig4cloud.pig.eh.vo.AdminChangeLogVO;
import com.pig4cloud.pig.eh.vo.AdminOpportunityVO;
import com.pig4cloud.pig.eh.vo.AdminVisitAggregateVO;
import com.pig4cloud.pig.eh.vo.AdminVisitPhotoVO;

import java.util.List;

public interface EhAdminAggregateService {

	List<AdminApplyPageVO> queryApplyAggregateList(String status);

	List<AdminApplyPageVO> queryApprovalTodoList();

	List<AdminVisitAggregateVO> queryVisitAggregateList();

	List<AdminVisitPhotoVO> queryVisitPhotoAggregateList();

	List<AdminOpportunityVO> queryOpportunityAggregateList();

	List<AdminChangeLogVO> queryChangeLogAggregateList();

	boolean upsertVisitRecord(VisitRecordUpsertDTO dto);

	boolean signVisitReturn(ReturnSignDTO dto);

	boolean approveOrRejectOrTransfer(ApprovalActionDTO dto);

	boolean saveVisitPhotos(VisitPhotoBatchDTO dto);
}
