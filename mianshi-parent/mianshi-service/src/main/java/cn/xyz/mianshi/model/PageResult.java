package cn.xyz.mianshi.model;

import java.util.List;

/*
 * 页面列表返回值
 */

public class PageResult<T> {
	
	
	protected List<T> data;
	
	
	protected long count;
	
	
	protected double total;  //该属性用于记录对data 数据某个值的求和
	
	protected String totalVo;  //该属性用于记录多个数据处理
	
	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public PageResult() {
		super();
	}

	public PageResult(List<T> data, long count) {
		
		this.data = data;
		this.count = count;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	/**
	 * @return the totalVo
	 */
	public String getTotalVo() {
		return totalVo;
	}

	/**
	 * @param totalVo the totalVo to set
	 */
	public void setTotalVo(String totalVo) {
		this.totalVo = totalVo;
	}


	

}
