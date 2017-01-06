package de.hbznrw.ygor.iet.enums;

public enum Status {
	
	UNKNOWN_REQUEST    ("UNKNOWN_REQUEST"),
	
	STATUS_OK          ("STATUS_OK"),
	STATUS_ERROR       ("STATUS_ERROR"),
	STATUS_NO_RESPONSE ("STATUS_NO_RESPONSE"),   
	
	// use as default value
	UNDEFINED          ("UNDEFINED"),
	// use for hardcoded values
	HARDCODED          ("HARDCODED"),
	// use for constants, tmp vars, etc
	IGNORE             ("IGNORE"),

	// api responses
	
	RESULT_OK               ("RESULT_OK"),
	RESULT_NO_MATCH         ("RESULT_NO_MATCH"),
	RESULT_MULTIPLE_MATCHES ("RESULT_MULTIPLE_MATCHES");
	
	private String value;
	
	Status(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
}
