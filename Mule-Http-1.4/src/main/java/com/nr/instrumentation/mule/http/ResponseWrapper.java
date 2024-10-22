package com.nr.instrumentation.mule.http;

import org.glassfish.grizzly.http.HttpResponsePacket;

import com.newrelic.api.agent.ExtendedResponse;
import com.newrelic.api.agent.HeaderType;

public class ResponseWrapper extends ExtendedResponse {
	
	private HttpResponsePacket response = null;
	
	public ResponseWrapper(HttpResponsePacket resp) {
		response = resp;
	}

	@Override
	public String getContentType() {
		return response.getContentType();
	}

	@Override
	public int getStatus() throws Exception {
		return response.getStatus();
	}

	@Override
	public String getStatusMessage() throws Exception {
		return response.getReasonPhrase();
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.HTTP;
	}

	@Override
	public void setHeader(String name, String value) {
		response.setHeader(name, value);
	}

	@Override
	public long getContentLength() {
		return response.getContentLength();
	}

}
