package com.proitc.wss.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.proitc.wss.endpoint.DemoServiceEndpointPortType;

@Configuration
@PropertySource("classpath:application-${spring.profiles.active}.properties")
public class WebServiceConfiguration {
	private static final Logger log = LoggerFactory.getLogger(WebServiceConfiguration.class.getName());
	@Value("${service.url}")
	private String serviceUrl;
	/* Datos truststore */
	@Value("${truststore.alias}")
	private String truststoreAlias;
	@Value("${truststore.password}")
	private String truststorePassword;
	@Value("${truststore.file}")
	private String truststoreFile;
	@Value("${truststore.type}")
	private String truststoreType;

	/**
	 * Servicio Cliente
	 */
	@Bean(name = "recepcionWSClient")
	public DemoServiceEndpointPortType efacturaConsultasClient() {
		JaxWsProxyFactoryBean jaxWsProxyFactory = new JaxWsProxyFactoryBean();
		jaxWsProxyFactory.setServiceClass(DemoServiceEndpointPortType.class);
		jaxWsProxyFactory.setAddress(serviceUrl);
		log.info("Consumiendo servicio de " + serviceUrl);
		jaxWsProxyFactory.getInInterceptors().add(wss4jIn());
		return (DemoServiceEndpointPortType) jaxWsProxyFactory.create();
	}

	/* WSS4JInInterceptor para validar firma del servidor */
	public WSS4JInInterceptor wss4jIn() {
		Map<String, Object> properties = new HashMap<>();
		properties.put(ConfigurationConstants.ACTION,
				ConfigurationConstants.SIGNATURE + " " + ConfigurationConstants.TIMESTAMP);
		properties.put("signingProperties", wss4jInProperties());
		properties.put(ConfigurationConstants.SIG_PROP_REF_ID, "signingProperties");
		properties.put(ConfigurationConstants.SIG_KEY_ID, "DirectReference");
		properties.put(ConfigurationConstants.SIGNATURE_PARTS,
				"{Element}{http://schemas.xmlsoap.org/soap/envelope/}Body");
		properties.put(ConfigurationConstants.SIG_ALGO, "http://www.w3.org/2000/09/xmldsig#rsa-sha1");
		WSS4JInInterceptor interceptor = new WSS4JInInterceptor(properties);
		return interceptor;
	}

	public Properties wss4jInProperties() {
		Properties properties = new Properties();
		properties.put("org.apache.wss4j.crypto.merlin.provider", "org.apache.wss4j.common.crypto.Merlin");
		properties.put("org.apache.wss4j.crypto.merlin.keystore.type", truststoreType);
		properties.put("org.apache.wss4j.crypto.merlin.keystore.password", truststorePassword);
		properties.put("org.apache.wss4j.crypto.merlin.keystore.alias", truststoreAlias);
		properties.put("org.apache.wss4j.crypto.merlin.keystore.file", truststoreFile);
		return properties;
	}

}
