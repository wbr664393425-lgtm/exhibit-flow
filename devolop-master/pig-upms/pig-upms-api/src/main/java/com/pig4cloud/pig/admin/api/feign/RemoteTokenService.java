package com.pig4cloud.pig.admin.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;

import java.util.Map;

/**
 * 远程令牌服务接口（单体模式：已移除 @FeignClient，由本地 TokenService 直接实现）
 *
 * @author lengleng
 * @date 2025/05/30
 */
public interface RemoteTokenService {

	/**
	 * 分页查询 token 信息
	 * 
	 * @param params 分页参数
	 * @return page
	 */
	R<Page> getTokenPage(Map<String, Object> params);

	/**
	 * 根据 token 删除 token 信息
	 * 
	 * @param token 要删除的 token
	 * @return 是否成功
	 */
	R<Boolean> removeTokenById(String token);

	/**
	 * 根据令牌查询用户信息
	 * 
	 * @param token 用户令牌
	 * @return 用户信息
	 */
	R<Map<String, Object>> queryToken(String token);

}
