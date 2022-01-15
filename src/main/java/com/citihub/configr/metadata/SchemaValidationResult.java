package com.citihub.configr.metadata;

import lombok.Getter;

public class SchemaValidationResult {
	
	@Getter
	private Boolean isSuccess;
	
	@Getter
	private String message;
	
	public SchemaValidationResult(Boolean isSuccess, String message) {
		this.isSuccess = isSuccess;
		this.message = message;
	}

}
