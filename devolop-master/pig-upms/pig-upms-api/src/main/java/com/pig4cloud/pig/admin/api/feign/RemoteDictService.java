package com.pig4cloud.pig.admin.api.feign;

import com.pig4cloud.pig.admin.api.entity.SysDictItem;
import com.pig4cloud.pig.common.core.util.R;

import java.util.List;

/**
 * 远程字典服务接口（单体模式：已移除 @FeignClient，由本地 SysDictItemService 直接实现）
 *
 * @author lengleng
 * @date 2025/05/30
 */
public interface RemoteDictService {

	/**
	 * 通过字典类型查找字典
	 * 
	 * @param type 字典类型
	 * @return 同类型字典
	 */
	R<List<SysDictItem>> getDictByType(String type);

}
