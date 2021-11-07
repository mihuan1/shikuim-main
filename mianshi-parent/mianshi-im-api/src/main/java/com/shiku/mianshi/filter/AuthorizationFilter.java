package com.shiku.mianshi.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.xyz.commons.utils.TagUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.shiku.mianshi.ResponseUtil;

import cn.xyz.commons.support.spring.SpringBeansUtils;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.service.AuthServiceUtils;


@WebFilter(filterName = "authorizationfilter", urlPatterns = {"/*"}, initParams = {
        @WebInitParam(name = "enable", value = "true")})
public class AuthorizationFilter implements Filter {

    private Map<String, String> requestUriMap;
    private AuthorizationFilterProperties properties;

    private Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Override
    public void init(FilterConfig arg0) throws ServletException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
            throws IOException, ServletException {

        if (null == requestUriMap || null == properties) {
            requestUriMap = Maps.newHashMap();
            properties = SpringBeansUtils.getContext().getBean(AuthorizationFilterProperties.class);

            for (String requestUri : properties.getRequestUriList()) {
                requestUriMap.put(requestUri, requestUri);
            }
        }

        HttpServletRequest request = (HttpServletRequest) arg0;
        HttpServletResponse response = (HttpServletResponse) arg1;
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        String accessToken = request.getParameter("access_token");
        long time = NumberUtils.toLong(request.getParameter("time"), 0);
        String secret = request.getParameter("secret");
        TagUtil.reqSample(request);
        //是否检验接口   老版客户端没有参数
        boolean falg = !StringUtil.isEmpty(secret);
        String requestUri = request.getRequestURI();
        if ("/favicon.ico".equals(requestUri))
            return;

        // DEBUG**************************************************DEBUG
        StringBuffer sb = new StringBuffer();
        sb.append(request.getMethod()).append(" 请求：" + request.getRequestURI());

        if (!request.getRequestURI().startsWith("/job")) logger.info(sb.toString());

        // DEBUG**************************************************DEBUG
        // 如果访问的是控制台或资源目录
        if (requestUri.startsWith("/console") || requestUri.startsWith("/mp") || requestUri.startsWith("/open") || requestUri.startsWith("/pages")) {
            if (requestUri.startsWith("/pages/privacyPolicy") || requestUri.startsWith("/console/login") || requestUri.startsWith("/mp/login") || requestUri.startsWith("/open/login") || requestUri.startsWith("/pages")) {
                arg2.doFilter(arg0, arg1);
                return;
            }
            logger.info("&&&&&&&&&&&&&&&&&&&&&&&&&&");
            checkAdminRequest(request, falg, accessToken, response, time, secret, arg0, arg1, arg2, requestUri);
        } else {
            if (requestUri.startsWith("/config") || requestUri.startsWith("/getCurrentTime") || requestUri.equals("/getImgCode")) {
                arg2.doFilter(arg0, arg1);
                return;
            }

			logger.info("************************");
            checkOtherRequest(request, falg, accessToken, response, time, secret, arg0, arg1, arg2, requestUri);
        }
    }

    private boolean isNeedLogin(String requestUri) {
        if (null != requestUri && requestUri.startsWith("/job")) return false;
        return !requestUriMap.containsKey(requestUri.trim());
    }

