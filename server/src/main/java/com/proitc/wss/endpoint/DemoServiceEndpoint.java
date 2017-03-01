package com.proitc.wss.endpoint;

import com.proitc.wss.sei.DemoService;

public class DemoServiceEndpoint implements DemoService {

	@Override
	public String status() {
		return "OK";
	}
}
