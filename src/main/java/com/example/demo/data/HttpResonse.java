package com.example.demo.data;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HttpResonse {

	private HttpStatus status;
	private String message;
	private String body;

}
