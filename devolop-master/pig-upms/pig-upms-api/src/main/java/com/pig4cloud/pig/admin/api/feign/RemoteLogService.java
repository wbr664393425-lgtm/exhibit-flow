package com.pig4cloud.pig.admin.api.feign;

import com.pig4cloud.pig.admin.api.entity.SysLog;
import com.pig4cloud.pig.common.core.util.R;

/**
 * 远程日志服务接口（单体模式：已移除 @FeignClient，由本地 SysLogService 直接实现）
 *
 * @author lengleng
 * @date 2025/05/30
 */
public interface RemoteLogService {

	/**
	 * 保存日志
	 * 
	 * @param sysLog 日志实体
	 * @return 是否成功
	 */
	R<Boolean> saveLog(SysLog sysLog);

}
