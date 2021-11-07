package cn.xyz.mianshi.lable;

import java.util.List;

import org.bson.types.ObjectId;

public interface LabelManager {

    Label createLabel(Integer userId);
    Label createLabelByParams(Integer userId,String logo,String name);
    Label getLabel(ObjectId labelId);
    Label getLabelByCode(String code);
    List<Label> getLabelList(Integer userId);
    Label updateLabel(Label label);
    Object saveLabel(ObjectId id,String logo,String name);
    Label queryLabelByName(String name);
}
