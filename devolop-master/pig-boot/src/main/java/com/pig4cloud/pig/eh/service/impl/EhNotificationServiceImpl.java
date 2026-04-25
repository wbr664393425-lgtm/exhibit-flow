package com.pig4cloud.pig.eh.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.eh.entity.EhNotification;
import com.pig4cloud.pig.eh.mapper.EhNotificationMapper;
import com.pig4cloud.pig.eh.service.EhNotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EhNotificationServiceImpl extends ServiceImpl<EhNotificationMapper, EhNotification> implements EhNotificationService {

	@Override
	public void createNotification(Long applyId, String recipient, String type, String title, String content, String route) {
		if (recipient == null || recipient.isBlank()) {
			return;
		}
		EhNotification entity = new EhNotification();
		entity.setApplyId(applyId);
		entity.setRecipient(recipient);
		entity.setType(type);
		entity.setTitle(title);
		entity.setContent(content);
		entity.setRoute(route);
		entity.setReadFlag("0");
		save(entity);
	}

	@Override
	public boolean markRead(Long id, String recipient) {
		EhNotification notification = getOne(
			Wrappers.<EhNotification>lambdaQuery()
				.eq(EhNotification::getId, id)
				.eq(EhNotification::getRecipient, recipient)
				.eq(EhNotification::getDelFlag, "0")
				.last("limit 1"),
			false
		);
		if (notification == null || "1".equals(notification.getReadFlag())) {
			return notification != null;
		}
		notification.setReadFlag("1");
		notification.setReadTime(LocalDateTime.now());
		return updateById(notification);
	}

	@Override
	public boolean markAllRead(String recipient) {
		return lambdaUpdate()
			.eq(EhNotification::getRecipient, recipient)
			.eq(EhNotification::getDelFlag, "0")
			.eq(EhNotification::getReadFlag, "0")
			.set(EhNotification::getReadFlag, "1")
			.set(EhNotification::getReadTime, LocalDateTime.now())
			.update();
	}
}
