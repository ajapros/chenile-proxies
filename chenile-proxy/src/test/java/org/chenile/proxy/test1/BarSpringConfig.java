package org.chenile.proxy.test1;

import org.chenile.proxy.builder.ProxyBuilder;
import org.chenile.proxy.builder.ProxyBuilder.ProxyMode;
import org.chenile.proxy.test.service.FooInterceptor;
import org.chenile.proxy.test.service.FooService;
import org.chenile.proxy.test.service.FooServiceImpl;
import org.chenile.proxy.test1.service.BarService;
import org.chenile.proxy.test1.service.BarServiceImpl;
import org.chenile.proxy.test1.service.Baz1;
import org.chenile.proxy.test1.service.Baz2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@SpringBootApplication(scanBasePackages = { "org.chenile.configuration", "org.chenile.service.registry.configuration" })
@PropertySource("classpath:org/chenile/proxy/test/TestChenileProxy.properties")
@PropertySource("classpath:application-fixedport1.properties")
@ActiveProfiles("unittest")
@EnableJpaRepositories(basePackages = "org.chenile.**.configuration.dao")
@EntityScan("org.chenile.**.model")
public class BarSpringConfig extends SpringBootServletInitializer{
	
	@Autowired ProxyBuilder proxyBuilder;
	@Value("${server.port}") String serverPort;


	public static void main(String[] args) {
		SpringApplication.run(BarSpringConfig.class, args);
	}

	@Bean("_fooService_") public FooService _fooService_() {
		return new FooServiceImpl();
	}

	@Bean("_barService1_") public BarService<Baz1> _barService1_() {
		return new BarServiceImpl<>();
	}

	@Bean("_barService2_") public BarService<Baz2> _barService2_() {
		return new BarServiceImpl<>();
	}

	@Bean public FooInterceptor fooInterceptor() {
		return new FooInterceptor();
	}

	@Bean public BarService<Baz1> barService1Proxy() {
		return proxyBuilder.buildProxy(BarService.class, "barService1",null,
				ProxyMode.COMPUTE_DYNAMICALLY, "localhost:" + serverPort);
	}
	
	@Bean public BarService<Baz1> barService1OnlyRemote() {
		return proxyBuilder.buildProxy(BarService.class, "barService1",null,
				ProxyMode.REMOTE, "localhost:" + serverPort);
	}

	@Bean public BarService<Baz2> barService2OnlyRemote() {
		return proxyBuilder.buildProxy(BarService.class, "barService2",null,
				ProxyMode.REMOTE, "localhost:" + serverPort);
	}

}

