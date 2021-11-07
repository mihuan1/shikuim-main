package cn.xyz.commons.utils;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bson.types.ObjectId;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.mongodb.DBObject;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.mianshi.vo.User;

public class ReqUtil {

	private static final String name = "LOGIN_USER_ID";

	public static void setLoginedUserId(int userId) {
		RequestContextHolder.getRequestAttributes().setAttribute(name, userId, RequestAttributes.SCOPE_REQUEST);
	}

	public static Integer getUserId() {
		// 获取AuthorizationFilter通过查询令牌用户映射设置的userId
		Object obj = RequestContextHolder.getRequestAttributes().getAttribute(name, RequestAttributes.SCOPE_REQUEST);
		// if (null == obj) {
		// HttpServletRequest request = ((ServletRequestAttributes)
		// RequestContextHolder.getRequestAttributes()).getRequest();
		// obj = request.getParameter("userId");
		// obj = (null == obj || "".equals(obj)) ? null : obj;
		// }
		return null == obj ? 0 : Integer.parseInt(obj.toString());
	}

	public static User getUser() {
		return null;
	}

	public HttpServletRequest getRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}

	public static ObjectId parseId(String s) {
		try {
			return (null == s || "".equals(s.trim())) ? null : new ObjectId(s);
		} catch (Exception e) {
			throw new ServiceException("请求参数错误");
		}
	}

	public static DBObject parseDBObj(String s) {
		return (DBObject) com.mongodb.util.JSON.parse(s);
	}

	public static List<ObjectId> parseArray(String text) {
		try {
			return new ObjectMapper().readValue(text, TypeFactory.defaultInstance().constructCollectionType(List.class, ObjectId.class));
		} catch (Exception e) {
			throw new ServiceException("请求参数错误");
		}
	}

}
