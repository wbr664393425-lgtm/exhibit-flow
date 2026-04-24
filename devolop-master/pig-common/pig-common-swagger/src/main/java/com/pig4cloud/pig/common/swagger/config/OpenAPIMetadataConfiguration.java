package com.pig4cloud.pig.common.swagger.config;

import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * OpenAPI 元数据配置类（单体模式：不向 ServiceInstance 注册元数据，空实现）
 *
 * @author lengleng
 * @date 2025/05/31
 */
public class OpenAPIMetadataConfiguration implements InitializingBean, ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Setter
	private String path;

	@Override
	public void afterPropertiesSet() throws Exception {
		// 单体模式：无 Nacos ServiceInstance，跳过元数据注册
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
