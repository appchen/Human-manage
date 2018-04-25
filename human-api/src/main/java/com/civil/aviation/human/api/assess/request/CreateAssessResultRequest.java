/**
 * 文 件 名:  CreateAssessResultRequest
 * 版    权:  Quanten Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  zping
 * 修改时间:  2018/4/2 0002
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.civil.aviation.human.api.assess.request;

import com.civil.aviation.human.api.assess.domain.AssessResultVo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author zping
 * @version 2018/4/2 0002
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Getter
@Setter
public class CreateAssessResultRequest implements Serializable
{
	/**
	 * 考核成绩
	 */
	private AssessResultVo assessResult;

	@Override
	public String toString ()
	{
		final StringBuilder sb = new StringBuilder ("CreateAssessResultRequest{");
		sb.append ("assessResult=").append (assessResult);
		sb.append ('}');
		return sb.toString ();
	}
}
