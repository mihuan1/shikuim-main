package tigase.shiku.conf;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;

import tigase.conf.ConfigurationException;
import tigase.conf.ConfiguratorAbstract;
import tigase.mongodb.muc.MongoHistoryProvider;
import tigase.osgi.ModulesManagerImpl;
import tigase.server.MessageRouter;
import tigase.server.MessageRouterConfig;
import tigase.server.XMPPServer;
import tigase.shiku.ShikuAutoReplyPlugin;
import tigase.shiku.ShikuMessageArchivePlugin;
import tigase.shiku.ShikuOfflineMsgPlugin;
import tigase.shiku.db.RedisService;
import tigase.shiku.db.UserDao;
import tigase.vhosts.VHostManagerIfc;

public class ShikuMessageRouter extends MessageRouter {
	
	private static final Logger log = LoggerFactory.getLogger(ShikuMessageRouter.class.getName());
	
	@Override
	public Map<String, Object> getDefaults(Map<String, Object> params) {
		Map<String, Object> defs = super.getDefaults(params);
		if(null!=params.get("--"+ShikuConfigBean.APIDBURI_KEY)){
			String apidb=(String) params.get("--"+ShikuConfigBean.APIDBURI_KEY);
			System.setProperty(ShikuConfigBean.APIDBURI_KEY, apidb);
			ShikuConfigBean.APIDBURI_VAL=apidb;
		}
		DBObject obj=UserDao.getInstance().getConfig();
		if(null!=obj) {
			Integer xmppTimeout=(int) obj.get("XMPPTimeout");
			
			Integer delay=(int)(xmppTimeout/2);
			Long inactivityTime=(long)(delay*10);
			
			xmppTimeout=xmppTimeout*1000;
			delay=delay*1000;
			params.put("--watchdog_timeout", xmppTimeout.toString());
			params.put("watchdog_timeout",  xmppTimeout.toString());
			params.put("--watchdog_delay", delay.toString());
			
			params.put("shikuSaveMsg", obj.get("isSaveMsg"));
			params.put("shikuSaveMucMsg", obj.get("isSaveMucMsg"));
			params.put("shikuMsgSendTime", obj.get("isMsgSendTime"));
			
			params.put("confirm-open-keyword", obj.get("isKeyWord"));
			
			// TODO Auto-generated method stub
			ConfiguratorAbstract configurator = XMPPServer.getConfigurator();
			try {
				
				Map<String, Object> props = configurator.getProperties("c2s");
				
				props.put("max-inactivity-time", inactivityTime);
				props.put("processors/urn:xmpp:sm:3/resumption-timeout", delay/1000);
				configurator.putProperties("c2s", props);
				Map<String, Object> sessman = configurator.getProperties("sess-man");
				String callbackhandler=null;
				if(null!=sessman.get("plugins-conf/urn:ietf:params:xml:ns:xmpp-sasl/callbackhandler-PLAIN")) {
					callbackhandler=sessman.get("plugins-conf/urn:ietf:params:xml:ns:xmpp-sasl/callbackhandler-PLAIN").toString();
				}else if(null!=sessman.get("plugins-conf/urn:ietf:params:xml:ns:xmpp-sasl/callbackhandler-PLAIN")){
					callbackhandler=sessman.get("plugins-conf/urn:ietf:params:xml:ns:xmpp-sasl/callbackhandler").toString();
				}
				if(null!=callbackhandler&&"tigase.shiku.RedisCallbackHandler".equals(callbackhandler)) {
					ShikuConfigBean.OPEN_REDIS_AUTH=true;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		
		
		defs.put("sess-man", "tigase.shiku.ShikuSessionManager");
		defs.put(MessageRouterConfig.MSG_RECEIVERS_PROP_KEY+"sess-man"+".class", "tigase.shiku.ShikuSessionManager");
		
		boolean cluster_mode = isTrue((String) params.get(CLUSTER_MODE));

		log.info("Cluster mode: {0}", params.get(CLUSTER_MODE));
		if (cluster_mode) {
			log.info("Cluster mode is on, replacing known components with cluster" +
					" versions:");
			defs.put("sess-man", "tigase.shiku.ShikuSessionManagerClustered");
			defs.put(MessageRouterConfig.MSG_RECEIVERS_PROP_KEY+"sess-man"+".class", "tigase.shiku.ShikuSessionManagerClustered");
		} else {
			log.info("Cluster mode is off.");
		}
		try {
			Class<?> loadClass = Class.forName("com.tigase.TigaseUtilsFilter");
			if(null!=loadClass) {
				defs.put("incoming-filters","com.tigase.TigaseUtilsFilter");
			}
		} catch (Exception e) {
			
		}
		initOldProperty(params);
		
		ShikuConfigBean.initConfig();
		UserDao.getInstance().getProducer();
		if(ShikuConfigBean.OPEN_REDIS_AUTH) {
			RedisService.getInstance().init(ShikuConfigBean.REDIS_URI_VAL, ShikuConfigBean.REDIS_PASSWORD_VAL,
					ShikuConfigBean.REDIS_DATABASE_VAL, ShikuConfigBean.REDIS_ISCLUSTER_VAL);
		}
		
		return defs;
	}
	@Override
	public void setProperties(Map<String, Object> props) throws ConfigurationException {
		// TODO Auto-generated method stub
		/**
		 * 
		 */
		ModulesManagerImpl.getInstance().registerPluginClass(ShikuMessageArchivePlugin.class);
		ModulesManagerImpl.getInstance().registerPluginClass(ShikuAutoReplyPlugin.class);
		ModulesManagerImpl.getInstance().registerPluginClass(ShikuOfflineMsgPlugin.class);
		
		
		ModulesManagerImpl.getInstance().registerClass(MongoHistoryProvider.class);
		
		
		super.setProperties(props);
		ShikuConfigBean.VIRTHOSTS=getDefHostName().getDomain();
	}
	
	@Override
	public void setVHostManager(VHostManagerIfc manager) {
		// TODO Auto-generated method stub
		super.setVHostManager(manager);
		
		
	}
	
	private void initOldProperty(Map<String, Object> params) {
		if(null!=params.get(ShikuConfigBean.APIDBURI_KEY)){
			String apidb=(String) params.get(ShikuConfigBean.APIDBURI_KEY);
			System.setProperty(ShikuConfigBean.APIDBURI_KEY, apidb);
		}
		if(null!=params.get(ShikuConfigBean.USER_DB_URI_KEY)){
			String userdb=(String) params.get(ShikuConfigBean.USER_DB_URI_KEY);
			System.setProperty(ShikuConfigBean.USER_DB_URI_KEY, userdb);
		}

		if(null!=params.get(ShikuConfigBean.REDIS_URI_KEY)){
			String str=(String) params.get(ShikuConfigBean.REDIS_URI_KEY);
			System.setProperty(ShikuConfigBean.REDIS_DATABASE_KEY, str);
		}
		if(null!=params.get(ShikuConfigBean.REDIS_DATABASE_KEY)){
			String str=(String) params.get(ShikuConfigBean.REDIS_DATABASE_KEY);
			System.setProperty(ShikuConfigBean.REDIS_DATABASE_KEY, str);
		}
		if(null!=params.get(ShikuConfigBean.REDIS_PASSWORD_KEY)){
			String str=(String) params.get(ShikuConfigBean.REDIS_PASSWORD_KEY);
			System.setProperty(ShikuConfigBean.REDIS_PASSWORD_KEY, str);
		}
		if(null!=params.get(ShikuConfigBean.REDIS_ISCLUSTER_KEY)){
			String str=(String) params.get(ShikuConfigBean.REDIS_ISCLUSTER_KEY);
			System.setProperty(ShikuConfigBean.REDIS_ISCLUSTER_KEY, str);
		}
		if(null!=params.get(ShikuConfigBean.push_mqAddr_Key)){
			String str=(String) params.get(ShikuConfigBean.push_mqAddr_Key);
			System.setProperty(ShikuConfigBean.push_mqAddr_Key, str);
		}
		
		if(null!=params.get(ShikuConfigBean.shikuArchiveJid_KEY)){
			String str=(String) params.get(ShikuConfigBean.shikuArchiveJid_KEY);
			System.setProperty(ShikuConfigBean.shikuArchiveJid_KEY, str);
		}
		if(null!=params.get(ShikuConfigBean.shikuDeBug_KEY)){
			String str=(String) params.get(ShikuConfigBean.shikuDeBug_KEY);
			System.setProperty(ShikuConfigBean.shikuDeBug_KEY, str);
		}
		if(null!=params.get(ShikuConfigBean.OPENKEYWORD_KEY)){
			String str=(String) params.get(ShikuConfigBean.OPENKEYWORD_KEY).toString();
			System.setProperty(ShikuConfigBean.OPENKEYWORD_KEY, str);
		}
		if(null!=params.get(ShikuConfigBean.shikuDeBug_KEY)){
			String str=(String) params.get(ShikuConfigBean.shikuDeBug_KEY);
			System.setProperty(ShikuConfigBean.shikuDeBug_KEY, str);
		}
		if(null!=params.get(ShikuConfigBean.ShikuXmppTimeOut_KEY)){
			String str=params.get(ShikuConfigBean.ShikuXmppTimeOut_KEY).toString();
			System.setProperty(ShikuConfigBean.ShikuXmppTimeOut_KEY, str.toString());
		}
		if(null!=params.get(ShikuConfigBean.ShikuSaveMsg_KEY)){
			Integer str=(Integer) params.get(ShikuConfigBean.ShikuSaveMsg_KEY);
			System.setProperty(ShikuConfigBean.ShikuSaveMsg_KEY, str.toString());
		}
		if(null!=params.get(ShikuConfigBean.ShikuSaveMucMsg_KEY)){
			Integer str=(Integer) params.get(ShikuConfigBean.ShikuSaveMucMsg_KEY);
			System.setProperty(ShikuConfigBean.ShikuSaveMucMsg_KEY, str.toString());
		}
		if(null!=params.get(ShikuConfigBean.ShikuMsgSendTime_KEY)){
			Integer str=(Integer) params.get(ShikuConfigBean.ShikuMsgSendTime_KEY);
			System.setProperty(ShikuConfigBean.ShikuMsgSendTime_KEY, str.toString());
		}
	}
	
	private static boolean isTrue(String val) {
		if (val == null) {
			return false;
		}

		String value = val.toLowerCase();

		return (value.equals("true") || value.equals("yes") || value.equals("on") || value
				.equals("1"));
	}
}
