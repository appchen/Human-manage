/**
 * 文 件 名:  CreateDepartmentRequest
 * 版    权:  Quanten Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  zping
 * 修改时间:  2018/3/22 0022
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.civil.aviation.human.api.office.request;

import com.civil.aviation.human.api.base.BaseRequest;
import com.civil.aviation.human.api.office.domain.OfficeVo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author zping
 * @version 2018/3/22 0022
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateOfficeRequest extends BaseRequest
{
	private OfficeVo officeVo;

	@Override
	public String toString ()
	{
		final StringBuilder sb = new StringBuilder ("CreateOfficeRequest{");
		sb.append ("officeVo=").append (officeVo);
		sb.append ('}');
		return sb.toString ();
	}
}
