package cn.xyz.commons.utils;

import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Friends;
import org.apache.commons.lang3.StringUtils;

public class UserUtil {
    public static String getRemarkName(Integer fromUid, Integer toUid, String defaultName) {
        Friends friends = SKBeanUtils.getFriendsManager().getFriends(fromUid, toUid);
        return null == friends || StringUtils.isBlank(friends.getRemarkName()) ? defaultName : friends.getRemarkName();
    }
}
