package tigase.shiku.db;

import java.util.Map;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import tigase.db.DBInitException;

public class MongoShikuMucRoomRepository implements ShikuMucRoomRepository {

	//private static final String ROOM_COLLECTION = "shiku_room";
	//private static final String ROOM_HIS_COLLECTION = "shiku_room_his";

	private DB db;
	private MongoClient mongo;

	@Override
	public void delete(String roomId) {
		//DBCollection dbCollection = db.getCollection(ROOM_COLLECTION);
	}

	@Override
	public void initRepository(String resource_uri, Map<String, String> params)
			throws DBInitException {
		try {
			MongoClientURI uri = new MongoClientURI(resource_uri);
			mongo = new MongoClient(uri);
			db = mongo.getDB(uri.getDatabase());

			/*DBCollection dbCollection = !db.collectionExists(ROOM_COLLECTION) ? db
					.createCollection(ROOM_COLLECTION, new BasicDBObject())
					: db.getCollection(ROOM_COLLECTION);
			dbCollection.createIndex(new BasicDBObject("room_jid_id", 1));
			dbCollection.createIndex(new BasicDBObject("room_jid_id", 1)
					.append("ts", 1));*/

			/*dbCollection = !db.collectionExists(ROOM_HIS_COLLECTION) ? db
					.createCollection(ROOM_HIS_COLLECTION, new BasicDBObject())
					: db.getCollection(ROOM_HIS_COLLECTION);
			dbCollection.createIndex(new BasicDBObject("sender", 1));
			dbCollection.createIndex(new BasicDBObject("receiver", 1));
			dbCollection.createIndex(new BasicDBObject("sender", 1).append(
					"receiver", 1));
			dbCollection.createIndex(new BasicDBObject("sender", 1).append(
					"receiver", 1).append("ts", 1));*/

		} catch (Exception ex) {
			throw new DBInitException(
					"Could not connect to MongoDB server using URI = "
							+ resource_uri, ex);
		}
	}

}
