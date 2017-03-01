package com.proitc.wss.configuration;

import static org.apache.cxf.Bus.DEFAULT_BUS_ID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.ws.Endpoint;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.proitc.wss.endpoint.DemoServiceEndpoint;
import com.proitc.wss.sei.DemoService;
import com.proitc.wss.support.ClientKeystorePasswordCallback;

@Configuration
@PropertySource("application-${spring.profiles.active}.properties")
public class WebServiceConfiguration {
	private static final Logger log = LoggerFactory.getLogger(WebServiceConfiguration.class.getName());
	@Value("${service.contextPath}")
	private String contextPath;
	@Value("${service.endpointUrl}")
	private String endpointUrl;
	@Value("${service.wsdlLocation}")
	private String wsdlLocation;
	/* Datos keystore */
	@Value("${keystore.alias}")
	private String keystoreAlias;
	@Value("${keystore.password}")
	private String keystorePassword;
	@Value("${keystore.file}")
	private String keystoreFile;
	@Value("${keystore.type}")
	private String keystoreType;
	
	/**
	 * Contexto del servicio
	 */
	@Bean
	public ServletRegistrationBean dispatcherServlet() {
		return new ServletRegistrationBean(new CXFServlet(), contextPath);
	}

	/**
	 * Bus de integración CXF/Spring
	 */
	@Bean(name = DEFAULT_BUS_ID)
	public SpringBus springBus() {
		SpringBus springBus = new SpringBus();
		springBus.setFeatures(Arrays.asList(new LoggingFeature()));
		return springBus;
	}

	/**
	 * Implementación del servicio
	 */
	public DemoService demoServiceEndpoint() {
		return new DemoServiceEndpoint();
	}

	/**
	 * Ubicación del wsdl y el endpoint
	 */
	@Bean
	public Endpoint endpoint() {
		EndpointImpl endpoint = new EndpointImpl(springBus(), demoServiceEndpoint());
		endpoint.publish(endpointUrl);
		log.info("Publicando servicio en " + endpointUrl);
		endpoint.setWsdlLocation(wsdlLocation);
		endpoint.getOutInterceptors().add(wss4jOut());
		//endpoint.getInInterceptors().add(wss4jIn());
		return endpoint;
	}

	public WSS4JOutInterceptor wss4jOut() {
		Map<String, Object> properties = new HashMap<>();
		properties.put(ConfigurationConstants.ACTION,
				ConfigurationConstants.SIGNATURE + " " + ConfigurationConstants.TIMESTAMP);
		properties.put("signingProperties", wss4jOutProperties());
		properties.put(ConfigurationConstants.SIG_PROP_REF_ID, "signingProperties");
		properties.put(ConfigurationConstants.SIG_KEY_ID, "DirectReference");
		properties.put(ConfigurationConstants.USER, keystoreAlias);
		properties.put(ConfigurationConstants.SIGNATURE_PARTS,
				"{Element}{http://schemas.xmlsoap.org/soap/envelope/}Body");
		properties.put(ConfigurationConstants.PW_CALLBACK_REF, clientKeystorePasswordCallback());
		properties.put(ConfigurationConstants.SIG_ALGO, "http://www.w3.org/2000/09/xmldsig#rsa-sha1");
		WSS4JOutInterceptor interceptor = new WSS4JOutInterceptor(properties);
		return interceptor;
	}

	public Properties wss4jOutProperties() {
		Properties properties = new Properties();
		properties.put("org.apache.wss4j.crypto.merlin.provider", "org.apache.wss4j.common.crypto.Merlin");
		properties.put("org.apache.wss4j.crypto.merlin.keystore.type", keystoreType);
		properties.put("org.apache.wss4j.crypto.merlin.keystore.password", keystorePassword);
		properties.put("org.apache.wss4j.crypto.merlin.keystore.alias", keystoreAlias);
		properties.put("org.apache.wss4j.crypto.merlin.keystore.file", keystoreFile);
		return properties;
	}
	
	public CallbackHandler clientKeystorePasswordCallback() {
		Map<String, String> passwords = new HashMap<>();
		passwords.put(keystoreAlias, keystorePassword);
		return new ClientKeystorePasswordCallback(passwords);
	}

}
