package com.shiku.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
* @Description: TODO(用一句话描述该文件做什么)
* @author lidaye
* @date 2018年7月27日 
*/
@Controller
public class UploadController {
	
	@RequestMapping("/")
	public String  index() {
		return "index";
	}
	
	
	
	

}

