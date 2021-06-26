package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.OutboundService;

@RestController
@RequestMapping("/")
public class Test {

	private OutboundService service;

	public Test(OutboundService service) {
		this.service = service;
	
	}
	
	@RequestMapping("/test")
	public String callExternalService() {
		
		return service.callService("http://localhost:8080", "/test");
		
	}
	
}
