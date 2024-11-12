package org.mule.runtime.core.api.processor;

import java.util.HashMap;
import java.util.Map;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.event.MuleUtils;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.mule.core.MuleReactorUtils;
import com.newrelic.mule.core.NRCoreUtils;

@Weave(type=MatchType.Interface)
public abstract class Processor {

	@Trace(async=true)
	public CoreEvent process(CoreEvent event) {
		TypedValue<?> flowNameType = event.getVariables().get("app_feature_name");
		if (flowNameType != null) {
			String flowName = (String) flowNameType.getValue();
			NewRelic.setTransactionName("Fugu", "/flow/" + flowName);
		}
		Map<String, Object> attributes = new HashMap<String, Object>();
		NRCoreUtils.recordCoreEvent("Input", event, attributes);
		if(!MuleReactorUtils.initialized) {
			MuleReactorUtils.init();
		}
		Token token = MuleUtils.getToken(event);
		if(token != null) {
			token.link();
		} else {
			token = NewRelic.getAgent().getTransaction().getToken();
			MuleUtils.setToken(event, token);
		}
		CoreEvent returnedEvent = Weaver.callOriginal();
		NRCoreUtils.recordCoreEvent("Returned", returnedEvent, attributes);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		
		Token token2 = MuleUtils.getToken(returnedEvent);
		if(token2 == null && token != null) {
			MuleUtils.setToken(returnedEvent, token);
			
		}
		
		return returnedEvent;
	}
	
}
