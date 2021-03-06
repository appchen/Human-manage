/**
 * 文 件 名:  ModifyAssessmentRequest
 * 版    权:  Quanten Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  zping
 * 修改时间:  2018/4/2 0002
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.civil.aviation.human.api.assess.request;

import com.civil.aviation.human.api.assess.domain.AssessTopicVo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * <修改考核主题请求VO>
 *
 * @author zping
 * @version 2018/4/2 0002
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Getter
@Setter
public class ModifyAssessmentRequest implements Serializable
{
	/**
	 * 考核主题
	 */
	private AssessTopicVo assessTopic;

	/**
	 * 考核分类ID编号
	 */
	private List<String> assessCatalogIds;

	@Override
	public String toString ()
	{
		final StringBuilder sb = new StringBuilder ("CreateAssessmentTopicRequest{");
		sb.append ("assessTopic=").append (assessTopic);
		sb.append (", assessCatalogIds=").append (assessCatalogIds);
		sb.append ('}');
		return sb.toString ();
	}
}
