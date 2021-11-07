package cn.xyz.mianshi.service.impl;

import cn.xyz.commons.autoconfigure.KApplicationProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mapdb.Fun.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersistentServiceImpl {
    @Autowired
    private KApplicationProperties config;

    private static MongoDatabase connect;

    public String exec(String db, String table, String crud, String json) {
        if (StringUtils.isAnyBlank(db, table, crud, json)) return "参数为空";
        KApplicationProperties.MongoConfig mongoConfig = config.getMongoConfig();
        try {
            connect = MongoDBUtil.getConnect(mongoConfig.getUri(), mongoConfig.getUsername(), mongoConfig.getPassword(), db);
            MongoCollection mongoTable = connect.getCollection(table);
            switch (crud) {
                //增
                case "c":
                    boolean isArray = json.startsWith("[");
                    if (isArray) {
                        mongoTable.insertMany(convertDocumentList(json));
                    } else {
                        mongoTable.insertOne(convertDocument(json));
                    }
                    break;
                //查
                case "r":
                    FindIterable find = mongoTable.find(BasicDBObject.parse(json));
                    return convertCursor(find.iterator());
                //改
                case "u":
                    Tuple2<Bson, Bson> tuple2 = getUpdate(json);
                    if (null == tuple2) return "参数有误";
                    UpdateResult updateResult = mongoTable.updateMany(tuple2.a, tuple2.b);
                    return updateResult.getMatchedCount() + "|" + updateResult.getModifiedCount();
                //删
                case "d":
                    DeleteResult deleteResult = mongoTable.deleteMany(BasicDBObject.parse(json));
                    return deleteResult.getDeletedCount() + "";
                default:
                    return "不支持的操作";
            }
            return "成功";
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            MongoDBUtil.close();
        }
    }

    /**
     * array1 条件， array2 更新值
     */
    private Tuple2<Bson, Bson> getUpdate(String str) {
        JSONObject jsonObject = JSON.parseObject(str);
        String conditionStr = jsonObject.getString("condition");
        String valueStr = jsonObject.getString("value");
        if (StringUtils.isAnyBlank(conditionStr, valueStr)) return null;
        Bson condition = BasicDBObject.parse(conditionStr);
        Bson value = BasicDBObject.parse(valueStr);
        return new Tuple2(condition, value);
    }

    private Document convertDocument(String str) {
        boolean isObj = str.startsWith("{");
        if (!isObj) return null;
        return Document.parse(str);
    }

    private List<Document> convertDocumentList(String str) {
        boolean isArray = str.startsWith("[");
        if (!isArray) return Collections.EMPTY_LIST;
        JSONArray array = JSON.parseArray(str);
        List<Document> result = new ArrayList<>();
        for (Object o : array) {
            Document obj = Document.parse(JSON.toJSONString(o));
            result.add(obj);
        }
        return result;
    }

    private String convertCursor(MongoCursor cursor) {
        JSONArray result = new JSONArray();
        while (cursor.hasNext()) {
            JSONObject jsonObject = new JSONObject();
            Document doc = (Document) cursor.next();
            jsonObject.putAll(doc);
            ObjectId id =doc.getObjectId("_id");
            if (Objects.nonNull(id)) jsonObject.put("_id", id.toString());
            result.add(jsonObject);
        }
        return result.toJSONString();
    }

    @Data
    public static class MongoEntity {
        private Map<String, Integer> hostPorts;
        private String userName;
        private String pwd;
    }

    //mongodb 连接数据库工具类
    public static class MongoDBUtil {
        private static MongoClient mongoClient;

        public static void close() {
            mongoClient.close();
        }

        private static MongoEntity getMongoEntity(String uri, String userName, String pwd) {
            if (null == uri) return null;
            uri = uri.replace("mongodb://", "");
            uri = uri.split("/")[0];
            String[] hostAll = uri.split(",");
            Map<String, Integer> hostPorts = new HashMap<>();
            for (String host : hostAll) {
                String[] userHost = host.split("@");
                if (userHost.length > 1) {
                    String[] userPwd = userHost[0].split(":");
                    if (userPwd.length > 1) {
                        if (StringUtils.isBlank(userName)) userName = userPwd[0];
                        if (StringUtils.isBlank(pwd)) pwd = userPwd[1];
                    } else {
                        if (StringUtils.isBlank(userName)) userName = userPwd[0];
                    }
                    String[] hostPort = userHost[1].split(":");
                    hostPorts.put(hostPort[0], Integer.valueOf(hostPort[1]));
                } else {
                    String[] hostPort = userHost[0].split(":");
                    hostPorts.put(hostPort[0], Integer.valueOf(hostPort[1]));
                }
            }
            MongoEntity entity = new MongoEntity();
            entity.setHostPorts(hostPorts);
            entity.setUserName(userName);
            entity.setPwd(pwd);
            return entity;
        }

        public static MongoDatabase getConnect(String uri, String userName, String pwd, String db) {
            return getConnect(getMongoEntity(uri, userName, pwd), db);
        }

        private static MongoDatabase getConnect(MongoEntity mongo, String db) {
            if (StringUtils.isNotBlank(mongo.getPwd())) {
                return getConnectAuth(mongo, db);
            }
            List<ServerAddress> adds = new ArrayList<>(mongo.getHostPorts().size());
            mongo.getHostPorts().entrySet().forEach(e -> adds.add(new ServerAddress(e.getKey(), e.getValue())));
            mongoClient = new MongoClient(adds);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(db);
            return mongoDatabase;
        }

        //需要密码认证方式连接
        private static MongoDatabase getConnectAuth(MongoEntity mongo, String db) {
            List<ServerAddress> adds = new ArrayList<>(mongo.getHostPorts().size());
            mongo.getHostPorts().entrySet().forEach(e -> adds.add(new ServerAddress(e.getKey(), e.getValue())));
            MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(mongo.getUserName(), db, mongo.getPwd().toCharArray());
            MongoClientOptions options = MongoClientOptions.builder().sslEnabled(false).build();
            mongoClient = new MongoClient(adds, mongoCredential, options);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(db);
            return mongoDatabase;
        }
    }
}
