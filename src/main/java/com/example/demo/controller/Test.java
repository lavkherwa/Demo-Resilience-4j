package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.data.HttpResponse;
import com.example.demo.service.OutboundService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
@RequestMapping("/")
public class Test {

	private static final String OUTBOUND_SERVICE = "outbound-service";

	private OutboundService service;

	public Test(OutboundService service) {
		this.service = service;

	}

	@RequestMapping("/test")
	public String callExternalService() {

		HttpResponse response = service.callService("http://localhost:8080", "/test");

		if (response.getStatus().is2xxSuccessful())
			return response.getBody();

		return "Status code: " + response.getStatus().value() + ", message: " + response.getMessage();

	}

	@RequestMapping("testRateLimit")
	@RateLimiter(name = OUTBOUND_SERVICE, fallbackMethod = "rateLimitFallback")
	public ResponseEntity<String> callExternalService2() {
		return new ResponseEntity<String>("Success", HttpStatus.OK);
	}

	public ResponseEntity<String> rateLimitFallback(Exception exp) {
		return new ResponseEntity<String>("Too Many Requests", HttpStatus.TOO_MANY_REQUESTS);
	}
}
