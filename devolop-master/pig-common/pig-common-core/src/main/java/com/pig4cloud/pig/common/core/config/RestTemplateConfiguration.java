package com.pig4cloud.pig.common.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 自动配置类
 *
 * @author lengleng
 * @date 2025/05/30
 */
@AutoConfiguration
public class RestTemplateConfiguration {

	/**
	 * 创建 REST 模板（单体模式，无负载均衡）
	 * 
	 * @return {@link RestTemplate}
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * 创建 REST 客户端构建器（单体模式，无负载均衡）
	 * 
	 * @return {@link RestClient.Builder}
	 */
	@Bean
	RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

}
