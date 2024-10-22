package com.nr.instrumentation.mule.http;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.util.DataChunk;

import com.newrelic.api.agent.ExtendedRequest;
import com.newrelic.api.agent.HeaderType;

public class InboundRequest extends ExtendedRequest {
	
	private HttpRequestPacket request = null;
	
	public InboundRequest(HttpRequestPacket packet) {
		request = packet;
	}

	@Override
	public String getRequestURI() {
		return request.getRequestURI();
	}

	@Override
	public String getRemoteUser() {
		DataChunk remoteUserChuck = request.remoteUser();
		if(remoteUserChuck != null) {
			return remoteUserChuck.toString();
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getParameterNames() {
		String queryString = request.getQueryString();
		if(queryString != null && !queryString.isEmpty()) {
			StringTokenizer st = new StringTokenizer(queryString, "&");
			List<String> list = new ArrayList<String>();
			
			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				int index = token.indexOf('=');
				if(index > 0) {
					String tmp = token.substring(0, index-1);
					list.add(tmp);
				}
			}
			return Collections.enumeration(list);
		}
		return null;
	}

	@Override
	public String[] getParameterValues(String name) {
		String queryString = request.getQueryString();
		if(queryString != null && !queryString.isEmpty()) {
			StringTokenizer st = new StringTokenizer(queryString, "&");
			List<String> list = new ArrayList<String>();
			
			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				int index = token.indexOf('=');
				if(index > 0) {
					String tmp = token.substring(index+1);
					list.add(tmp);
				}
			}
			String[] values = new String[list.size()];
			list.toArray(values);
			return values;
		}
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return request.getAttribute(name);
	}

	@Override
	public String getCookieValue(String name) {
		return null;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.HTTP;
	}

	@Override
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public String getMethod() {
		return request.getMethod().getMethodString();
	}

}
