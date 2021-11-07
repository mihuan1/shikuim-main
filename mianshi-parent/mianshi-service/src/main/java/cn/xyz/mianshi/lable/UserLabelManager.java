package cn.xyz.mianshi.lable;

import org.springframework.stereotype.Service;

@Service
public interface UserLabelManager {
    Object addLabel(Integer userId,String labelId,String name,String logo,String code,long date);
    Object getUserLabels(Integer userId);
    UserLabel queryUserLabel(Integer userId,String labelId);
    UserLabel queryUserLabelByCode(Integer userId, String code);
}