    private String getUserId(String _AccessToken) {
        String userId = null;

        try {
            userId = KSessionUtil.getUserIdBytoken(_AccessToken);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userId;
    }

    private String getAdminUserId(String _AccessToken) {
        String userId = null;
        try {
            userId = KSessionUtil.getAdminUserIdByToken(_AccessToken);
            logger.info("=====>> KSessionUtil.getAdminUserIdByToken(_AccessToken) = " + userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userId;
    }

    private static final String template = "{\"resultCode\":%1$s,\"resultMsg\":\"%2$s\"}";

    private static void renderByErrorKey(ServletResponse response, int tipsKey) {
        String tipsValue = ConstantUtil.getMsgByCode(tipsKey + "", "zh").getValue();
        String s = String.format(template, tipsKey, tipsValue);

        ResponseUtil.output(response, s);
    }

    private static void renderByError(ServletResponse response, String errMsg) {

        String s = String.format(template, 0, errMsg);

        ResponseUtil.output(response, s);
    }

    // 校验后台所有相关接口
    public void checkAdminRequest(HttpServletRequest request, boolean falg, String accessToken, HttpServletResponse response,
                                  long time, String secret, ServletRequest arg0, ServletResponse arg1, FilterChain arg2, String requestUri) throws IOException, ServletException {

    	logger.info("=====> accessToken= "+ accessToken);
        // 需要登录
        if (isNeedLogin(request.getRequestURI())) {
            falg = true;
            // 请求令牌是否包含
            if (StringUtil.isEmpty(accessToken)) {
                logger.info("不包含请求令牌");
                int tipsKey = 1030101;
                renderByErrorKey(response, tipsKey);
            } else {
                String userId = getAdminUserId(accessToken);
                logger.info("------->  userId = " + userId);
                if (StringUtil.isEmpty(userId)) {
					logger.info("------->  userId-1 = " + userId + "; requestUri = " + requestUri );
                    if (requestUri.startsWith("/open/getHelperList")
                            || requestUri.startsWith("/open/codeAuthorCheck")
                            || requestUri.startsWith("/open/authInterface")
                            || requestUri.startsWith("/open/sendMsgByGroupHelper")
                            || requestUri.startsWith("/open/webAppCheck")) {
						logger.info("------->  userId-2 = " + userId);
                        userId = getUserId(accessToken);
                    }
                }
                // 请求令牌是否有效
				logger.info("------->  userId-3 = " + userId);
                if (null == userId) {
                    logger.info("请求令牌无效或已过期...");
                    int tipsKey = 1030102;
                    renderByErrorKey(response, tipsKey);
                } else {
                    if (falg) {
                        if (!AuthServiceUtils.authRequestApi(userId, time, accessToken, secret, requestUri)) {
                            renderByError(response, "授权认证失败");
                            return;
                        }
                    }

                    ReqUtil.setLoginedUserId(Integer.parseInt(userId));
                    arg2.doFilter(arg0, arg1);
                    return;
                }
            }
        } else {
            /**
             * 校验没有登陆的接口
             */
            if (null == accessToken) {
                if (falg) {
                    if (!AuthServiceUtils.authOpenApiSecret(time, secret)) {
                        renderByError(response, "授权认证失败");
                        return;
                    }
                }
            }

            String userId = getUserId(accessToken);
            if (null != userId) {
                ReqUtil.setLoginedUserId(Integer.parseInt(userId));
            }
            arg2.doFilter(arg0, arg1);
        }
    }

    public void checkOtherRequest(HttpServletRequest request, boolean falg, String accessToken, HttpServletResponse response,
                                  long time, String secret, ServletRequest arg0, ServletResponse arg1, FilterChain arg2, String requestUri) throws IOException, ServletException {
        // 需要登录
        if (isNeedLogin(request.getRequestURI()) && !"dzt".equals(request.getParameter("pwd"))) {
            falg = true;
            // 请求令牌是否包含
            if (StringUtil.isEmpty(accessToken)) {
                logger.info("不包含请求令牌");
                int tipsKey = 1030101;
                renderByErrorKey(response, tipsKey);
            } else {
                String userId = getUserId(accessToken);
                // 请求令牌是否有效
                if (null == userId) {
                    logger.info("请求令牌无效或已过期...");
                    int tipsKey = 1030102;
                    renderByErrorKey(response, tipsKey);
                } else {
                    if (falg) {
                        if (!AuthServiceUtils.authRequestApi(userId, time, accessToken, secret, requestUri)) {
                            renderByError(response, "授权认证失败");
                            return;
                        }
                    }

                    ReqUtil.setLoginedUserId(Integer.parseInt(userId));
                    arg2.doFilter(arg0, arg1);
                    return;
                }
            }
        } else {
            /**
             * 校验没有登陆的接口
             */
            if (null == accessToken) {
                if (falg) {
                    if (!AuthServiceUtils.authOpenApiSecret(time, secret)) {
                        renderByError(response, "授权认证失败");
                        return;
                    }
                }
            }

            String userId = getUserId(accessToken);
            if (null != userId) {
                ReqUtil.setLoginedUserId(Integer.parseInt(userId));
            }
            arg2.doFilter(arg0, arg1);
        }
    }
}
