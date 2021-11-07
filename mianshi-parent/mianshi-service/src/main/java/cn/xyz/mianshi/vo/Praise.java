package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import cn.xyz.commons.utils.JSONUtil;

@Entity(value = "s_praise", noClassnameStored = true)
public class Praise {
	private @Indexed ObjectId msgId;// 赞所属消息Id
	private String nickname;// 赞用户昵称
	private @Id ObjectId praiseId;// 赞Id
	private long time;// 赞时间
	private int userId;// 赞用户Id

	public Praise() {
		super();
	}

	public Praise(ObjectId praiseId, ObjectId msgId, int userId,
			String nickname, long time) {
		super();
		this.praiseId = praiseId;
		this.msgId = msgId;
		this.userId = userId;
		this.nickname = nickname;
		this.time = time;
	}

	public ObjectId getMsgId() {
		return msgId;
	}

	public String getNickname() {
		return nickname;
	}

	public ObjectId getPraiseId() {
		return praiseId;
	}

	public long getTime() {
		return time;
	}

	public int getUserId() {
		return userId;
	}

	public void setMsgId(ObjectId msgId) {
		this.msgId = msgId;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setPraiseId(ObjectId praiseId) {
		this.praiseId = praiseId;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return JSONUtil.toJSONString(this);
	}

}
