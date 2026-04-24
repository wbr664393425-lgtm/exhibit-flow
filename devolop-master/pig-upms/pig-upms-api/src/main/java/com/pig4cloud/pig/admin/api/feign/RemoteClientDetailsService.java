package com.pig4cloud.pig.admin.api.feign;

import com.pig4cloud.pig.admin.api.entity.SysOauthClientDetails;
import com.pig4cloud.pig.common.core.util.R;

/**
 * 远程客户端详情服务接口（单体模式：已移除 @FeignClient，由本地 Service 直接实现）
 *
 * @author lengleng
 * @date 2025/05/30
 */
public interface RemoteClientDetailsService {

	/**
	 * 通过clientId 查询客户端信息
	 * 
	 * @param clientId 客户端ID
	 * @return R
	 */
	R<SysOauthClientDetails> getClientDetailsById(String clientId);

}
