package tigase.shiku.conf;



import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import tigase.conf.Configurator;

public class ShikuConfigurator extends Configurator{
	

	@Override
	public void parseArgs(String[] args) {
		// TODO Auto-generated method stub
		super.parseArgs(args);
		
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory(); 
		Logger rootLogger = loggerContext.getLogger("org.mongodb.driver"); 
		rootLogger.setLevel(ch.qos.logback.classic.Level.ERROR); 
		Logger redisLogger = loggerContext.getLogger("org.redisson"); 
		redisLogger.setLevel(ch.qos.logback.classic.Level.ERROR); 
		System.out.println("============ShikuConfigurator  > parseArgs ");
		
	}
	
	@Override
	public String getMessageRouterClassName() {
		// TODO Auto-generated method stub
		//return super.getMessageRouterClassName();
		
		return "tigase.shiku.conf.ShikuMessageRouter";
	}
}
