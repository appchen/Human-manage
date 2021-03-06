/**
 * 文 件 名:  AssessmentApiImpl
 * 版    权:  Quanten Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  zping
 * 修改时间:  2018/4/2 0002
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.civil.aviation.human.provider.impl.assessment;

import com.civil.aviation.human.api.assess.AssessmentApi;
import com.civil.aviation.human.api.assess.domain.*;
import com.civil.aviation.human.api.assess.request.*;
import com.civil.aviation.human.api.assess.response.*;
import com.civil.aviation.human.common.core.annotation.Api;
import com.civil.aviation.human.common.core.cons.Constants;
import com.civil.aviation.human.common.core.domain.Result;
import com.civil.aviation.human.common.core.utils.DateUtils;
import com.civil.aviation.human.common.core.utils.SessionUtils;
import com.civil.aviation.human.database.entity.*;
import com.civil.aviation.human.database.mapper.AssessCatalogMapper;
import com.civil.aviation.human.database.mapper.AssessmentMapper;
import com.civil.aviation.human.database.mapper.EmployeeMappper;
import com.civil.aviation.human.provider.mapper.EntityMapperHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.civil.aviation.human.common.core.utils.DateUtils.YYYY;

/**
 * <员工考核主题接口实现>
 *
 * @author zping
 * @version 2018/4/2 0002
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Api
public class AssessmentApiImpl implements AssessmentApi
{

	/**
	 * 日志记录器
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger (AssessmentApiImpl.class);

	@Autowired
	private AssessmentMapper assessmentMapper;

	@Autowired
	private EmployeeMappper employeeMappper;

	@Autowired
	private AssessCatalogMapper assessCatalogMapper;

	/**
	 * 添加考核主题
	 *
	 * @param request
	 * @param createAssessmentTopicRequest
	 * @return
	 * @throws Exception
	 */
	@Override
	public Result addAssessmentTopic (HttpServletRequest request,
			CreateAssessmentTopicRequest createAssessmentTopicRequest) throws Exception
	{

		if (null == createAssessmentTopicRequest)
		{
			return Result.fail ("illega params is null.");
		}

		if (null == createAssessmentTopicRequest.getAssessTopic ())
		{
			return Result.fail ("illega params is null.");
		}

		if (CollectionUtils.isEmpty (createAssessmentTopicRequest.getAssessCatalogIds ()))
		{
			return Result.fail ("illega params is null.");
		}

		AssessTopic assessTopic = EntityMapperHandler.INSTANCE
				.voTOAssessTopic (createAssessmentTopicRequest.getAssessTopic ());

		//查询当前是否存在生效的考核主题
		Map<String, Object> params = Maps.newHashMap ();
		params.put ("startTime", assessTopic.getStartTime ());
		params.put ("endTime", assessTopic.getStartTime ());
		List<AssessTopic> assessTopics = assessmentMapper.qryAssessTopicByConfit (params);
		if (! CollectionUtils.isEmpty (assessTopics))
		{
			return Result.fail ("考核时间不能重叠.");
		}

		//考核标准
		List<AssessCatalogRela> assessCatalogRelas = Lists.newArrayList ();
		AssessCatalogRela assessCatalogRela = null;
		for (String assessCatalogId : createAssessmentTopicRequest.getAssessCatalogIds ())
		{
			assessCatalogRela = new AssessCatalogRela ();
			assessCatalogRela.setAssessCatalogId (assessCatalogId);
			assessCatalogRela.setTopicId (assessTopic.getId ());
			assessCatalogRelas.add (assessCatalogRela);
		}

		int flag = assessmentMapper.addAssessTopic (assessTopic);

		if (flag > 0)
		{
			if (assessCatalogMapper.addCatalogRela (assessCatalogRelas) > 0)
			{
				return Result.success ("add assess topic success.");
			}
			else
			{
				return Result.fail ("add assess topic failed.");
			}
		}
		else
		{
			return Result.fail ("add assess topic failed.");
		}
	}

	@Override
	public QryAssessTopicByIdResponse queryAssessmentTopicById (HttpServletRequest request,
			QryAssessTopicByIdRequest qryAssessTopicByIdRequest) throws Exception
	{
		QryAssessTopicByIdResponse qryAssessTopicByIdResponse = new QryAssessTopicByIdResponse ();
		if (null == qryAssessTopicByIdRequest)
		{
			return (QryAssessTopicByIdResponse) Result.fail ("illega params is null.");
		}

		if (StringUtils.isEmpty (qryAssessTopicByIdRequest.getTopicId ()))
		{
			return (QryAssessTopicByIdResponse) Result.fail ("illega params is null.");
		}

		AssessTopic assessTopic = assessmentMapper.queryAssessTopicById (qryAssessTopicByIdRequest.getTopicId ());

		if (null != assessTopic)
		{
			AssessTopicVo assessTopicVo = EntityMapperHandler.INSTANCE.assessTopicToVo (assessTopic);
			List<AssessCatalog> assessCatalogs = assessCatalogMapper.queryAssessCatalogByTopicId (assessTopic.getId ());
			if (! CollectionUtils.isEmpty (assessCatalogs))
			{
				List<AssessCatalogVo> assessCatalogVos = Lists.newArrayList ();
				AssessCatalogVo assessCatalogVo = null;
				for (AssessCatalog assessCatalog : assessCatalogs)
				{
					assessCatalogVo = new AssessCatalogVo ();
					BeanUtils.copyProperties (assessCatalog, assessCatalogVo);
					assessCatalogVos.add (assessCatalogVo);
				}
				qryAssessTopicByIdResponse.setAssessCatalogVos (assessCatalogVos);
			}
			qryAssessTopicByIdResponse.setAssessTopic (assessTopicVo);
		}
		else
		{
			qryAssessTopicByIdResponse.setResultMessage ("assessTopic not exist.");
		}
		return qryAssessTopicByIdResponse;
	}

	/**
	 * 修改考核主题
	 *
	 * @param request
	 * @param modifyAssessmentRequest
	 * @return
	 * @throws Exception
	 */
	@Override
	public Result modifyAssessmentTopic (HttpServletRequest request, ModifyAssessmentRequest modifyAssessmentRequest)
			throws Exception
	{
		if (null == modifyAssessmentRequest)
		{
			return Result.fail ("illega params is null.");
		}

		if (null == modifyAssessmentRequest.getAssessTopic ())
		{
			return Result.fail ("illega params is null.");
		}

		if (CollectionUtils.isEmpty (modifyAssessmentRequest.getAssessCatalogIds ()))
		{
			return Result.fail ("illega params is null.");
		}

		AssessTopic assessTopic = EntityMapperHandler.INSTANCE
				.voTOAssessTopic (modifyAssessmentRequest.getAssessTopic ());

		//查询考核主题分类信息
		List<AssessCatalogRela> assessCatalogRelas = Lists.newArrayList ();
		AssessCatalogRela assessCatalogRela = null;
		for (String assessCatalogId : modifyAssessmentRequest.getAssessCatalogIds ())
		{
			assessCatalogRela = new AssessCatalogRela ();
			assessCatalogRela.setAssessCatalogId (assessCatalogId);
			assessCatalogRela.setTopicId (assessTopic.getId ());
			assessCatalogRelas.add (assessCatalogRela);
		}

		//先删除考核标准
		int flag = assessCatalogMapper.deleteCatalogRelas (assessTopic.getId ());

		if (flag > 0)
		{
			if (assessmentMapper.modifyAssessTopic (assessTopic) > 0)
			{
				if (assessCatalogMapper.addCatalogRela (assessCatalogRelas) > 0)
				{
					return Result.success ("modify assess topic success.");
				}
				else
				{
					return Result.fail ("modify assess topic failed.");
				}
			}
			else
			{
				return Result.fail ("modify assess topic failed.");
			}
		}
		else
		{
			return Result.fail ("modify assess topic failed.");
		}
	}

	/**
	 * 添加考核内容信息
	 *
	 * @param request
	 * @param createAssessContentRequest
	 * @return
	 */
	@Override
	public Result addAssessContent (HttpServletRequest request, CreateAssessContentRequest createAssessContentRequest)
			throws Exception
	{
		if (null == createAssessContentRequest || CollectionUtils
				.isEmpty (createAssessContentRequest.getAssessContents ()))
		{
			return Result.fail ("illega params");
		}

		List<AssessContentVo> assessContentVos = createAssessContentRequest.getAssessContents ();
		List<AssessContent> assessContents = Lists.newArrayList ();
		AssessContent assessContent = null;

		for (AssessContentVo assessContentVo : assessContentVos)
		{
			assessContent = new AssessContent ();
			BeanUtils.copyProperties (assessContentVo, assessContent);
			assessContents.add (assessContent);
		}

		int flag = assessmentMapper.addAssessContent (assessContents);
		if (flag > 0)
		{
			return Result.success ("add assess content success.");
		}
		else
		{
			return Result.fail ("add assess content failed.");
		}
	}

	/**
	 * 修改考核内容信息
	 *
	 * @param request
	 * @param modifyAssessContentRequest
	 * @return
	 */
	@Override
	public Result modifyAssessContent (HttpServletRequest request,
			ModifyAssessContentRequest modifyAssessContentRequest) throws Exception
	{

		if (null == modifyAssessContentRequest || null == modifyAssessContentRequest.getAssessContent ())
		{
			return Result.fail ("illega params");
		}

		AssessContentVo assessContentVo = modifyAssessContentRequest.getAssessContent ();
		AssessContent assessContent = EntityMapperHandler.INSTANCE.voToAssessContent (assessContentVo);

		int flag = assessmentMapper.modifyAssessContent (assessContent);
		if (flag > 0)
		{
			return Result.success ("modify assess content success.");
		}
		else
		{
			return Result.fail ("modify assess content failed.");
		}
	}

	/**
	 * 删除考核内容信息
	 *
	 * @param request
	 * @param deleteAssessContentRequest
	 * @return
	 */
	@Override
	public Result deleteAssessContent (HttpServletRequest request,
			DeleteAssessContentRequest deleteAssessContentRequest) throws Exception
	{
		if (null == deleteAssessContentRequest || StringUtils
				.isEmpty (deleteAssessContentRequest.getAssessContentId ()))
		{
			return Result.fail ("illega params");
		}

		int flag = assessmentMapper.deleteAssessContent (deleteAssessContentRequest.getAssessContentId ());
		if (flag > 0)
		{
			return Result.success ("delete assess content success.");
		}
		else
		{
			return Result.fail ("delete assess content failed.");
		}
	}

	/**
	 * 查询考核内容列表信息
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	public QryAssessContentListResponse queryAssessContentList (HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		QryAssessContentListResponse qryAssessContentListResponse = new QryAssessContentListResponse ();
		Map<String, Object> params = Maps.newHashMap ();

		if (! StringUtils.isEmpty (request.getParameter ("catalog")))
		{
			params.put ("catalog", request.getParameter ("catalog"));
		}

		String pageIndex = request.getParameter ("pageIndex");
		String pageSize = request.getParameter ("pageSize");

		if (StringUtils.isEmpty (pageIndex))
		{
			pageIndex = "1";
		}
		if (StringUtils.isEmpty (pageSize))
		{
			pageSize = "10";
		}
		params.put ("pageIndex", (Integer.valueOf (pageIndex) - 1) * Integer.valueOf (pageSize));
		params.put ("pageSize", pageSize);

		List<AssessContent> assessContents = assessmentMapper.queryAssessContents (params);
		List<AssessContentVo> assessContentVos = null;
		if (! CollectionUtils.isEmpty (assessContents))
		{
			assessContentVos = Lists.newArrayList ();
			AssessContentVo assessContentVo = null;
			for (AssessContent assessContent : assessContents)
			{
				assessContentVo = EntityMapperHandler.INSTANCE.assessContentToVo (assessContent);
				assessContentVos.add (assessContentVo);
			}
			qryAssessContentListResponse.setCount (assessmentMapper.queryAssessContentCount (params));
		}
		qryAssessContentListResponse.setAssessContents (assessContentVos);
		return qryAssessContentListResponse;
	}

	/**
	 * 查询考核内容信息
	 *
	 * @param request
	 * @param queryAssessContentRequest
	 * @return
	 */
	@Override
	public QryAssessContentResponse queryAssessContentById (HttpServletRequest request,
			QueryAssessContentRequest queryAssessContentRequest) throws Exception
	{
		if (null == queryAssessContentRequest || StringUtils.isEmpty (queryAssessContentRequest.getAssessContentId ()))
		{
			return (QryAssessContentResponse) Result.fail ("illega params");
		}
		QryAssessContentResponse qryAssessContentResponse = new QryAssessContentResponse ();
		AssessContent assessContent = assessmentMapper
				.queryAssessContentById (queryAssessContentRequest.getAssessContentId ());

		if (null != assessContent)
		{
			AssessContentVo assessContentVo = EntityMapperHandler.INSTANCE.assessContentToVo (assessContent);
			qryAssessContentResponse.setAssessContent (assessContentVo);
		}
		else
		{
			qryAssessContentResponse.setResultMessage ("Content not exist");
		}
		return qryAssessContentResponse;
	}

	/**
	 * 添加考核成绩
	 *
	 * @param request
	 * @param createAssessResultRequest
	 * @return
	 * @throws Exception
	 */
	@Override
	public Result addAssessResult (HttpServletRequest request, CreateAssessResultRequest createAssessResultRequest)
			throws Exception
	{
		if (null == createAssessResultRequest || null == createAssessResultRequest.getAssessResult ())
		{
			return Result.fail ("illega params is null.");
		}

		AssessResultVo assessResultVo = createAssessResultRequest.getAssessResult ();

		if (null == assessmentMapper
				.queryAssessResult (assessResultVo.getEmployeeId (), assessResultVo.getDiscussant (),
						assessResultVo.getTopic ()))
		{
			AssessResult assessResult = EntityMapperHandler.INSTANCE
					.voToAssessResult (createAssessResultRequest.getAssessResult ());
			assessResult.setDiscussant ((String) SessionUtils.getValue (SessionUtils.EMPLOYEE_ID_SESSION_KEY));
			int flag = assessmentMapper.addAssessResult (assessResult);

			if (flag > 0)
			{
				return Result.success ("add assess result success.");
			}
			else
			{
				return Result.fail ("add assess result failed.");
			}
		}
		else
		{
			return Result.fail ("add assess result failed, You had assess him.");
		}
	}

	/**
	 * 查询当前生效的考核主题
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	public AssessmentTopicResponse qryEffectiveAssessTopic (HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		AssessmentTopicResponse result = new AssessmentTopicResponse ();

		//查询当前生效的考核主题
		List<AssessTopicContent> assessTopicContents = assessmentMapper.qryEffectiveAssessTopic ();

		//为空，则说明不存在
		if (null == assessTopicContents || CollectionUtils.isEmpty (assessTopicContents))
		{
			result.setResultCode ("999999");
			result.setResultMessage ("It is not time for assessment.");
			return result;
		}
		else
		{
			List<AssessTopicContentVo> assessTopicContentVos = Lists.newArrayList ();
			AssessTopicContentVo assessTopicContentVo = null;
			for (AssessTopicContent assessTopicContent : assessTopicContents)
			{
				assessTopicContentVo = new AssessTopicContentVo ();
				BeanUtils.copyProperties (assessTopicContent, assessTopicContentVo);
				assessTopicContentVos.add (assessTopicContentVo);
			}
			result.setResultMessage ("query success.");
			result.setResultCode (Constants.ResultCode.SUCCESS);
			result.setAssessTopics (assessTopicContentVos);
		}
		return result;
	}

	/**
	 * 根据条件查询考核主题信息
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@Override
	public QryAssessTopicResponse qryAssessTopicByCondition (HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		QryAssessTopicResponse qryAssessTopicResponse = new QryAssessTopicResponse ();
		Map<String, Object> params = Maps.newHashMap ();
		if (! StringUtils.isEmpty (request.getParameter ("status")))
		{
			params.put ("status", request.getParameter ("status"));
		}

		if (! StringUtils.isEmpty (request.getParameter ("startTime")))
		{
			params.put ("startTime", request.getParameter ("startTime"));
		}

		if (! StringUtils.isEmpty (request.getParameter ("endTime")))
		{
			params.put ("endTime", request.getParameter ("endTime"));
		}

		String pageIndex = request.getParameter ("pageIndex");
		String pageSize = request.getParameter ("pageSize");

		if (StringUtils.isEmpty (pageIndex))
		{
			pageIndex = "1";
		}
		if (StringUtils.isEmpty (pageSize))
		{
			pageSize = "10";
		}
		params.put ("pageIndex", (Integer.valueOf (pageIndex) - 1) * Integer.valueOf (pageSize));
		params.put ("pageSize", pageSize);

		//查询考核标题信息
		List<AssessTopic> assessTopics = assessmentMapper.qryAssessTopicByCondition (params);
		List<AssessTopicVo> assessTopicVos = null;
		AssessTopicVo assessTopicVo = null;
		if (! CollectionUtils.isEmpty (assessTopics))
		{
			assessTopicVos = Lists.newArrayList ();
			for (AssessTopic assessTopic : assessTopics)
			{
				assessTopicVo = EntityMapperHandler.INSTANCE.assessTopicToVo (assessTopic);
				assessTopicVos.add (assessTopicVo);
			}
		}
		qryAssessTopicResponse.setAssessTopics (assessTopicVos);
		qryAssessTopicResponse.setCount (assessmentMapper.qryAssessTopicCountByCondition (params));
		return qryAssessTopicResponse;
	}

	/**
	 * 根据员工编号查询去年的考核成绩
	 *
	 * @param request
	 * @param qryAssessResultByEmployRequest
	 * @return
	 * @throws Exception
	 */
	@Override
	public QryAssessResultByEmployResponse qryAssessResultByEmployee (HttpServletRequest request,
			QryAssessResultByEmployRequest qryAssessResultByEmployRequest) throws Exception
	{
		QryAssessResultByEmployResponse qryAssessResultByEmployResponse = new QryAssessResultByEmployResponse ();
		if (null == qryAssessResultByEmployRequest || StringUtils
				.isEmpty (qryAssessResultByEmployRequest.getEmployeeId ()))
		{
			return (QryAssessResultByEmployResponse) Result.fail ("illega params.");
		}
		if (StringUtils.isEmpty (qryAssessResultByEmployRequest.getYear ()))
		{
			qryAssessResultByEmployRequest.setYear (DateUtils.formatNow (DateUtils.YYYY));
		}
		AssessGradeVo assessGradeVo = new AssessGradeVo ();
		//查询自评
		AssessResult assessResult = assessmentMapper
				.queryAssessResultBySelf (qryAssessResultByEmployRequest.getEmployeeId (),
						qryAssessResultByEmployRequest.getYear ());

		//自评分数
		if (null != assessResult)
		{
			assessGradeVo.setTime (assessResult.getTime ().substring (0, 4));
			assessGradeVo.setTopic (assessResult.getTopic ());
			Double score = assessResult.getScore () * assessResult.getWeight () / 100;
			assessGradeVo.setScore (score);
		}

		//上级分数
		List<AssessResult> higherAssessResult = assessmentMapper
				.queryAssessResultByEmployee (qryAssessResultByEmployRequest.getEmployeeId (), 60,
						qryAssessResultByEmployRequest.getYear ());
		if (! CollectionUtils.isEmpty (higherAssessResult))
		{
			assessGradeVo.setHigherUpScore (statisticalResults (higherAssessResult, 60));
		}

		//下级分数
		List<AssessResult> lowerAssessResult = assessmentMapper
				.queryAssessResultByEmployee (qryAssessResultByEmployRequest.getEmployeeId (), 20,
						qryAssessResultByEmployRequest.getYear ());
		if (! CollectionUtils.isEmpty (lowerAssessResult))
		{
			assessGradeVo.setLowerUpScore (statisticalResults (lowerAssessResult, 20));
		}

		//同级分数
		List<AssessResult> visAssessResult = assessmentMapper
				.queryAssessResultByEmployee (qryAssessResultByEmployRequest.getEmployeeId (), 10,
						qryAssessResultByEmployRequest.getYear ());
		if (! CollectionUtils.isEmpty (visAssessResult))
		{
			assessGradeVo.setVis_a_vis_score (statisticalResults (visAssessResult, 10));
		}

		assessGradeVo.setEmployeeId (qryAssessResultByEmployRequest.getEmployeeId ());
		assessGradeVo.setEmployeeName (
				employeeMappper.queryEmployById (qryAssessResultByEmployRequest.getEmployeeId ()).getName ());
		qryAssessResultByEmployResponse.setAssessGrade (assessGradeVo);
		return qryAssessResultByEmployResponse;
	}

	@Override
	public QryAssessResultsResponse qryAssessResults (HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		QryAssessResultsResponse qryAssessResultsResponse = new QryAssessResultsResponse ();
		String seeseeYear = request.getParameter ("year");
		Map<String, Object> params = Maps.newHashMap ();
		if (! StringUtils.isEmpty (request.getParameter ("job")))
		{
			params.put ("job", request.getParameter ("job"));
		}

		if (! StringUtils.isEmpty (request.getParameter ("name")))
		{
			params.put ("name", request.getParameter ("name"));
		}

		if (! StringUtils.isEmpty (request.getParameter ("rank")))
		{
			params.put ("rank", request.getParameter ("rank"));
		}

		if (! StringUtils.isEmpty (request.getParameter ("office")))
		{
			params.put ("office", request.getParameter ("office"));
		}

		if (! StringUtils.isEmpty (request.getParameter ("department")))
		{
			params.put ("department", request.getParameter ("department"));
		}

		if (StringUtils.isEmpty (seeseeYear))
		{
			seeseeYear = DateUtils.formatNow (YYYY);
		}

		String pageIndex = request.getParameter ("pageIndex");
		String pageSize = request.getParameter ("pageSize");
		if (StringUtils.isEmpty (pageIndex))
		{
			pageIndex = "1";
		}
		if (StringUtils.isEmpty (pageSize))
		{
			pageSize = "10";
		}
		params.put ("pageIndex", (Integer.valueOf (pageIndex) - 1) * Integer.valueOf (pageSize));
		params.put ("pageSize", pageSize);

		//查询员工编号
		List<Employee> employees = employeeMappper.queryAssessResultEmploy (params);

		if (! CollectionUtils.isEmpty (employees))
		{
			List<AssessGradeVo> assessGradeVos = Lists.newArrayList ();
			AssessGradeVo assessGradeVo = null;
			for (Employee employee : employees)
			{
				assessGradeVo = new AssessGradeVo ();
				String employeeId = employee.getId ();
				String employeeName = employee.getName ();
				assessGradeVo.setEmployeeName (employeeName);
				assessGradeVo.setEmployeeId (employeeId);
				//查询自评
				AssessResult assessResult = assessmentMapper.queryAssessResultBySelf (employeeId, seeseeYear);
				//自评分数
				if (null != assessResult)
				{
					Double score = assessResult.getScore () * assessResult.getWeight () / 100;
					assessGradeVo.setTotal (1);
					assessGradeVo.setScore (score);
				}

				//上级分数
				List<AssessResult> higherAssessResult = assessmentMapper
						.queryAssessResultByEmployee (employeeId, 60, seeseeYear);
				if (! CollectionUtils.isEmpty (higherAssessResult))
				{
					assessGradeVo.setTotal (assessGradeVo.getTotal () + higherAssessResult.size ());
					assessGradeVo.setHigherUpScore (statisticalResults (higherAssessResult, 60));
				}

				//下级分数
				List<AssessResult> lowerAssessResult = assessmentMapper
						.queryAssessResultByEmployee (employeeId, 20, seeseeYear);
				if (! CollectionUtils.isEmpty (lowerAssessResult))
				{
					assessGradeVo.setTotal (assessGradeVo.getTotal () + lowerAssessResult.size ());
					assessGradeVo.setLowerUpScore (statisticalResults (lowerAssessResult, 20));
				}

				//同级分数
				List<AssessResult> visAssessResult = assessmentMapper
						.queryAssessResultByEmployee (employeeId, 10, seeseeYear);
				if (! CollectionUtils.isEmpty (visAssessResult))
				{
					assessGradeVo.setTotal (assessGradeVo.getTotal () + visAssessResult.size ());
					assessGradeVo.setVis_a_vis_score (statisticalResults (visAssessResult, 10));
				}
				assessGradeVos.add (assessGradeVo);
			}
			qryAssessResultsResponse.setAssessGrades (assessGradeVos);
			qryAssessResultsResponse.setTotal (employeeMappper.queryAssessResultEmployCount (params));
		}
		return qryAssessResultsResponse;
	}

	/**
	 * 统计成绩
	 *
	 * @param assessResults 成绩列表
	 * @param weight        权重
	 * @return
	 */
	private static double statisticalResults (List<AssessResult> assessResults, int weight)
	{
		if (! CollectionUtils.isEmpty (assessResults))
		{
			double visTotal = 0;
			for (AssessResult result : assessResults)
			{
				visTotal += result.getScore ();
			}
			Double visScore = visTotal / assessResults.size () * weight / 100;
			return visScore;
		}
		return 0;
	}

	/**
	 * 删除考核主题
	 *
	 * @param request
	 * @param deleteAssessTopicRequest
	 * @return
	 * @throws Exception
	 */
	@Override
	public Result deleteAssessTopic (HttpServletRequest request, DeleteAssessTopicRequest deleteAssessTopicRequest)
			throws Exception
	{
		if (StringUtils.isEmpty (deleteAssessTopicRequest.getAssessTopicId ()))
		{
			return Result.fail ("考核主题编号不能为空.");
		}

		int flag = assessmentMapper.deleteAssessTopic (deleteAssessTopicRequest.getAssessTopicId ());

		if (flag > 0)
		{
			if (assessCatalogMapper.deleteCatalogRelas (deleteAssessTopicRequest.getAssessTopicId ()) > 0)
			{
				return Result.success ("delete assess result success.");
			}
			else
			{
				return Result.fail ("delete assess result failed.");
			}
		}
		else
		{
			return Result.fail ("当前考核主题不允许删除.");
		}
	}

	/**
	 * 添加考核分类
	 *
	 * @param request
	 * @param createAssessCatalogRequest
	 * @return
	 * @throws Exception
	 */
	@Override
	public Result addAssessCatalog (HttpServletRequest request, CreateAssessCatalogRequest createAssessCatalogRequest)
			throws Exception
	{
		if (null == createAssessCatalogRequest || null == createAssessCatalogRequest.getAssessCatalog ())
		{
			return Result.fail ("illega params.");
		}

		AssessCatalogVo assessCatalogVo = createAssessCatalogRequest.getAssessCatalog ();
		AssessCatalog assessCatalog = new AssessCatalog ();
		BeanUtils.copyProperties (assessCatalogVo, assessCatalog);
		int flag = assessCatalogMapper.add (assessCatalog);
		if (0 < flag)
		{
			return Result.success ("add assess catalog success");
		}
		else
		{
			return Result.fail ("add assess catalog failed.");
		}
	}

	/**
	 * 修改考核分类
	 *
	 * @param request
	 * @param modifyAssessCatalogRequest
	 * @return
	 * @throws Exception
	 */
	@Override
	public Result modifyAssessCatalog (HttpServletRequest request,
			ModifyAssessCatalogRequest modifyAssessCatalogRequest) throws Exception
	{
		if (null == modifyAssessCatalogRequest || null == modifyAssessCatalogRequest.getAssessCatalog ())
		{
			return Result.fail ("illega params.");
		}

		AssessCatalogVo assessCatalogVo = modifyAssessCatalogRequest.getAssessCatalog ();
		AssessCatalog assessCatalog = new AssessCatalog ();
		BeanUtils.copyProperties (assessCatalogVo, assessCatalog);
		int flag = assessCatalogMapper.modify (assessCatalog);
		if (0 < flag)
		{
			return Result.success ("add assess catalog success");
		}
		else
		{
			return Result.fail ("add assess catalog failed.");
		}
	}

	/**
	 * 删除考核分类
	 *
	 * @param request
	 * @param deleteAssessCatalogRequest
	 * @return
	 * @throws Exception
	 */
	@Override
	public Result deleteAssessCatalog (HttpServletRequest request,
			DeleteAssessCatalogRequest deleteAssessCatalogRequest) throws Exception
	{
		if (null == deleteAssessCatalogRequest || StringUtils
				.isEmpty (deleteAssessCatalogRequest.getAssessCatalogId ()))
		{
			return Result.fail ("illega params.");
		}

		int flag = assessCatalogMapper.delete (deleteAssessCatalogRequest.getAssessCatalogId ());

		if (0 < flag)
		{
			return Result.success ("delete assess catalog success");
		}
		else
		{
			return Result.fail ("delete assess catalog failed.");
		}
	}

	/**
	 * 查询考核分类信息
	 *
	 * @param request
	 * @param queryAssessCatalogRequest
	 * @return
	 * @throws Exception
	 */
	@Override
	public QryAssessCatalogByIdResponse queryAssessCatalogList (HttpServletRequest request,
			QueryAssessCatalogRequest queryAssessCatalogRequest) throws Exception
	{
		if (null == queryAssessCatalogRequest || StringUtils.isEmpty (queryAssessCatalogRequest.getAssessCatalogId ()))
		{
			return (QryAssessCatalogByIdResponse) Result.fail ("illega params.");
		}
		QryAssessCatalogByIdResponse qryAssessCatalogByIdResponse = new QryAssessCatalogByIdResponse ();
		AssessCatalog assessCatalog = assessCatalogMapper
				.queryCatalogById (queryAssessCatalogRequest.getAssessCatalogId ());
		AssessCatalogVo assessCatalogVo = null;
		if (null != assessCatalog)
		{
			assessCatalogVo = new AssessCatalogVo ();
			BeanUtils.copyProperties (assessCatalog, assessCatalogVo);
		}
		qryAssessCatalogByIdResponse.setAssessCatalog (assessCatalogVo);
		return qryAssessCatalogByIdResponse;
	}

	/**
	 * 查询考核分类信息
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@Override
	public QryAssessCatalogResponse queryAssessCatalogList (HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		QryAssessCatalogResponse qryAssessCatalogResponse = new QryAssessCatalogResponse ();
		Map<String, Object> params = Maps.newHashMap ();
		String catalogName = request.getParameter ("name");
		if (! StringUtils.isEmpty (catalogName))
		{
			params.put ("name", catalogName);
		}

		String pageIndex = request.getParameter ("pageIndex");
		String pageSize = request.getParameter ("pageSize");
		if (StringUtils.isEmpty (pageIndex))
		{
			pageIndex = "1";
		}
		if (StringUtils.isEmpty (pageSize))
		{
			pageSize = "10";
		}
		params.put ("pageIndex", (Integer.valueOf (pageIndex) - 1) * Integer.valueOf (pageSize));
		params.put ("pageSize", pageSize);

		List<AssessCatalog> assessCatalogs = assessCatalogMapper.queryList (params);
		List<AssessCatalogVo> assessCatalogVos = null;

		if (! CollectionUtils.isEmpty (assessCatalogs))
		{
			assessCatalogVos = Lists.newArrayList ();

			AssessCatalogVo assessCatalogVo = null;
			for (AssessCatalog assessCatalog : assessCatalogs)
			{
				assessCatalogVo = new AssessCatalogVo ();
				BeanUtils.copyProperties (assessCatalog, assessCatalogVo);
				assessCatalogVos.add (assessCatalogVo);
			}
			qryAssessCatalogResponse.setCount (assessCatalogMapper.queryCount (params));
		}
		qryAssessCatalogResponse.setAssessCatalogs (assessCatalogVos);
		return qryAssessCatalogResponse;
	}
}
