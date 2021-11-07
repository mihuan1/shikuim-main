package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import lombok.Data;

/**
 * 系统 接口调用日志
 * @author lidaye
 *
 */

@Data
@Entity(value="SysApiLog",noClassnameStored=true)
public class SysApiLog {
	@Id
	private ObjectId id;
	
	/**
	 * 接口id className+methodName
	 */
	@Indexed
	private String apiId;
	
	/**
	 * 请求用户ID
	 */
	private int userId;
	
	/**
	 * 请求时间
	 */
	@Indexed
	private long time;
	
	/**
	 * RequestURI + 请求参数
	 */
	private String fullUri;
	
	/**
	 *  User-Agent
	 *  设备信息
	 */
	private String userAgent;
	
	/**
	 * 客户端ip
	 */
	private String clientIp;
	
	/**
	 * 接口耗时  （毫秒）
	 */
	private long totalTime;
	
	/**
	 * 异常信息
	 */
	private String stackTrace;
	
	
	

}
