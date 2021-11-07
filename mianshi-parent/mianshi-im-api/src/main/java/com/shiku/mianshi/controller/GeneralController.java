package com.shiku.mianshi.controller;

import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.vo.FriendsterWebsite;
import cn.xyz.mianshi.utils.SKBeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/general")
public class GeneralController extends AbstractController {

	@RequestMapping(value = "/agora/info")
	public JSONMessage agoraInfo(String channel) {
		return JSONMessage.success(SKBeanUtils.getAgoraManager().getAgoraProperty(ReqUtil.getUserId(), channel));
	}

    /**
     * 朋友圈页自定义网站列表
     *
     * @param page
     * @param limit
     * @return
     */
    @RequestMapping(value = "/friendsterWebsiteList")
    public JSONMessage queryReceiptQRList(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") Integer limit) {
        PageResult<FriendsterWebsite> result = SKBeanUtils.getLocalSpringBeanManager().getFriendsterWebsiteManager().queryFriendsterWebsiteList(page,
                limit);
        return JSONMessage.success(result);
    }
}
