package cn.xyz.mianshi.lable;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;


/**
 *     群标识码
 */
@Entity(value = "label", noClassnameStored = true)
@Indexes({ @Index("userId") })
public class Label {
    //群标识码ID
    private @Id ObjectId id;
    //创建人
    private Integer  userId;

    //标识码（系统自动生成）
    @Indexed(unique=true)
    private String  code;
    //群标识名称
    @Indexed(unique=true)
    private String  name;
    //存放图片文件地址
    private String  logo;
    //备注
    private String  mark;


    public Label(){ }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


}
