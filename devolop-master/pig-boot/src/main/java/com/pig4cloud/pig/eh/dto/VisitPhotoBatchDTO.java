package com.pig4cloud.pig.eh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "现场照片批量保存")
public class VisitPhotoBatchDTO {

	@Schema(description = "参观留存ID")
	private Long visitId;

	@Schema(description = "申请ID")
	private Long applyId;

	@Schema(description = "照片列表")
	private List<PhotoDTO> photos = new ArrayList<>();

	@Data
	@Schema(description = "照片项")
	public static class PhotoDTO {

		@Schema(description = "文件URL")
		private String fileUrl;

		@Schema(description = "文件名")
		private String fileName;

		@Schema(description = "文件大小")
		private Long fileSize;
	}

}
