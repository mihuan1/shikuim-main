package cn.xyz.mianshi.lable;

public interface UserLabelRepository {
    Object addLabel(UserLabel userLabel);//添加群标识码
    Object getUserLabel(Integer userId,String labelId);//获取群标识码列表
    Object getUserLabels(Integer userId);//获取群标识码列表
    Object queryUserLabel(Integer userId, String code);//查询是否群标识码
}
