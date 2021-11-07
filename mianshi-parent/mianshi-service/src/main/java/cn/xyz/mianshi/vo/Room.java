package cn.xyz.mianshi.vo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;

import com.alibaba.fastjson.annotation.JSONField;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.utils.DateUtil;

@Entity(value = "shiku_room", noClassnameStored = true)
@Indexes({@Index("userId"), @Index("jid"), @Index("userId,jid")})
public class Room {

    // 房间编号
    @Id
    private ObjectId id;

    private String jid; //群的id
    // 房间名称
    private String name;
    // 房间描述
    private String desc;
    // 房间主题
    private String subject;
    // 房间分类
    private Integer category;
    // 房间标签
    private List<String> tags;
    //语音通话标识符
    private String call;
    //视频会议标识符
    private String videoMeetingNo;

    // 房间公告
    private Notice notice;
    // 公告列表
    private List<Notice> notices;

    // 当前成员数
    private Integer userSize;
    // 最大成员数
    private Integer maxUserSize = 1000;
    // 自己
    private Member member;
    // 成员列表
    private List<Member> members;

    private Integer countryId;// 国家Id
    private Integer provinceId;// 省份Id
    private Integer cityId;// 城市Id
    private Integer areaId;// 地区Id

    private Double longitude;// 经度
    private Double latitude;// 纬度

    // 创建者Id
    private Integer userId;
    // 创建者昵称
    private String nickname;

    // 创建时间
    private Long createTime;
    // 修改人
    private Integer modifier;
    // 修改时间
    private Long modifyTime;

    private Integer s;// 状态  1:正常, -1:被禁用

    private Integer isLook = 1;// 是否可见   0为可见   1为不可见

    // 群主设置 群内消息是否发送已读 回执 显示数量  0 ：不发生 1：发送
    private int showRead = 0;

    private Integer isNeedVerify = 0; // 加群是否需要通过验证  0：不要   1：要

    private int showMember = 1;// 显示群成员给 普通用户   1 显示  0  不显示

    private int allowSendCard = 1;// 允许发送名片 好友  1 允许  0  不允许

    private int allowHostUpdate = 1;// 是否允许群主修改 群属性

    private int allowInviteFriend = 1;// 允许普通成员邀请好友  默认 允许

    private int allowUploadFile = 1;// 允许群成员上传群共享文件

    private int allowConference = 1;// 允许成员 召开会议

    private int allowSpeakCourse = 1;// 允许群成员 开启 讲课

    private int isAttritionNotice = 1;// 群组减员发送通知  0:关闭 ，1：开启

    // 大于当前时间时禁止发言
    private long talkTime;

    // -1.0永久保存    1.0保存一天   365.0保存一年
    private double chatRecordTimeOut = -1;


    private String labelName;

    private String promotionUrl;// 推广链接

    private String redPacketLock = "-1";

    public String getRedPacketLock() {
        return redPacketLock;
    }

    public void setRedPacketLock(String redPacketLock) {
        this.redPacketLock = redPacketLock;
    }

    public int getShowMember() {
        return showMember;
    }

    public void setShowMember(int showMember) {
        this.showMember = showMember;
    }

    public int getAllowSendCard() {
        return allowSendCard;
    }

    public void setAllowSendCard(int allowSendCard) {
        this.allowSendCard = allowSendCard;
    }

    public int getAllowHostUpdate() {
        return allowHostUpdate;
    }

    public void setAllowHostUpdate(int allowHostUpdate) {
        this.allowHostUpdate = allowHostUpdate;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getCall() {
        return call;
    }

    public void setCall(String call) {
        this.call = call;
    }

    public String getVideoMeetingNo() {
        return videoMeetingNo;
    }

    public void setVideoMeetingNo(String videoMeetingNo) {
        this.videoMeetingNo = videoMeetingNo;
    }

    public Notice getNotice() {
        return notice;
    }

    public void setNotice(Notice notice) {
        this.notice = notice;
    }

    public List<Notice> getNotices() {
        return notices;
    }

    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }

    public Integer getUserSize() {
        return userSize;
    }

    public void setUserSize(Integer userSize) {
        this.userSize = userSize;
    }

    public Integer getMaxUserSize() {
        return maxUserSize;
    }

