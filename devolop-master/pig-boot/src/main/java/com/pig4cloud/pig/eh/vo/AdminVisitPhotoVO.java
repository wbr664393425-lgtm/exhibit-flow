package com.pig4cloud.pig.eh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "管理端现场照片聚合视图")
public class AdminVisitPhotoVO {

	private Long applyId;
	private Long visitId;
	private String title;
	private String status;
	private LocalDateTime startTime;
	private Integer photoCount;
	private List<PhotoVO> photos = new ArrayList<>();

	@Data
	public static class PhotoVO {
		private Long id;
		private String fileUrl;
		private String fileName;
		private Long fileSize;
	}
}
