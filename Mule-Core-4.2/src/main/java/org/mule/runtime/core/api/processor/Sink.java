package org.mule.runtime.core.api.processor;

import java.util.HashMap;
import java.util.Map;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.event.MuleUtils;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.mule.core.NRCoreUtils;

@Weave(type=MatchType.Interface)
public abstract class Sink {

	@Trace(async=true,excludeFromTransactionTrace=true)
	public boolean emit(CoreEvent event) {
		Map<String, Object> attributes = new HashMap<String, Object>();
		NRCoreUtils.recordCoreEvent("Input", event, attributes);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		NewRelic.getAgent().getTracedMethod().setMetricName(new String[] {"Custom","Sink",getClass().getSimpleName(),"emit"});
		Token token = MuleUtils.getToken(event);
		if(token != null) {
			token.link();
		} else {
			token = NewRelic.getAgent().getTransaction().getToken();
			MuleUtils.setToken(event, token);
		}
		boolean returned = Weaver.callOriginal();
		
		return returned;
	}
	
	@Trace(async=true,excludeFromTransactionTrace=true)
	public void accept(final CoreEvent event) {
		NewRelic.getAgent().getTracedMethod().setMetricName(new String[] {"Custom","Sink",getClass().getSimpleName(),"accept"});
		Token token = MuleUtils.getToken(event);
		if(token != null) {
			token.link();
		} else {
			token = NewRelic.getAgent().getTransaction().getToken();
			MuleUtils.setToken(event, token);
		}
		Weaver.callOriginal();
	}
}
