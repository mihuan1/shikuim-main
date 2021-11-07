package cn.xyz.mianshi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.model.KSession;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.AddressBook;
import cn.xyz.mianshi.vo.Config;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;
@Service
public class AddressBookManagerImpl extends MongoRepository<AddressBook,ObjectId> {
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getLocalSpringBeanManager().getDatastore();
	}

	@Override
	public Class<AddressBook> getEntityClass() {
		return AddressBook.class;
	}
	private static Config getSystemConfig(){
		return SKBeanUtils.getSystemConfig();
	}
	private static FriendsManagerImpl getFriendsManager(){
		return SKBeanUtils.getFriendsManager();
	}
	
	public List<AddressBook> uploadTelephone(User user,String deleteStr,String uploadStr,String uploadJsonStr){
//		public List<AddressBook> uploadTelephone(KSession session,String deleteStr,String uploadStr){
		List<AddressBook> books = null;
//		String telephone=session.getTelephone();
		if(!StringUtil.isEmpty(deleteStr))
			deleteByStrs(user.getTelephone(), deleteStr);
		else if(!StringUtil.isEmpty(uploadStr))
			books =	uploadTelephone(user.getUserId(),user.getTelephone(), uploadStr);
		else if(!StringUtil.isEmpty(uploadJsonStr)){
			books =	uploadJsonTelephone(user.getUserId(),user.getTelephone(), uploadJsonStr);
		}
		return books;
	}
	
	/** @Description:（新版通讯录） 
	* @param userId
	* @param telephone
	* @param strs
	* @return
	**/ 
	private List<AddressBook> uploadJsonTelephone(Integer userId, String telephone, String uploadJsonStr ) {
		List<AddressBook> address = JSONObject.parseArray(uploadJsonStr, AddressBook.class);
		List<AddressBook> bookList = new ArrayList<AddressBook>();
		for(int i = 0; i < address.size(); i++){
			String repPhone = address.get(i).getToTelephone();
			String toTelephone = repPhone.replace(" ", "");
			toTelephone = toTelephone.replace("-", "");
			User user = null;
				if (0 < get(telephone, toTelephone))
					continue;// 不能让自己成为自己的通讯录好友
				if(toTelephone.equals(telephone))
					continue;
				user = SKBeanUtils.getUserManager().getUser(toTelephone);
				AddressBook saveBook = saveBook(telephone, toTelephone, user, userId, address.get(i).getToRemarkName());
				bookList.add(saveBook);
		}
		save(bookList);
		
		//logger.info("====>  导入完成后：   用户：  "+userId  +"  的 通讯录好友： "+JSONObject.toJSONString(bookList));
		return bookList.stream().
				filter(book -> 1==book.getRegisterEd())
				.collect(Collectors.toList());
	}
	
	/** @Description:（普通版通讯录） 
	* @param userId
	* @param telephone
	* @param strs
	* @return
	**/ 
	private List<AddressBook> uploadTelephone(Integer userId, String telephone, String strs) {
		strs = strs.replace(" ", "");
		strs = strs.replace("-", "");
		String[] array = strs.split(",");
		User user = null;
		List<AddressBook> bookList = new ArrayList<AddressBook>();
		for (String str : array) {
			if (0 < get(telephone, str))
				continue;// 不能让自己成为自己的通讯录好友
			user = SKBeanUtils.getUserManager().getUser(str);
			if(str.equals(telephone))
				continue;
			AddressBook saveBook = buildAddressBook(telephone, str, user, userId);
			bookList.add(saveBook);
			
		}
		save(bookList);
		return bookList.stream().
				filter(book -> 1==book.getRegisterEd())
				.collect(Collectors.toList());
	}

	private AddressBook saveBook(String telephone, String str, User user, Integer userId, String toRemark) {
		AddressBook book = null;
		book = new AddressBook();
		book.setTelephone(telephone);
		book.setToTelephone(str);
		book.setRegisterEd(null == user ? 0 : 1);
		book.setUserId(userId);
		book.setToUserId(null == user ? null : user.getUserId());
		book.setRegisterTime(null == user ? 0 : user.getCreateTime());
		book.setToUserName(null == user ? null : user.getNickname());
		book.setToRemarkName(toRemark);
		if(null != user){
			Friends friends = getFriendsManager().getFriends(userId, user.getUserId());
			if(null == friends && 0 == getSystemConfig().getIsAutoAddressBook())
				book.setStatus(0);
			else if(null != friends && 2 == friends.getStatus())
				book.setStatus(2);
			else if(0 == getSystemConfig().getIsAutoAddressBook()){// 不自动添加
				book.setStatus(0);
			}else if (1 == getSystemConfig().getIsAutoAddressBook()) {
				book.setStatus(1);
			}
		}else {// 没有注册im
			book.setStatus(0);
		}
		
		if(null != user && getSystemConfig().getIsAutoAddressBook()==1){
			ThreadUtil.executeInThread(new Callback() {
				
				@Override
				public void execute(Object obj) {
					Map<String, String> bookMap = Maps.newConcurrentMap();
					bookMap.put("toUserId", String.valueOf(user.getUserId()));
					bookMap.put("toRemark", toRemark);
					autofollowUser(userId, bookMap);
				}
			});
			
		}
		return book;
	}
	private AddressBook buildAddressBook(String telephone, String str, User user, Integer userId) {
		AddressBook book = new AddressBook();
		book.setTelephone(telephone);
		book.setToTelephone(str);
		book.setRegisterEd(user == null ? 0 : 1);
		book.setUserId(userId);
		book.setToUserId(user == null ? null : user.getUserId());
		book.setRegisterTime(user == null ? 0 : user.getCreateTime());
		book.setToUserName(user == null ? null : user.getNickname());
		return book;
	}
	private AddressBook saveBook(String telephone, String str, User user, Integer userId) {
		AddressBook book = null;
		book = new AddressBook();
		book.setTelephone(telephone);
		book.setToTelephone(str);
		book.setRegisterEd(user == null ? 0 : 1);
		book.setUserId(userId);
		book.setToUserId(user == null ? null : user.getUserId());
		book.setRegisterTime(user == null ? 0 : user.getCreateTime());
		book.setToUserName(user == null ? null : user.getNickname());
		save(book);
		return book;
	}
	
	/** @Description:（自动成为好友） 
	* @param toUserId
	* @param addressBook
	**/ 
	public void autofollowUser(Integer userId,Map<String, String> addressBook){
		SKBeanUtils.getFriendsManager().autofollowUser(userId, addressBook);
	}
	
	
	
	public void notifyBook(String telephone,Integer userId,String nickName,Long registerTime){
		System.out.println("注册时修改数据："+"telephone:"+telephone+"   toUserId:"+userId+"    nickName:"+nickName+"   registerTime:"+registerTime);
		DBCollection lastdbCollection=null;
		lastdbCollection = getDatastore().getDB().getCollection("AddressBook");
		BasicDBObject lastquery=new BasicDBObject();
		lastquery.put("registerEd", 0);
		lastquery.put("toTelephone", telephone);
		BasicDBObject values=new BasicDBObject();
		values.put("registerEd", 1);
		values.put("toUserName", nickName);
		values.put("registerTime", registerTime);
		values.put("toUserId", userId);
		values.put("status", 0 == getSystemConfig().getIsAutoAddressBook() ? 0 : 1);
		lastdbCollection.update(lastquery,new BasicDBObject(MongoOperator.SET, values) ,false,true);
		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
				notifyBook(telephone);
			}
			
		});
	}
	public void notifyBook(String telephone){
		System.out.println("推送使用的电话号码："+telephone);
		//a注册   a给b发xmpp通知他我们成为通讯录好友
		List<AddressBook> list = get(telephone);
		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
					for(AddressBook book :list){
						MessageBean messageBean=new MessageBean();
						messageBean.setType(KXMPPServiceImpl.registAddressBook);
						messageBean.setFromUserId(String.valueOf(book.getToUserId()));
						messageBean.setFromUserName(book.getToUserName());
						messageBean.setToUserId(String.valueOf(book.getUserId()));
						messageBean.setToUserName(SKBeanUtils.getUserManager().getNickName(book.getUserId()));
						messageBean.setContent(JSONObject.toJSON(book));
						messageBean.setMsgType(0);// 单聊消息
						messageBean.setMessageId(StringUtil.randomUUID());
						try {
							KXMPPServiceImpl.getInstance().send(messageBean);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
			}
		});
	}
	
	private void deleteByStrs(String telephone,String strs){
		strs=strs.replace(" ", "");
		strs=strs.replace("-", "");
		String[] deleteArray=strs.split(",");
		Query<AddressBook> query=createQuery();
		query.filter("telephone", telephone);
		query.filter("toTelephone", new BasicBSONObject(MongoOperator.IN, deleteArray));
		deleteByQuery(query);
	}
	
	public List<AddressBook> findRegisterList(KSession session,int pageIndex,int pageSize){
		Query<AddressBook> query=createQuery();
		query.filter("registerEd", 1);
		query.filter("telephone",session.getTelephone());
		List<AddressBook> list=query.offset(pageIndex*pageSize).limit(pageSize).asList();
		list.forEach(book ->{
			book.setToUserName(SKBeanUtils.getUserManager().getNickName(book.getToUserId()));
		});
		
		return list;
	}
	public List<AddressBook> get(String toTelephone){
		Query<AddressBook> query=createQuery();
		query.filter("toTelephone", toTelephone);

		return query.asList();
	}
	public long get(String telephone,String toTelephone){
		Query<AddressBook> query=createQuery();
		query.filter("telephone", telephone);
		query.filter("toTelephone", toTelephone);
		return query.countAll();
	}
	
	public boolean get(String telephone,Integer toUserId){
		Query<AddressBook> query=createQuery();
		query.filter("telephone", telephone);
		query.filter("toUserId", toUserId);
		return query.get()!=null;
	}
	
	public void delete(String telephone,String toTelephone,Integer userId){
		Query<AddressBook> query=createQuery();
		if(!StringUtil.isEmpty(telephone))
			query.filter("telephone", telephone);
		if(!StringUtil.isEmpty(telephone))
			query.filter("toTelephone", toTelephone);
		if(0 != userId)
			query.filter("userId", userId);
		deleteByQuery(query);
	}
	
	public List<AddressBook> getAll(String telephone,int pageIndex,int pageSize){
		Query<AddressBook> query=createQuery();
		query.filter("telephone", telephone);
		query.filter("registerEd", 1);// 注册im的通讯录的人
		return query.asList();
	}
	
	public void checkAddressBook(String toTelephone,Integer toUserId){
		DBObject q=new BasicDBObject("toTelephone", toTelephone);
		List<Object> list=distinct("telephone",q);
		BasicDBObject query=null;
		BasicDBObject obj=null;
		BasicDBObject value=null;
		
		
		for (Object telephone : list) {
			
			query=new BasicDBObject("telephone", telephone.toString());
			query.append("toTelephone", toTelephone);
			obj=(BasicDBObject) findOne(query);
			if(1==obj.getInt("registerEd"))
				continue;
			query.append("registerEd", 0);
			value=new BasicDBObject("registerEd", 1);
			long registerTime= DateUtil.currentTimeSeconds();
			value.append("registerTime",registerTime);
			value.append("toUserId", toUserId);
			
		}
		
	}
	//注销手机号码
	public void writeOffUser(String telephone){
		BasicDBObject value=new BasicDBObject("registerEd", 0);
		value.append("registerTime",0);
		updateAttributeSet("AddressBook", "toTelephone", telephone, value);
	}
	
	private void push(Integer receiver,Integer fromUserId,String telephone,long registerTime){
		JSONObject json=new JSONObject();
		//消息类型  审核专长
		//json.put("type", KConstants.MsgType.AddressBook);
		json.put("timeSend", DateUtil.currentTimeSeconds());
		json.put("messageId", UUID.randomUUID());
		
		long from=KConstants.SystemNo.AddressBook;
		json.put("objectId", fromUserId);
		
		json.put("fromUserId", from);
		json.put("fromUserName",ConstantUtil.getMsgByCode(from+"", "").getValue());
		JSONObject contentJson=new JSONObject();
		
		contentJson.put("registerTime", registerTime);
		contentJson.put("phone", telephone);
		json.put("content",contentJson.toJSONString());
		
		
	}
	
	// 获取通讯录好友
	public List<Integer> getAddressBookUserIds(Integer userId){
		Query<AddressBook> query = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId).retrievedFields(true, "toUserId");
		query.or(query.criteria("status").equal(1),query.criteria("status").equal(2));
		List<Integer> userIds = new ArrayList<Integer>();
		query.asList().forEach(addressBook ->{
			userIds.add(addressBook.getToUserId());
		});
		logger.info("用户 "+userId+" : 的通讯录好友:{}",JSONObject.toJSONString(userIds));
		return userIds;
	}

}
