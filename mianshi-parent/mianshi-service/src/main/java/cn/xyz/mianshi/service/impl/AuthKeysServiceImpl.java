package cn.xyz.mianshi.service.impl;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.AuthKeys;

/**
 * @author lidaye
 *
 */
@Service
public class AuthKeysServiceImpl extends MongoRepository<AuthKeys,Integer>{

	@Override
	public Datastore getDatastore() {
		// TODO Auto-generated method stub
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<AuthKeys> getEntityClass() {
		// TODO Auto-generated method stub
		return AuthKeys.class;
	}
	
	public void uploadPayKey(int userId,String publicKey,String privateKey) {
		AuthKeys userKeys = get(userId);
		if(null==userKeys) {
			userKeys=new AuthKeys();
			userKeys.setUserId(userId);
			userKeys.setPayPublicKey(publicKey);
			userKeys.setPayPrivateKey(privateKey);
			userKeys.setCreateTime(DateUtil.currentTimeSeconds());
			save(userKeys);
			return;
		}
		UpdateOperations<AuthKeys> operations = createUpdateOperations();
		operations.set("payPublicKey", publicKey);
		operations.set("payPrivateKey", privateKey);
		operations.set("modifyTime",DateUtil.currentTimeSeconds());
		updateAttributeByOps(userId, operations);
	}
	public String getPayPublicKey(int userId) {
		Object payPublicKey = queryOneFieldById("payPublicKey", userId);
		if(null==payPublicKey)
			return null;
		else return String.valueOf(payPublicKey);
	}
	public String getPayPrivateKey(int userId) {
		Object key = queryOneFieldById("payPrivateKey", userId);
		if(null==key)
			return null;
		else return String.valueOf(key);
	}
	
	public void uploadMsgKey(int userId,String publicKey,String privateKey) {
		AuthKeys userKeys = get(userId);
		if(null==userKeys) {
			userKeys=new AuthKeys();
			userKeys.setUserId(userId);
			userKeys.setMsgPublicKey(publicKey);
			userKeys.setMsgPrivateKey(privateKey);
			userKeys.setCreateTime(DateUtil.currentTimeSeconds());
			AuthKeys.PublicKey puKey=new AuthKeys.PublicKey();
			puKey.setKey(publicKey);
			puKey.setTime(DateUtil.currentTimeMilliSeconds());
			userKeys.getPublicKeyList().add(puKey);
			save(userKeys);
			return;
		}
		UpdateOperations<AuthKeys> operations = createUpdateOperations();
		if(!StringUtil.isEmpty(publicKey)) {
			AuthKeys.PublicKey puKey=new AuthKeys.PublicKey();
			puKey.setKey(publicKey);
			puKey.setTime(DateUtil.currentTimeMilliSeconds());
			userKeys.getPublicKeyList().add(puKey);
			operations.set("msgPublicKey", publicKey);
			operations.set("publicKeyList", userKeys.getPublicKeyList());
		}
		if(!StringUtil.isEmpty(privateKey)) {	
			operations.set("msgPrivateKey", privateKey);
		}
		
		updateAttributeByOps(userId, operations);
	}
	
	public String getMsgPublicKey(int userId) {
		Object payPublicKey = queryOneFieldById("msgPublicKey", userId);
		if(null==payPublicKey)
			return null;
		else return String.valueOf(payPublicKey);
	}
	
	

}
