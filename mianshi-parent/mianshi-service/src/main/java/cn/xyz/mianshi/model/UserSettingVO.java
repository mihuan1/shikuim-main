package cn.xyz.mianshi.model;

import cn.xyz.mianshi.vo.User.UserSettings;
import lombok.Data;
@Data
public class UserSettingVO extends UserSettings{

	private int allowAtt=1;// 允许关注
	private int allowGreet=1;// 允许打招呼
	private int friendsVerify=-1;// 加好友需验证 1：开启    0：关闭
	private int openService=-1;//是否开启客服模式 1：开启    0：关闭
	private int isVibration=-1;// 是否振动   1：开启    0：关闭
	private int isTyping=-1;// 让对方知道我正在输入   1：开启       0：关闭
	private int isUseGoogleMap=-1;// 使用google地图    1：开启   0：关闭
	private int isEncrypt=-1;// 是否开启加密传输    1:开启    0:关闭
	private int multipleDevices=1;// 是否开启多点登录   1:开启     0:关闭
	private int closeTelephoneFind=0;// 关闭手机号搜索用户    关闭次选项 不用使用手机号搜索用户   0 开启    1 关闭    默认开启
	
	//聊天记录 销毁  时间   -1 0  永久   1 一天
	private String chatRecordTimeOut="0";
	
	private double chatSyncTimeLen=0;//  聊天记录 最大 漫游时长    -1 永久  -2 不同步
	
	private Integer isKeepalive = -1;// 是否安卓后台常驻保活app 0：取消保活  1：保活
	
	private Integer showLastLoginTime = 0;// 显示上次上线时间   -1 所有人不显示 1所有人显示  2 所有好友显示   3 手机联系人显示
	
	private Integer showTelephone = 0;// 显示我的手机号码   -1 所有人不显示 1所有人显示  2 所有好友显示   3 手机联系人显示
	
	private Integer phoneSearch = -1;// 允许手机号搜索 1 允许 0 不允许
	
	private Integer nameSearch = -1;// 允许昵称搜索  1 允许 0 不允许
	
	private String friendFromList;// 通过什么方式添加我  0:系统添加好友 1:二维码 2：名片 3：群组 4： 手机号搜索 5： 昵称搜索
}
