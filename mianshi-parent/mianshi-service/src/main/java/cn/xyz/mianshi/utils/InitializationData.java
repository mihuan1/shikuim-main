package cn.xyz.mianshi.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBCollection;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.vo.ErrorMessage;
import cn.xyz.mianshi.vo.Role;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.KXMPPServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * @version:（1.0）
 * @ClassName InitializationData
 * @Description: （初始化数据）
 * @author: wcl
 * @date:2018年8月25日下午4:07:23
 */
@Component
@Slf4j
public class InitializationData implements CommandLineRunner {


    @Value("classpath:data/message.json")
    private Resource resource;

    public Datastore getDatastore() {
        return SKBeanUtils.getDatastore();
    }


    @Override
    public void run(String... args) throws Exception {

        if (1 == SKBeanUtils.getLocalSpringBeanManager().getAppConfig().getOpenClearAdminToken())
            //启动时清空 redis 里的
            SKBeanUtils.getRedisCRUD().deleteKeysByPattern("adminToken:*");

        initSuperAdminData();

        initErrorMassageData();

    }


    /**
     * 初始化异常信息数据
     *
     * @throws Exception
     */
    private void initErrorMassageData() throws Exception {
        if (null == resource) {
            System.out.println("error initErrorMassageData  resource is null");
            return;
        }
        //DBCollection errMsgCollection = getDatastore().getCollection(ErrorMessage.class);

        BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        StringBuilder message = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            message.append(line);
        }
        String defaultString = message.toString();
        if (!StringUtil.isEmpty(defaultString)) {
            List<ErrorMessage> errorMessages = JSONObject.parseArray(defaultString, ErrorMessage.class);
            errorMessages.stream().filter(msg -> !StringUtil.isEmpty(msg.getCode())).forEach(errorMessage -> {
                Query<ErrorMessage> query = getDatastore().createQuery(ErrorMessage.class);
                query.filter("code", errorMessage.getCode());
                if (0 == getDatastore().getCount(query)) {
                    log.info("insert error msg {}", errorMessage);
                    getDatastore().save(errorMessage);
                }
            });

        }
        log.info(">>>>>>>>>>>>>>> 异常信息数据初始化完成  <<<<<<<<<<<<<");
        ConstantUtil.initMsgMap();
    }

    /**
     * 初始化默认超级管理员数据
     */
    private void initSuperAdminData() {

        DBCollection adminCollection = getDatastore().getCollection(Role.class);
        if (adminCollection == null || adminCollection.count() == 0) {
            try {
                User user = new User();
                user.setUserId(1000);
                user.setNickname("1000");
                user.setTelephone("861000");
                user.setPhone("1000");
                user.setUserKey(DigestUtils.md5Hex("861000"));
                user.setPassword(DigestUtils.md5Hex("1000"));
                user.setCreateTime(DateUtil.currentTimeSeconds());
                getDatastore().save(user);
                KXMPPServiceImpl.getInstance().registerAndXmppVersion(user.getUserId() + "", user.getPassword());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Role role = new Role(1000, "1000", (byte) 6, (byte) 1, 0);
            getDatastore().save(role);

            // 初始化10000号
            try {
                SKBeanUtils.getUserManager().addUser(10000, "10000");
                KXMPPServiceImpl.getInstance().registerSystemNo("10000", DigestUtils.md5Hex("10000"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("\n" + ">>>>>>>>>>>>>>> 默认管理员数据初始化完成  <<<<<<<<<<<<<");
        }

        Query<User> query = getDatastore().createQuery(User.class);
        query.field("_id").equal(1100);
        if (query.get() == null) {
            // 初始化1100号 作为金钱相关通知系统号码
            try {
                SKBeanUtils.getUserManager().addUser(1100, "1100");
                KXMPPServiceImpl.getInstance().registerSystemNo("1100", DigestUtils.md5Hex("1100"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("\n" + ">>>>>>>>>>>>>>> 默认系统通知数据初始化完成  <<<<<<<<<<<<<");
        }
    }


}
