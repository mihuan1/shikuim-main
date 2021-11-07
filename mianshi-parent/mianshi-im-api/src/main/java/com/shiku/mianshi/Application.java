package com.shiku.mianshi;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.google.common.collect.Maps;
import com.shiku.mianshi.filter.AuthorizationFilter;

import cn.xyz.commons.autoconfigure.GracefulShutdownTomcat;
import cn.xyz.commons.autoconfigure.KApplicationProperties.AppConfig;
import cn.xyz.mianshi.utils.SKBeanUtils;

@EnableScheduling
@Configuration
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, RedisAutoConfiguration.class,
        DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
@ComponentScan({"cn.xyz", "com.shiku"})
public class Application extends SpringBootServletInitializer {

    @Autowired(required = false)
    private KAdminProperties props;

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Resource(name = "appConfig")
    public AppConfig appConfig;

    @Autowired
    private GracefulShutdownTomcat gracefulShutdownTomcat;


    public static void main(String... args) {
        /**
         * 内置Tomcat版本导致的 The valid characters are defined in RFC 7230 and RFC 3986
         * 修改 系统参数
         */
        try {
            System.setProperty("tomcat.util.http.parser.HttpParser.requestTargetAllow", "|{}");
            System.setProperty("es.set.netty.runtime.available.processors", "false");
            SpringApplication.run(Application.class, args);
            log.info("启动成功  当前版本编译时间  =====》 " + SKBeanUtils.getLocalSpringBeanManager().getAppConfig().getBuildTime());
        } catch (Exception e) {
            System.out.println("启动报错=== " + e.getMessage());

        }

    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        try {
            return application.sources(new Class[]{Application.class});
        } catch (Exception e) {
            log.error("启动报错", e);
            return null;
        }

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
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
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
        tomcat.addConnectorCustomizers(gracefulShutdownTomcat);
        return tomcat;
    }

    private Connector initiateHttpConnector() {
        System.out.println("启用http转https协议，http端口：" + this.httpPort + "，https端口：" + this.httpsPort);
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(true);
        connector.setRedirectPort(httpsPort);
        return connector;
    }


    @Bean
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
    }

    @Bean(name = "viewResolver")
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/pages/");
        viewResolver.setSuffix(".jsp");

        return viewResolver;
    }


    @Bean
    public ViewResolver getViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/pages/");
        resolver.setSuffix(".html");
        return resolver;
    }


    private static Map<String, String> dataConversion(Map<String, String> map, String[] data) {
        for (String t : data) {
            String[] user = t.split(":");
            //System.out.println(user.toString());
            map.put(user[0], user[1]);
        }
        return map;
    }
}
