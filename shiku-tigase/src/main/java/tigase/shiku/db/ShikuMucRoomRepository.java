package tigase.shiku.db;

import tigase.db.Repository;

public interface ShikuMucRoomRepository extends Repository {

	/**
	 * 删除房间
	 * 
	 * @param roomId
	 *            房间Id
	 */
	void delete(String roomId);

}
