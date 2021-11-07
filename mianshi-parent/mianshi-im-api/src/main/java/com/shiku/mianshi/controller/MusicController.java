package com.shiku.mianshi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;

/**
 * 音乐模块接口
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/music")
public class MusicController extends AbstractController {

	@RequestMapping(value = "/list")
	public JSONMessage queryMusicList(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "20") Integer pageSize,@RequestParam(defaultValue = "") String keyword) {
		Object data = SKBeanUtils.getLocalSpringBeanManager().getMusicManager()
				.queryMusicInfo(pageIndex, pageSize, keyword);
		return JSONMessage.success(null, data);
	}
	
}
