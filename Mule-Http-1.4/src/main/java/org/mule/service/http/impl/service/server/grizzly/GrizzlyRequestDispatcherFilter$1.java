package org.mule.service.http.impl.service.server.grizzly;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
abstract class GrizzlyRequestDispatcherFilter$1 {

	@NewField
	private Token token = null;
	
	@WeaveAllConstructors
	GrizzlyRequestDispatcherFilter$1() {
		if(token == null) {
			Token t = NewRelic.getAgent().getTransaction().getToken();
			if(t != null && t.isActive()) {
				token = t;
			} else if(t != null) {
				t.expire();
				t = null;
			}
		}
	}
	
	@Trace(async = true)
	 public void responseReady(org.mule.runtime.http.api.domain.message.response.HttpResponse resp, org.mule.runtime.http.api.server.async.ResponseStatusCallback callback) {
		 if(token != null) {
			 token.linkAndExpire();
			 token = null;
		 }
		 Weaver.callOriginal();
	 }
}
