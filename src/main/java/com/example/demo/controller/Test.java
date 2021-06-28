package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.data.HttpResonse;
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

		HttpResonse response = service.callService("http://localhost:8080", "/test");

		if (response.getStatus().is2xxSuccessful())
			return response.getBody();

		return response.getMessage();

	}

}
