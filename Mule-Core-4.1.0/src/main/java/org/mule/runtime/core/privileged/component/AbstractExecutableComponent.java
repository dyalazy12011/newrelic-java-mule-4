package org.mule.runtime.core.privileged.component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.component.execution.ExecutionResult;
import org.mule.runtime.api.component.execution.InputEvent;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.event.MuleUtils;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.mule.core.NRCoreUtils;

@Weave(type=MatchType.BaseClass)
public abstract class AbstractExecutableComponent extends AbstractComponent {

	@Trace(dispatcher=true)
	public CompletableFuture<ExecutionResult> execute(InputEvent paramInputEvent) {
		TypedValue<?> flowNameType = paramInputEvent.getVariables().get("app_feature_name");
		if (flowNameType != null) {
			String flowName = (String) flowNameType.getValue();
			NewRelic.setTransactionName("Fugu", "/flow/" + flowName);
		}
		CompletableFuture<ExecutionResult> f = Weaver.callOriginal();
		ComponentLocation location = getLocation();
		if(location != null) {
			String locationStr = location.getLocation();
			if(locationStr != null && !locationStr.isEmpty()) {
				NewRelic.getAgent().getTracedMethod().setMetricName(new String[] {"Custom","AbstractExecutableComponent",getClass().getSimpleName(),"execute"});
			}
		}
		return f;
	}

	@Trace(dispatcher=true,async=true)
	public CompletableFuture<Event> execute(Event paramEvent) {
		TypedValue<?> flowNameType = paramEvent.getVariables().get("app_feature_name");
		if (flowNameType != null) {
			String flowName = (String) flowNameType.getValue();
			NewRelic.setTransactionName("Fugu", "/flow/" + flowName);
		}
		Map<String, Object> attributes = new HashMap<String, Object>();
		NRCoreUtils.recordCoreEvent(null, paramEvent, attributes);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.addCustomAttributes(attributes);
		if(CoreEvent.class.isInstance(paramEvent)) {
			Token token = MuleUtils.getToken((CoreEvent)paramEvent);
			if(token != null) {
				token.link();
			}
		}
		CompletableFuture<Event> f = Weaver.callOriginal();
		ComponentLocation location = getLocation();
		if(location != null) {
			String locationStr = location.getLocation();
			if(locationStr != null && !locationStr.isEmpty()) {
				traced.addCustomAttribute("Location", locationStr);
				traced.setMetricName(new String[] {"Custom","AbstractExecutableComponent",getClass().getSimpleName(),"execute"});
			}
		}
		
		return f;
	}

}