    public void setMaxUserSize(Integer maxUserSize) {
        this.maxUserSize = maxUserSize;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public synchronized void addMember(Member member) {
        if (null == members || 0 == members.size()) {
            members = new ArrayList<>();
            members.add(member);
        } else {

            boolean contains = false;
            for (Member mem : members) {
                if (mem.userId.equals(member.getUserId())) {
                    contains = true;
                    break;
                }
            }
            if (!contains)
                members.add(member);

        }


    }

    public synchronized void removeMember(Member member) {
        if (null != members || 0 < members.size()) {
            members.remove(member);
        }


    }

    public synchronized void removeMember(int userId) {
        if (null != members || 0 < members.size()) {
            Member member = null;
            for (Member mem : members) {
                if (mem.userId.equals(userId)) {
                    member = mem;
                    break;
                }
            }
            if (null != member)
                members.remove(member);
        }


    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Integer getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getModifier() {
        return modifier;
    }

    public void setModifier(Integer modifier) {
        this.modifier = modifier;
    }

    public Long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Integer getS() {
        return s;
    }

    public void setS(Integer s) {
        this.s = s;
    }

    public int getShowRead() {
        return showRead;
    }

    public void setShowRead(int showRead) {
        this.showRead = showRead;
    }

    public Integer getIsLook() {
        return isLook;
    }

    public void setIsLook(Integer isLook) {
        this.isLook = isLook;
    }

    public Integer getIsNeedVerify() {
        return isNeedVerify;
    }

    public void setIsNeedVerify(Integer isNeedVerify) {
        this.isNeedVerify = isNeedVerify;
    }


    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }


    public double getChatRecordTimeOut() {
        return chatRecordTimeOut;
    }

    public void setChatRecordTimeOut(double chatRecordTimeOut) {
        this.chatRecordTimeOut = chatRecordTimeOut;
    }


    public int getAllowInviteFriend() {
        return allowInviteFriend;
    }

    public void setAllowInviteFriend(int allowInviteFriend) {
        this.allowInviteFriend = allowInviteFriend;
    }


    public int getAllowUploadFile() {
        return allowUploadFile;
    }

    public void setAllowUploadFile(int allowUploadFile) {
        this.allowUploadFile = allowUploadFile;
    }


    public int getAllowConference() {
        return allowConference;
    }

    public void setAllowConference(int allowConference) {
        this.allowConference = allowConference;
    }


    public int getAllowSpeakCourse() {
        return allowSpeakCourse;
    }

    public void setAllowSpeakCourse(int allowSpeakCourse) {
        this.allowSpeakCourse = allowSpeakCourse;
    }


    public long getTalkTime() {
        return talkTime;
    }

    public void setTalkTime(long talkTime) {
        this.talkTime = talkTime;
    }


    public int getIsAttritionNotice() {
        return isAttritionNotice;
    }

    public void setIsAttritionNotice(int isAttritionNotice) {
        this.isAttritionNotice = isAttritionNotice;
    }


    public String getPromotionUrl() {
        return promotionUrl;
    }

    public void setPromotionUrl(String promotionUrl) {
        this.promotionUrl = promotionUrl;
    }


    @Entity(value = "shiku_room_notice", noClassnameStored = true)
    @Indexes({@Index("roomId")})
    public static class Notice {
        @Id
        private ObjectId id;// 通知Id

        private ObjectId roomId;// 房间Id
        private String text;// 通知文本
        private Integer userId;// 用户Id
        private String nickname;// 用户昵称
        private long time;// 时间
        private long modifyTime;// 修改时间

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

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        /**
         * @return the modifyTime
         */
        public long getModifyTime() {
            return modifyTime;
        }

        /**
         * @param modifyTime the modifyTime to set
         */
        public void setModifyTime(long modifyTime) {
            this.modifyTime = modifyTime;
        }

        public Notice() {

        }

        public Notice(ObjectId id, ObjectId roomId, String text, Integer userId, String nickname) {
            this.id = id;
            this.roomId = roomId;
            this.text = text;
            this.userId = userId;
            this.nickname = nickname;
            this.time = DateUtil.currentTimeSeconds();
        }

    }

    @Entity(value = "shiku_room_member", noClassnameStored = true)
    @Indexes({@Index("roomId"), @Index("userId"), @Index("roomId,userId"), @Index("userId,role")})
    public static class Member {
        @Id
        @JSONField(serialize = false)
        private ObjectId id;

        // 房间Id
        @JSONField(serialize = false)
        private ObjectId roomId;

        // 成员Id
        private Integer userId;

        // 成员昵称
        private String nickname;

        //群主 备注 成员名称
        private String remarkName;

        // 成员角色：1=创建者、2=管理员、3=普通成员、4=隐身人、5=监控人
        private int role;

        // 订阅群信息：0=否、1=是
        private Integer sub;

        //语音通话标识符
        private String call;

        //视频会议标识符
        private String videoMeetingNo;

        //消息免打扰（1=是；0=否）
        private Integer offlineNoPushMsg = 0;

        // 大于当前时间时禁止发言
        private Long talkTime;

        // 最后一次互动时间
        private Long active;

        // 创建时间
        private Long createTime;

        // 修改时间
        private Long modifyTime;

        // 是否开启置顶聊天 0：关闭，1：开启
        private byte isOpenTopChat = 0;

        // 开启置顶聊天时间
        private long openTopChatTime = 0;

        public ObjectId getId() {
            return id;
        }

        public String getCall() {
            return call;
        }

        public void setCall(String call) {
            this.call = call;
        }

        public String getVideoMeetingNo() {
            return videoMeetingNo;
        }

        public void setVideoMeetingNo(String videoMeetingNo) {
            this.videoMeetingNo = videoMeetingNo;
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

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public int getRole() {
            return role;
        }

        public void setRole(int role) {
            this.role = role;
        }

        public Integer getOfflineNoPushMsg() {
            return offlineNoPushMsg;
        }

        public void setOfflineNoPushMsg(Integer offlineNoPushMsg) {
            this.offlineNoPushMsg = offlineNoPushMsg;
        }

        public Integer getSub() {
            return sub;
        }

        public void setSub(Integer sub) {
            this.sub = sub;
        }

        public Long getTalkTime() {
            return talkTime;
        }

        public void setTalkTime(Long talkTime) {
            this.talkTime = talkTime;
        }

        public Long getActive() {
            return active;
        }

        public void setActive(Long active) {
            this.active = active;
        }

        public Long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Long createTime) {
            this.createTime = createTime;
        }

        public Long getModifyTime() {
            return modifyTime;
        }

        public void setModifyTime(Long modifyTime) {
            this.modifyTime = modifyTime;
        }

        public String getRemarkName() {
            return remarkName;
        }

        public void setRemarkName(String remarkName) {
            this.remarkName = remarkName;
        }

        public byte getIsOpenTopChat() {
            return isOpenTopChat;
        }

        public void setIsOpenTopChat(byte isOpenTopChat) {
            this.isOpenTopChat = isOpenTopChat;
        }

        public long getOpenTopChatTime() {
            return openTopChatTime;
        }

        public void setOpenTopChatTime(long openTopChatTime) {
            this.openTopChatTime = openTopChatTime;
        }

        public Member(ObjectId roomId, Integer userId, String nickname) {
            this.active = DateUtil.currentTimeSeconds();
            this.roomId = roomId;
            this.userId = userId;
            this.nickname = nickname;
            this.role = KConstants.Room_Role.MEMBER;
            this.sub = 1;
            this.talkTime = 0L;
            this.createTime = this.active;
            this.modifyTime = this.active;
        }

        public Member() {

        }

    }

    @Entity(value = "shiku_room_share", noClassnameStored = true)
    public static class Share {


        private @Id
        ObjectId shareId;//id
        private @Indexed
        ObjectId roomId;
        private String name;//文件名称
        private String url;//文件路径
        private long time;//发送时间
        private @Indexed
        Integer userId;//发消息的用户id
        private String nickname;//昵称
        private int type;//文件类型()
        private float size;//文件大小

        public Share() {
        }

        public Share(ObjectId shareId, ObjectId roomId, String name, String url, long time, Integer userId,
                     String nickname, int type, float size) {
            this.shareId = shareId;
            this.roomId = roomId;
            this.name = name;
            this.url = url;
            this.time = time;
            this.userId = userId;
            this.nickname = nickname;
            this.type = type;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ObjectId getShareId() {
            return shareId;
        }

        public void setShareId(ObjectId shareId) {
            this.shareId = shareId;
        }

        public ObjectId getRoomId() {
            return roomId;
        }

        public void setRoomId(ObjectId roomId) {
            this.roomId = roomId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public Integer getUserId() {
            return userId;
        }


        public void setUserId(Integer userId) {
            this.userId = userId;
        }


        public String getNickname() {
            return nickname;
        }


        public void setNickname(String nickname) {
            this.nickname = nickname;
        }


        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public float getSize() {
            return size;
        }

        public void setSize(float size) {
            this.size = size;
        }

    }
}
