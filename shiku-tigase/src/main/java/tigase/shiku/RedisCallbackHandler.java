package tigase.shiku;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tigase.auth.callbacks.VerifyPasswordCallback;
import tigase.auth.impl.AuthRepoPlainCallbackHandler;
import tigase.shiku.db.RedisService;

/**
 * @author lidaye
 * 认证 redis user token  类
 *
 */
public class RedisCallbackHandler extends AuthRepoPlainCallbackHandler{

	/**
	 * 
	 */
	public RedisCallbackHandler() {
		
	}
	private  Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	@Override
	protected void handleVerifyPasswordCallback(VerifyPasswordCallback pc) throws IOException {
		boolean flag = RedisService.getInstance().authUser(jid.getLocalpart(), pc.getPassword());
		pc.setVerified(flag);
		logger.info("auth user {}  result {}",jid.getLocalpart(),flag);
		//super.handleVerifyPasswordCallback(pc);
	}
}
