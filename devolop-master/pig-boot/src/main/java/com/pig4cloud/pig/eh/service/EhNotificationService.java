package com.pig4cloud.pig.eh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.eh.entity.EhNotification;

public interface EhNotificationService extends IService<EhNotification> {

	void createNotification(Long applyId, String recipient, String type, String title, String content, String route);

	boolean markRead(Long id, String recipient);

	boolean markAllRead(String recipient);
}
