package org.mule.runtime.core.internal.processor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.event.MuleUtils;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.mule.core.NRCoreUtils;

@Weave
public abstract class InvokerMessageProcessor {
	
	protected Method method = Weaver.callOriginal();

	@Trace(async=true)
	public CoreEvent process(final CoreEvent event) {
		TypedValue<?> flowNameType = event.getVariables().get("app_feature_name");
		if (flowNameType != null) {
			String flowName = (String) flowNameType.getValue();
			NewRelic.setTransactionName("Fugu", "/flow/" + flowName);
		}
		Map<String, Object> attributes = new HashMap<String, Object>();
		NRCoreUtils.recordCoreEvent("Input", event, attributes);
		Token token = MuleUtils.getToken(event);
		if(token != null) {
			token.link();
		}
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName(new String[] {"Custom","InvokerMessageProcessor","process",method.getDeclaringClass().getName(),method.getName()});
		CoreEvent returnedEvent =  Weaver.callOriginal();
		NRCoreUtils.recordCoreEvent("Returned", returnedEvent, attributes);
		
		traced.addCustomAttributes(attributes);
		return returnedEvent;
	}
}
