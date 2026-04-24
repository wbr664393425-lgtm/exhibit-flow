package com.pig4cloud.pig.admin.api.feign;

import com.pig4cloud.pig.admin.api.dto.UserDTO;
import com.pig4cloud.pig.admin.api.dto.UserInfo;
import com.pig4cloud.pig.common.core.util.R;

/**
 * 远程用户服务接口（单体模式：已移除 @FeignClient，由本地 SysUserService 直接实现）
 *
 * @author lengleng
 * @date 2025/05/30
 */
public interface RemoteUserService {

	/**
	 * 通过用户名查询用户、角色信息
	 * 
	 * @param user 用户查询对象
	 * @return R
	 */
	R<UserInfo> info(UserDTO user);

}
