package com.shiku.mianshi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.NearbyUser;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.User.LoginLog;

/**
 * 附近接口
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/nearby")
public class NearbyController {
	
	//附近的用户
	@RequestMapping(value = "/user")
	public JSONMessage nearbyUser(@ModelAttribute NearbyUser poi) {
		try {
			List<User> nearbyUser=SKBeanUtils.getUserManager().nearbyUser(poi);
				return JSONMessage.success(null,nearbyUser);
			
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	
	
	//附近的用户（用于web版分页）
	@RequestMapping(value = "/nearbyUserWeb")
	public JSONMessage nearbyUserWeb(@ModelAttribute NearbyUser poi) {
		try {
			Object nearbyUser = SKBeanUtils.getUserManager().nearbyUserWeb(poi);
			return JSONMessage.success(null,nearbyUser);
		} catch (Exception e) {
			return JSONMessage.failure("暂无该用户");
		}
		
	}
	
	
	//最新的用户
	@RequestMapping("/newUser")
	public JSONMessage newUser(@RequestParam(defaultValue="0") int pageIndex,@RequestParam(defaultValue="12") int pageSize,@RequestParam(defaultValue="0") int isAuth) {
		JSONMessage jMessage = null;
		try {
			String phone = SKBeanUtils.getUserManager().getUser(ReqUtil.getUserId()).getPhone();
			if(!StringUtil.isEmpty(phone) && !phone.equals("18938880001")) {
				return JSONMessage.failure("权限不足");
			}
			List<User> dataList = SKBeanUtils.getUserManager().getUserlimit(pageIndex, pageSize,isAuth);
			if(null != dataList && dataList.size()>0){
				LoginLog loginLog=null;
				for (User user : dataList) {
					loginLog=SKBeanUtils.getUserRepository().getLogin(user.getUserId());
					user.setLoginLog(loginLog);
				}
				jMessage = JSONMessage.success(null,dataList);
			}
		} catch (Exception e) {
			e.printStackTrace();
			jMessage = JSONMessage.error(e);
		}
		return jMessage;
	}
	
}
