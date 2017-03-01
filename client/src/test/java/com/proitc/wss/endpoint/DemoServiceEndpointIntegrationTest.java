package com.proitc.wss.endpoint;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.proitc.wss.configuration.WebServiceConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebServiceConfiguration.class)
@SpringBootTest
public class DemoServiceEndpointIntegrationTest {

	@Autowired
	@Qualifier("recepcionWSClient")
	private DemoServiceEndpointPortType demoClient;

	@Test
	public void shouldResultOK() {
		String result = demoClient.status();
		assertEquals("OK", result);
	}
}
