package cn.xyz.mianshi.model;

public class PageExample {
	protected int pageIndex = 0;
	protected int pageSize = 20;
	protected int offset;
	protected int limit;

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getOffset() {
		offset = pageIndex * pageSize;
		return offset;
	}

	public int getLimit() {
		limit = pageSize;
		return limit;
	}

}
