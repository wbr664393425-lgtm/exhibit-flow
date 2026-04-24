package com.pig4cloud.pig.admin.api.feign;

import com.pig4cloud.pig.common.core.util.R;

/**
 * 远程参数服务接口（单体模式：已移除 @FeignClient，由本地 SysPublicParamService 直接实现）
 *
 * @author lengleng
 * @date 2025/05/30
 */
public interface RemoteParamService {

	/**
	 * 通过key 查询参数配置
	 * 
	 * @param key key
	 * @return 参数值
	 */
	R<String> getByKey(String key);

}
