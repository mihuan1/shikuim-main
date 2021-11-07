package cn.xyz.mianshi.model;

import java.util.List;

public class MenuVO {
	
	private String id;
	private String menuId;
	private long parentId;
	private int userId;
	private int index;
	private String name;
	private String desc;
	private String url;
	private List<MenuVO> menuList;
	
	
	public MenuVO() {
		// TODO Auto-generated constructor stub
	}
	
	
	public MenuVO(String id, String menuId, long parentId, int userId, int index, String name, String desc,
			String url, List<MenuVO> menuList) {
		this.id = id;
		this.menuId = menuId;
		this.parentId = parentId;
		this.userId = userId;
		this.index = index;
		this.name = name;
		this.desc = desc;
		this.url = url;
		this.menuList = menuList;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMenuId() {
		return menuId;
	}
	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}
	public long getParentId() {
		return parentId;
	}
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public List<MenuVO> getMenuList() {
		return menuList;
	}
	public void setMenuList(List<MenuVO> menuList) {
		this.menuList = menuList;
	}
	
}
