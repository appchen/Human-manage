/**
 * 文 件 名:  AdminRoleMapper
 * 版    权:  Quanten Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  zping
 * 修改时间:  2017/7/11 0011
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.civil.aviation.human.database.mapper;

import com.civil.aviation.human.database.entity.AdminRoleRlat;

import java.util.List;
import java.util.Map;

/**
 * <员工角色Mapper接口>
 *
 * @author zping
 * @version 2017/7/11 0011
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface AdminRoleMapper
{
	/**
	 * 根据管理员ID查询角色
	 *
	 * @param adminId
	 * @return
	 */
	List<AdminRoleRlat> findById (String adminId);

	/**
	 * 添加员工角色关系信息
	 *
	 * @param adminRoleRlat
	 */
	int add (AdminRoleRlat adminRoleRlat);

	/**
	 * 删除角色关系信息
	 *
	 * @param adminId
	 * @param roleId
	 */
	int delete (String adminId, int roleId);

	/**
	 * 绑定管理员角色关系信息
	 */
	void bindRole (Map<String, String> params);
}
