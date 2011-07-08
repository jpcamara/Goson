package com.jpcamara.gosu.json;

import org.json.JSONException;

public class JSONParserException extends RuntimeException {
	private static final long serialVersionUID = 5407463188711170624L;

	public JSONParserException(JSONException e) {
		super(e);
	}
	
	public JSONParserException(String message) {
		super(message);
	}
}