/**
 * 文 件 名:  DeleteAssessTopicRequest
 * 版    权:  Quanten Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  zping
 * 修改时间:  2018/4/2 0002
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.civil.aviation.human.api.assess.request;

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
public class DeleteAssessTopicRequest implements Serializable
{

	/**
	 * 考核主题编号
	 */
	private String assessTopicId;

	@Override
	public String toString ()
	{
		final StringBuilder sb = new StringBuilder ("DeleteAssessTopicRequest{");
		sb.append ("assessTopicId='").append (assessTopicId).append ('\'');
		sb.append ('}');
		return sb.toString ();
	}
}
