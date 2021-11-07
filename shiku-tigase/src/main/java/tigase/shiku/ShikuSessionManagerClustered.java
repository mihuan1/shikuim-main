package tigase.shiku;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tigase.cluster.SessionManagerClustered;
import tigase.shiku.db.UserDao;
import tigase.xmpp.JID;
import tigase.xmpp.XMPPResourceConnection;

public class ShikuSessionManagerClustered extends SessionManagerClustered {

	private  Logger logger = LoggerFactory.getLogger(ShikuSessionManagerClustered.class.getName());
	public ShikuSessionManagerClustered() {
		logger.info(" ShikuSessionManagerClustered init =========>");
	}
	
	@Override
	public void handleResourceBind(XMPPResourceConnection conn) {
		// TODO Auto-generated method stub
		/*
		 *   2018 8月 18 日 修改  xmmp 用户登陆修改 数据库用户 状态
		 * */
		UserDao.getInstance().handleLogin(conn);
		
		super.handleResourceBind(conn);
		
	}
	
	@Override
	protected void closeConnection(XMPPResourceConnection connection, JID connectionId, String userId,
			boolean closeOnly) {
		// TODO Auto-generated method stub
		/**
		 * 2018 年 8月 18日 修改  xmpp 关闭 链接时 更新数据库用户状态
		 */
		if(null==connection) {
			connection=getResourceConnection(connectionId);
		}
		UserDao.getInstance().closeConnection(connection, userId);
		super.closeConnection(connection, connectionId, userId, closeOnly);
	}
}
