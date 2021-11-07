package com.shiku.mianshi.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.xyz.commons.constants.KConstants.Result;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.CommonText;
/**
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/CustomerService")
public class CustomerController extends AbstractController {
	private static Logger logger = LoggerFactory.getLogger(CustomerController.class);
	
	/**
	 * 客服模块-客户注册
	 * @param example
	 * @return
	 */
	@RequestMapping(value = "/register")
	public JSONMessage customerRegister(@RequestParam String companyId,@RequestParam String departmentId) {
		String requestIp = getRequestIp(); //获取注册用户ip地址
		//String macAddress = getMACAddress(requestIp);   //根据ip获取用户mac地址
		Object data = SKBeanUtils.getCustomerManager().registerUser(companyId,departmentId,requestIp);
		return JSONMessage.success(null, data);
	}
	
	/**
	* @Title: commonTextAdd
	* @Description: 创建常用语
	* @param @param commonText
	* @param @return    参数
	* @return JSONMessage    返回类型
	* @throws
	*/
	@RequestMapping("/commonText/add")
	public JSONMessage commonTextAdd(@Valid CommonText commonText){
		
		JSONMessage jsonMessage = Result.ParamsAuthFail;
		try {
			if (!StringUtil.isEmpty(commonText.toString())) {
				commonText = SKBeanUtils.getCustomerManager().commonTextAdd(commonText);
				return JSONMessage.success("", commonText);
			}else{
				return jsonMessage;
			}
		} catch (Exception e) {
			logger.error("添加关键字失败！");
			return jsonMessage;
		}
	}
	
	/**
	* @Title: deleteCommonText
	* @Description: 删除常用语
	* @param @param commonTextId
	* @param @return    参数
	* @return JSONMessage    返回类型
	* @throws
	*/
	@RequestMapping("/commonText/delete")
	public JSONMessage deleteCommonText(@RequestParam String commonTextId){
		
		JSONMessage jsonMessage = Result.ParamsAuthFail;
		try {
			if (!StringUtil.isEmpty(commonTextId)) {
				SKBeanUtils.getCustomerManager().deleteCommonTest(commonTextId);
				return JSONMessage.success("", null);
			}else{
				return jsonMessage;
			}
		} catch (Exception e) {
			logger.error("删除关键字失败！");
			return jsonMessage;
		}
	}
	
	/**
	* @Title: commonTextGet
	* @Description: 根据公司id查询常用语
	* @param @param companyId
	* @param @param pageIndex
	* @param @param pageSize
	* @param @return    参数
	* @return JSONMessage    返回类型
	* @throws
	*/
	@RequestMapping("/commonText/get")
	public JSONMessage commonTextGetByCompanyId(@RequestParam String companyId,@RequestParam(defaultValue = "0") int pageIndex,@RequestParam(defaultValue = "10") int pageSize){
		JSONMessage jsonMessage = Result.ParamsAuthFail;
		try {
			List<CommonText> commonTextList = SKBeanUtils.getCustomerManager().commonTextGetByCompanyId(companyId, pageIndex, pageSize);
			if (!StringUtil.isEmpty(commonTextList.toString()) && commonTextList.size()>0) {
				return JSONMessage.success("", commonTextList);
			}else{
				return JSONMessage.failure(null);
			}
		} catch (Exception e) {
			logger.error("查询常用语失败！");
			return jsonMessage;
		}
	}
	
	/**
	* @Title: commonTextGetByUserId
	* @Description: 根据userId查询常用语
	* @param @param pageIndex   页码
	* @param @param pageSize    每页数量
	* @param @return    参数
	* @return JSONMessage    返回类型
	* @throws
	*/
	@RequestMapping("/commonText/getByUserId")
	public JSONMessage commonTextGetByUserId(@RequestParam(defaultValue = "0") int pageIndex,@RequestParam(defaultValue = "10") int pageSize){
		JSONMessage jsonMessage = Result.ParamsAuthFail;
		try {
			List<CommonText> commonTextList = SKBeanUtils.getCustomerManager().commonTextGetByUserId(ReqUtil.getUserId(), pageIndex, pageSize);
			if (!StringUtil.isEmpty(commonTextList.toString()) && commonTextList.size()>0) {
				return JSONMessage.success("", commonTextList);
			}else{
				return JSONMessage.failure(null);
			}
		} catch (Exception e) {
			logger.error("查询常用语失败！");
			return jsonMessage;
		}
	}

	/**
	* @Title: commonTextModify
	* @Description: 修改常用语
	* @param @param commonText
	* @param @return    参数
	* @return JSONMessage    返回类型
	* @throws
	*/
	@RequestMapping("/commonText/modify")
	public JSONMessage commonTextModify(CommonText commonText){
		JSONMessage jsonMessage = Result.ParamsAuthFail;
		try {
			if (!StringUtil.isEmpty(commonText.toString())) {
				SKBeanUtils.getCustomerManager().commonTextModify(commonText);
				return JSONMessage.success("", null);
			}else{
				return jsonMessage;
			}
		} catch (Exception e) {
			logger.error("修改常用语失败！");
			return jsonMessage;
		}
		
	}
	
	/**
	 * 此接口用于将查找customer客户表中的数据，然后封装成 user 返回
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/getUser")
	public JSONMessage getUser(@RequestParam String customerId) {
		
		Object data = SKBeanUtils.getCustomerManager().getUser(customerId);
		return JSONMessage.success(null, data);
	}
	
	
	
}
