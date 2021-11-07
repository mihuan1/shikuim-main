package cn.xyz.mianshi.vo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value = "FriendsterWebsite", noClassnameStored = true)
@Data
public class FriendsterWebsite {
    private @Id ObjectId id;

    //图标url
    private String icon;

    //链接
    private String url;

    //标题
    private String title;

    //时间
    private long time;
}
