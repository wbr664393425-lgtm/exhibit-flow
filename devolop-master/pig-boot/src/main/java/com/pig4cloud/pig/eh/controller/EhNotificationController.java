package com.pig4cloud.pig.eh.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.eh.entity.EhNotification;
import com.pig4cloud.pig.eh.service.EhNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/eh/notification")
@Tag(description = "eh/notification", name = "展厅通知中心")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class EhNotificationController {

	private final EhNotificationService ehNotificationService;

	@GetMapping("/page")
	@Operation(summary = "通知分页")
	public R getPage(@ParameterObject Page<EhNotification> page) {
		String username = SecurityUtils.getUser().getUsername();
		return R.ok(
			ehNotificationService.lambdaQuery()
				.eq(EhNotification::getRecipient, username)
				.eq(EhNotification::getDelFlag, "0")
				.orderByDesc(EhNotification::getCreateTime)
				.page(page)
		);
	}

	@PutMapping("/{id}/read")
	@Operation(summary = "单条通知已读")
	public R markRead(@PathVariable Long id) {
		return R.ok(ehNotificationService.markRead(id, SecurityUtils.getUser().getUsername()));
	}

	@PutMapping("/read-all")
	@Operation(summary = "全部已读")
	public R markAllRead() {
		return R.ok(ehNotificationService.markAllRead(SecurityUtils.getUser().getUsername()));
	}
}
