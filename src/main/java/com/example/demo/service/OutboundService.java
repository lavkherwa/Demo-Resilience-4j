package com.example.demo.service;

import java.net.URI;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.data.HttpResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
public class OutboundService {

	private static final String OUTBOUND_SERVICE = "outbound-service";

	private RestTemplate restTemplate;

	public OutboundService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;

	}

	@CircuitBreaker(name = OUTBOUND_SERVICE)
	@Retry(name = OUTBOUND_SERVICE, fallbackMethod = "outboundServiceFallback")
	public HttpResponse callService(String url, String endpoint) {

		System.out.println("attempting to call external service");

		// @formatter:off
		final URI serviceURI = UriComponentsBuilder
										.fromHttpUrl(url)
										.path(endpoint)
										.build(true)
										.toUri();

		
		RequestEntity<Void> requestEntity = RequestEntity
												.get(serviceURI)
												.headers(initHeaders())
												.build();
		// @formatter:on

		ResponseEntity<String> response = null;

		/* Try to perform the HTTP request call */
		try {
			response = restTemplate.exchange(requestEntity, String.class);
		} catch (HttpClientErrorException exp) {
			// @formatter:off
			final String exception = String.format(
					"ERROR: Call failed; with status code %d; Error `%s` on application URL `%s`; Status Code Text `%s`; Response Body `%s`",
					exp.getStatusCode().value(), 
					exp.getMessage(), 
					serviceURI, 
					exp.getStatusText(),
					exp.getResponseBodyAsString());

			System.out.println(exception);
			
			return HttpResponse
						.builder()
						.status(exp.getStatusCode())
						.message(exp.getMessage())
						.build();
			// @formatter:on
		}

		/* return the body if the response was successful */
		// @formatter:off
 		return response != null
				? HttpResponse
						.builder()
						.status(response.getStatusCode())
						.body(response.getBody())
						.build()
				: null;
 		// @formatter:on
	}

	public HttpResponse outboundServiceFallback(Exception exp) {

		if (exp instanceof HttpServerErrorException) {
			// @formatter:off
			final String exception = String.format(
					"ERROR: Call failed; with status code %d; Error `%s` on application URL `%s`; Status Code Text `%s`; Response Body `%s`",
					((HttpServerErrorException) exp).getStatusCode().value(), 
					((HttpServerErrorException) exp).getMessage(), 
					null, // serviceURI
					((HttpServerErrorException) exp).getStatusText(),
					((HttpServerErrorException) exp).getResponseBodyAsString());
	
			System.out.println("Exception occured: details: " + exception);
			return HttpResponse
					.builder()
					.status(((HttpServerErrorException) exp).getStatusCode())
					.message(((HttpServerErrorException) exp).getMessage())
					.build();
			// @formatter:on
		}

		// for ResourceAccessException exception give SERVICE UNAVAILABLE - 503
		System.out.println("Exception occured: details: " + exp.getMessage());

		// @formatter:off
		return HttpResponse
				.builder()
				.status(HttpStatus.SERVICE_UNAVAILABLE)
				.message(exp.getMessage())
				.build();
		// @formatter:on
	}

	private HttpHeaders initHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

}
