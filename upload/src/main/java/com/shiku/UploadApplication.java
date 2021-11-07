package com.shiku;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.csource.common.MyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.shiku.commons.TaskManager;
import com.shiku.commons.utils.ConfigUtils;
import com.shiku.commons.utils.FastDFSUtils;

/**
 * @author lidaye
 */
@ServletComponentScan
@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
public class UploadApplication extends SpringBootServletInitializer  implements EnvironmentAware{

    private static final Logger log = LoggerFactory.getLogger(UploadApplication.class);

    public static void main(String[] args) {
    	SpringApplication application=new SpringApplication(UploadApplication.class);

    	application.run(args);
    	
    	//SpringApplication.run(UploadApplication.class, args);
        log.info("启动成功...=======>");
       String configInfo =ConfigUtils.configInfo();
       log.info("\n upload config ======================>");
       log.info(configInfo);
       log.info("\n==========================================================================>");
       String dfsConfig = ConfigUtils.getFastDFSConfigInfo();
       log.info("\n FastDFS config ======================>");
       log.info(dfsConfig);
		
       log.info("\n============================> 上传服务启动成功 请放心使用  ===========>");
     //ClassUtils.runAllGetMethod(SKBeanUtils.getSystemConfig());
     TaskManager taskManager=new TaskManager();
     taskManager.onStartup();
    }

	@Override
	public void setEnvironment(Environment env) {
		try {
			FastDFSUtils.initServer(env);
			
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		// TODO Auto-generated method stub
		 // 注意这里要指向原先用main方法执行的Application启动类
        return builder.sources(UploadApplication.class);
	}
	
	
	//如果没有使用默认值80 
	  @Value("${http.port:8092}") 
	  Integer httpPort; 
	  
	  //正常启用的https端口 如443 
	  @Value("${server.port}") 
	  Integer httpsPort; 
	  
	  
	 @Bean 
	 @ConditionalOnProperty(name = "server.openHttps", havingValue = "true")
	 public TomcatServletWebServerFactory servletContainer() {
		 TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory () { 
			 @Override 
			 protected void postProcessContext(Context context) {
				 SecurityConstraint securityConstraint = new SecurityConstraint(); 
				 securityConstraint.setUserConstraint("CONFIDENTIAL"); 
				 SecurityCollection collection = new SecurityCollection(); 
				 collection.addPattern("/*"); 
				 securityConstraint.addCollection(collection); 
				 context.addConstraint(securityConstraint); 
				 } 
			 }; 
			 tomcat.addAdditionalTomcatConnectors(initiateHttpConnector()); 
			 return tomcat; 
	 }

	 private Connector initiateHttpConnector() { 
		 log.info("启用http转https协议，http端口："+this.httpPort+"，https端口："+this.httpsPort);
		 Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol"); 
		 connector.setScheme("http"); 
		 connector.setPort(httpPort); 
		 connector.setSecure(true); 
		 connector.setRedirectPort(httpsPort); 
		 return connector; 
	}
	 
	/* @Bean
	public FilterRegistrationBean filterRegistrationBean() {
		AuthorizationFilter filter = new AuthorizationFilter();
		Map<String, String> initParameters = Maps.newHashMap();
		initParameters.put("enable", "true");
		List<String> urlPatterns = Arrays.asList("/*");

		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(filter);
		
		registrationBean.setInitParameters(initParameters);
		registrationBean.setUrlPatterns(urlPatterns);
		return registrationBean;
	}*/
    
	
}
