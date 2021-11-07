package cn.xyz.mianshi.vo;

import java.util.HashSet;
import java.util.Set;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Data;

/**
 * @author lidaye
 *用户 权限验证数据表
 */
@Data
@Entity(value = "auth_keys",noClassnameStored = true)
public class AuthKeys {

	@Id
	private int userId;
	
	private String password;
	
	private long createTime;
	
	private long modifyTime;
	/**
	 *支付公钥
	 */
	private String payPublicKey;
	/**
	 *支付密钥
	 */
	private String payPrivateKey;
	
	/**
	 *消息公钥
	 */
	private String msgPublicKey;
	/**
	 *消息密钥
	 */
	private String msgPrivateKey;
	
	// 用户支付密码
	private String payPassword;
	
	private Set<PublicKey> publicKeyList=new HashSet<PublicKey>();
	
	@Data
	public static class PublicKey{
		private long time;
		private String key;
		
		
	}
	
	
}
