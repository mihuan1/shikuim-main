package cn.xyz.mianshi.lable;

import java.util.List;

import org.bson.types.ObjectId;

/**
* @Description: TODO(用一句话描述该文件做什么)
* @author lidaye
* @date 2018年7月24日 
*/
public interface LabelRepository {

	/**
	* @Description: TODO(这里用一句话描述这个方法的作用)
	* @param @param label
	* @param @return    参数
	*/
	Object createLabel(Label label);

	/**
	* @Description: TODO(这里用一句话描述这个方法的作用)
	* @param @param userId
	* @param @return    参数
	*/
	List<Label> getLabelList(Integer userId);

	/**
	* @Description: TODO(这里用一句话描述这个方法的作用)
	* @param @param labelId
	* @param @return    参数
	*/
	Label getLabel(ObjectId labelId);

	/**
	* @Description: TODO(这里用一句话描述这个方法的作用)
	* @param @param code
	* @param @return    参数
	*/
	Label getLabelByCode(String code);

	/**
	* @Description: TODO(这里用一句话描述这个方法的作用)
	* @param @param label
	* @param @return    参数
	*/
	Label updateLabel(Label label);

	/**
	* @Description: TODO(这里用一句话描述这个方法的作用)
	* @param @param id
	* @param @param logo
	* @param @param name
	* @param @return    参数
	*/
	Object saveLabel(ObjectId id, String logo, String name);

	/**
	* @Description: TODO(这里用一句话描述这个方法的作用)
	* @param @param name
	* @param @return    参数
	*/
	Label queryLabelByName(String name);

}

