package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Data;


@Data
@Entity(value="musicInfo",noClassnameStored=true)
public class MusicInfo {
	
	@Id
	private ObjectId id;
	public String cover; // 封面图地址
    public long length; // 音乐长度
    public String name; // 音乐名称
    public String nikeName; // 创作人
    public String path; // 音乐地址
    
    private int useCount;//使用 数量
}
