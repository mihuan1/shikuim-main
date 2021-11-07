package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

//直播间
@Entity(value="LiveRoom",noClassnameStored=true)
public class LiveRoom {
	
	private @Id ObjectId roomId;
	
	@Indexed
	private Integer userId; //直播间创建者
	private String nickName;
	private String name;//直播间名称
	private String notice;//房间公告
	private String url;//直播间推流地址
	private String img;//房间封面
	private Integer numbers=0;//直播间人数
	
	private Integer status=0;//直播状态 1:开启直播 0:关闭直播
	
	private Integer currentState = 0;// 直播间当前状态 0：正常, 1：禁用 
	
	private long createTime=0;//创建时间

	private String jid;
	
	public LiveRoom() {
		
	}
	
	public LiveRoom(ObjectId roomId, String name, String notice, String url, Integer currentState) {
		this.roomId = roomId;
		this.name = name;
		this.notice = notice;
		this.url = url;
		this.currentState = currentState;
	}



	public ObjectId getRoomId() {
		return roomId;
	}

	public void setRoomId(ObjectId roomId) {
		this.roomId = roomId;
	}
	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public Integer getNumbers() {
		return numbers;
	}

	public void setNumbers(Integer numbers) {
		this.numbers = numbers;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public Integer getCurrentState() {
		return currentState;
	}

	public void setCurrentState(Integer currentState) {
		this.currentState = currentState;
	}

	//直播间的用户
	@Entity(value="LiveRoomMember",noClassnameStored=true)
	public static class LiveRoomMember {
		
		@Id
		private ObjectId id;
		@Indexed
		private ObjectId roomId;
		@Indexed
		private Integer userId;
		//private String name;
		private String nickName;
		private int type=3;//1为创建者 2为管理员 3为成员
		private int state=0;	//为了判断是否被禁言1为禁言，0为未禁言
		private int number;//送礼物的数量
		
		private long createTime;//加入时间
		private int online=1;//是否在线  1为在线0为退出
		
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		public int getState() {
			return state;
		}
		public void setState(int state) {
			this.state = state;
		}
		public int getOnline() {
			return online;
		}
		public void setOnline(int online) {
			this.online = online;
		}
		public int getNumber() {
			return number;
		}
		public void setNumber(int number) {
			this.number = number;
		}
		public ObjectId getId() {
			return id;
		}
		public void setId(ObjectId id) {
			this.id = id;
		}
		public ObjectId getRoomId() {
			return roomId;
		}
		public void setRoomId(ObjectId roomId) {
			this.roomId = roomId;
		}
		public Integer getUserId() {
			return userId;
		}
		public void setUserId(Integer userId) {
			this.userId = userId;
		}
		public String getNickName() {
			return nickName;
		}
		public void setNickName(String nickName) {
			this.nickName = nickName;
		}
		public long getCreateTime() {
			return createTime;
		}
		public void setCreateTime(long createTime) {
			this.createTime = createTime;
		}
		
		

	}
}
