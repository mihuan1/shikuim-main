package com.shiku.mianshi.controller;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
public class SercretChannelContoller {
    private static final Logger LOGGER = LoggerFactory.getLogger(SercretChannelContoller.class);
    @RequestMapping(value = "/saveRemoteData")
    public Object saveRemoteData(@ModelAttribute RemoteReq req) {
        try {
            LOGGER.info(JSON.toJSONString(req));
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Data
    public static class RemoteReq {
        private String server_name;
        private String remote_addr;
        private String header_referer;
        private String header_origin;
        private String header_host;
    }
}
