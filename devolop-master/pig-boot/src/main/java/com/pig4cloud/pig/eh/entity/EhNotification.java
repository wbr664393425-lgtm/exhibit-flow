package com.pig4cloud.pig.eh.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("eh_notification")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "展厅通知")
public class EhNotification extends Model<EhNotification> {

	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	@Schema(description = "关联申请ID")
	private Long applyId;

	@Schema(description = "接收人用户名")
	private String recipient;

	@Schema(description = "通知类型")
	private String type;

	@Schema(description = "通知标题")
	private String title;

	@Schema(description = "通知内容")
	private String content;

	@Schema(description = "跳转路由")
	private String route;

	@Schema(description = "是否已读,0未读,1已读")
	private String readFlag;

	@Schema(description = "已读时间")
	private LocalDateTime readTime;

	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建人")
	private String createBy;

	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "修改人")
	private String updateBy;

	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "修改时间")
	private LocalDateTime updateTime;

	@TableLogic
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "删除标记,0正常,1删除")
	private String delFlag;
}
