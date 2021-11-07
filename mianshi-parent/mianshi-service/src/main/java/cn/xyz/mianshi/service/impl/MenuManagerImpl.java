package cn.xyz.mianshi.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;

import cn.xyz.commons.IdWorker;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.model.MenuVO;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.Menu;
import cn.xyz.mianshi.vo.User;

@Service
public class MenuManagerImpl extends MongoRepository<Menu, ObjectId>{

	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<Menu> getEntityClass() {
		return Menu.class;
	}
	
	public List<Menu> getMenu(int userId){
		Query<Menu> q = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId).field("parentId")
				.equal(0);
		List<Menu> data = q.order("index").asList();
		if (null != data && data.size() > 0) {
			for (Menu menu : data) {
				q = getDatastore().createQuery(getEntityClass()).field("parentId").equal(menu.getId()).order("index");
				List<Menu> urlList = q.asList();
				for(Menu urlMenu : urlList){
					if(!StringUtil.isEmpty(urlMenu.getUrl()))
						urlMenu.setUrl(urlMenu.getUrl().replaceAll(" ", ""));
				}
				menu.setMenuList(urlList);
			}
		}
		return data;
	} 
	
	public List<MenuVO> navMenu(int userId) {
		Query<Menu> q = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId).field("parentId").equal(0);
		List<Menu> menuList = q.order("index").asList();
		List<MenuVO> list = new ArrayList<>();
		List<MenuVO> listMenu = new ArrayList<>();
		MenuVO menuVO = null;
		MenuVO menus = null;
		if (null != menuList && menuList.size() > 0) {
			for (Menu menu : menuList) {

				q = getDatastore().createQuery(getEntityClass()).field("parentId").equal(menu.getId());
				menu.setMenuList(q.order("index").asList());
				menuVO = new MenuVO();
				menuVO.setId(String.valueOf(menu.getId()));
				menuVO.setIndex(menu.getIndex());
				menuVO.setMenuId(menu.getMenuId());
				menuVO.setDesc(menu.getDesc());
				menuVO.setParentId(menu.getParentId());
				menuVO.setUrl(menu.getUrl());
				menuVO.setName(menu.getName());
				for (Menu me : menu.getMenuList()) {
					menus = new MenuVO();
					menus.setId(String.valueOf(me.getId()));
					menus.setIndex(me.getIndex());
					menus.setMenuId(me.getMenuId());
					menus.setDesc(me.getDesc());
					menus.setParentId(me.getParentId());
					menus.setUrl(me.getUrl());
					menus.setName(me.getName());
					listMenu.add(menus);
				}
				menuVO.setMenuList(listMenu);
				list.add(menuVO);

			}
		}
		return list;
	}
	
	@SuppressWarnings("deprecation")
	public JSONObject getHomeCount(int userId) {
		JSONObject obj = new JSONObject();
		Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(userId);
		long fansCount = q.countAll();
		Query<User> qUser = getDatastore().createQuery(User.class);
		long userCount = qUser.countAll();
		BasicDBObject query = new BasicDBObject("direction", 0);
		query.append("receiver", userId);
		query.append("isRead", 0);
		long msgCount = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_msgs").count(query);
		obj.put("msgCount", msgCount);
		obj.put("fansCount", fansCount);
		obj.put("userCount", userCount);
		return obj;
	}
	
	public void menuOp(int userId, String op, long parentId, String desc, String name, int index, String urls, long id,String menuId,
			HttpServletResponse response) {
		Menu entity = new Menu();
		try {
			if ("save".equals(op)) {
				entity.setId(IdWorker.getId());
				entity.setUserId(userId);
				entity.getName();

				entity.setParentId(parentId);
				entity.setIndex(index);
				if (!StringUtil.isEmpty(desc))
					entity.setDesc(desc);
				if (!StringUtil.isEmpty(name))
					entity.setName(name);
				if (!StringUtil.isEmpty(urls))
					entity.setUrl(urls);
				if(!StringUtil.isEmpty(menuId))
					entity.setMenuId(menuId);
				getDatastore().save(entity);
				response.sendRedirect("/mp/menuList");
			} else if ("delete".equals(op)) {
				getDatastore().delete(getDatastore().createQuery(getEntityClass()).field("_id").equal(id));
				response.sendRedirect("/mp/menuList");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	public Map<String, Long> getFans(int userId){
		Map<String, Long> map = Maps.newConcurrentMap();
		Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(userId);
		long fansCount=q.countAll();
		map.put("fansCount", fansCount);
		BasicDBObject query=new BasicDBObject("direction", 0);
		query.append("receiver", userId);
		query.append("isRead", 0);
		long msgCount = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_msgs").count(query);
		map.put("msgCount", msgCount);
		return map;
	}
	
	public void saveupdate(Menu entity) {
		Query<Menu> query = getDatastore().createQuery(getEntityClass()).field("_id").equal(entity.getId());
		UpdateOperations<Menu> ops = getDatastore().createUpdateOperations(getEntityClass());
		if (0 != entity.getParentId())
			ops.set("parentId", entity.getParentId());
		if (!StringUtil.isEmpty(entity.getName()))
			ops.set("name", entity.getName());
		
		ops.set("url", entity.getUrl());
		if (0 != entity.getIndex())
			ops.set("index", entity.getIndex());
		if (!StringUtil.isEmpty(entity.getDesc()))
			ops.set("desc", entity.getDesc());
		if(!StringUtil.isEmpty(entity.getMenuId()))
			ops.set("menuId", entity.getMenuId());
		getDatastore().findAndModify(query, ops);
	}
	
}
