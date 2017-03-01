package com.proitc.wss.sei;

import javax.jws.WebService;

@WebService(targetNamespace = "http://endpoint.wss.proitc.com/")
public interface DemoService {
	public String status();
}
