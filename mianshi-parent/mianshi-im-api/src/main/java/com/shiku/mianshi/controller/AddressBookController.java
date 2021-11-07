package com.shiku.mianshi.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.impl.AddressBookManagerImpl;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.AddressBook;
import cn.xyz.mianshi.vo.User;

@RestController
public class AddressBookController extends AbstractController{
	private static AddressBookManagerImpl getAddressBookManager(){
		AddressBookManagerImpl addressBookManger = SKBeanUtils.getAddressBookManger();
		return addressBookManger;
	};
	@RequestMapping(value = "/addressBook/upload")
	public JSONMessage upload(HttpServletRequest request, @RequestParam(defaultValue="")String deleteStr,@RequestParam(defaultValue="")String uploadStr,@RequestParam(defaultValue="")String uploadJsonStr){
		Integer userId = ReqUtil.getUserId();
		List<AddressBook> uploadTelephone = null;
		if(StringUtil.isEmpty(deleteStr) && StringUtil.isEmpty(uploadStr) && StringUtil.isEmpty(uploadJsonStr))
			return new JSONMessage(KConstants.ResultCode.ParamsLack,"");
		if(!StringUtil.isEmpty(uploadStr) && !StringUtil.isEmpty(uploadJsonStr))
			return new JSONMessage(KConstants.ResultCode.ParamsLack,"参数有误");
		User user = SKBeanUtils.getUserManager().getUser(userId);
		uploadTelephone = getAddressBookManager().uploadTelephone(user,deleteStr, uploadStr, uploadJsonStr);
		return JSONMessage.success(null,uploadTelephone);
	}
	
	
	/** @Description:（查询通讯录好友） 
	* @param pageIndex
	* @param pageSize
	* @return
	**/ 
	@RequestMapping(value = "/addressBook/getAll")
	public JSONMessage getAll(@RequestParam(defaultValue="0") int pageIndex,@RequestParam(defaultValue="20") int pageSize) {
		Integer userId = ReqUtil.getUserId();
		User user = SKBeanUtils.getUserManager().getUser(userId);
		List<AddressBook> data=getAddressBookManager().getAll(user.getTelephone(),pageIndex, pageSize);
		if(null==data){
			return JSONMessage.failure("没有通讯录好友");
		}else {
			return JSONMessage.success(null, data);
		}
			
	}
	/** @Description:（查询已注册的通讯录好友） 
	* @param pageIndex
	* @param pageSize
	* @return
	**/ 
	@RequestMapping(value = "/addressBook/getRegisterList")
	public JSONMessage getRegisterList(@RequestParam(defaultValue="0") int pageIndex,@RequestParam(defaultValue="20") int pageSize) {
		List<AddressBook> data=getAddressBookManager().findRegisterList(getSession(), pageIndex, pageSize);
		return JSONMessage.success(null,data);
	}
	

}
