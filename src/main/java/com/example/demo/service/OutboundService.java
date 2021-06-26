package com.example.demo.service;

import java.net.URI;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
public class OutboundService {

	private static final String OUTBOUND_SERVICE = "outbound-service";

	private RestTemplate restTemplate;

	public OutboundService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;

	}

	@CircuitBreaker(name = OUTBOUND_SERVICE, fallbackMethod = "outboundServiceFallback")
	public String callService(String url, String endpoint) {

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)//
				.path(endpoint);

		final URI serviceURI = uriBuilder.build(true).toUri();

		RequestEntity<Void> requestEntity = RequestEntity.get(serviceURI).headers(initHeaders()).build();

		ResponseEntity<String> response = null;

		try {
			/* Try to perform the HTTP request call */
			response = restTemplate.exchange(requestEntity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			// @formatter:off
			final String exception = String.format(
					"ERROR: Call failed; with status code %d; Error `%s` on application URL `%s`; Status Code Text `%s`; Response Body `%s`",
					ex.getStatusCode().value(), 
					ex.getMessage(), 
					serviceURI, 
					ex.getStatusText(),
					ex.getResponseBodyAsString());
			// @formatter:on
			System.out.println(exception);

		}

		/* return the body if the response was successful */
		return response != null ? response.getBody() : null;
	}

	public String outboundServiceFallback(Exception exp) {

		return "outbound service is down; details: " + exp.getMessage();
	}

	private HttpHeaders initHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

}
