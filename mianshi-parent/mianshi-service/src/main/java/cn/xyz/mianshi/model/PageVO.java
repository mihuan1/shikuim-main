package cn.xyz.mianshi.model;

import java.util.List;

public class PageVO {

	protected Long allPageCount;
	protected Long pageCount;
	protected List<?> pageData;
	protected Integer pageIndex = 0;
	protected Integer pageSize = 15;
	protected Integer start;
	protected Long total;

	public PageVO() {
		super();
	}

	public PageVO(List<?> pageData, Long total) {
		super();
		this.pageData = pageData;
		this.total = total;
		this.pageCount = total / pageSize + (total % pageSize > 0 ? 1 : 0);
	}

	public PageVO(List<?> pageData, Long total, Integer pageIndex,
			Integer pageSize) {
		super();
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
		this.pageData = pageData;
		this.total = total;
		this.pageCount = total / pageSize + (total % pageSize > 0 ? 1 : 0);
	}
	
	public PageVO(List<?> pageData, Long total, Integer pageIndex,
			Integer pageSize,Long allPageCount) {
		super();
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
		this.pageData = pageData;
		this.total = total;
		this.pageCount = total / pageSize + (total % pageSize > 0 ? 1 : 0);
		this.allPageCount=allPageCount;
	}

	public Long getPageCount() {
		this.pageCount = total / pageSize + (total % pageSize > 0 ? 1 : 0);
		return this.pageCount;
	}

	public Long getAllPageCount() {
		return allPageCount;
	}

	public void setAllPageCount(Long allPageCount) {
		this.allPageCount = allPageCount;
	}

	public List<?> getPageData() {
		return pageData;
	}

	public void setPageData(List<?> pageData) {
		this.pageData = pageData;
	}

	public Integer getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getStart() {
		return start = (pageIndex * pageSize);
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

}
