package org.mule.runtime.core.internal.execution;

import java.util.HashMap;
import java.util.Map;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.execution.CompletableCallback;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.exception.MessagingException;
import org.reactivestreams.Publisher;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.mule.core.NRCoreUtils;
import com.newrelic.mule.core.NRMuleHeaders;

@Weave(type=MatchType.BaseClass)
public abstract class FlowProcessingTemplate {

	@Trace
	public CoreEvent routeEvent(CoreEvent muleEvent) {
		TypedValue<?> flowNameType = muleEvent.getVariables().get("app_feature_name");
		if (flowNameType != null) {
			String flowName = (String) flowNameType.getValue();
			NewRelic.setTransactionName("Fugu", "/flow/" + flowName);
		}
		Map<String, Object> attributes = new HashMap<String, Object>();
		NRCoreUtils.recordCoreEvent("Input", muleEvent, attributes);
		CoreEvent returnedEvent = Weaver.callOriginal();
		NRCoreUtils.recordCoreEvent("Returned", returnedEvent, attributes);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);

		return returnedEvent;
	}

	@Trace
	public Publisher<CoreEvent> routeEventAsync(CoreEvent event) {
		TypedValue<?> flowNameType = event.getVariables().get("app_feature_name");
		if (flowNameType != null) {
			String flowName = (String) flowNameType.getValue();
			NewRelic.setTransactionName("Fugu", "/flow/" + flowName);
		}
		Map<String, Object> attributes = new HashMap<String, Object>();
		NRCoreUtils.recordCoreEvent("Input", event, attributes);
		return Weaver.callOriginal();
	}

	@Trace
	public void sendResponseToClient(CoreEvent response, Map<String, Object> parameters, CompletableCallback<Void> callback) { 
		Map<String, Object> attributes = new HashMap<String, Object>();
		NRCoreUtils.recordCoreEvent("Response", response, attributes);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		if(callback.headers == null || callback.headers.isEmpty()) {
			callback.headers = new NRMuleHeaders();
			NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(callback.headers);
		}
		Weaver.callOriginal();
	}
	
	@Trace
	public void sendFailureResponseToClient(MessagingException exception, Map<String, Object> parameters,CompletableCallback<Void> callback) {
		CoreEvent event = exception.getEvent();
		Map<String, Object> attributes = new HashMap<String, Object>();
		NRCoreUtils.recordCoreEvent(null, event, attributes);
		Component failing = exception.getFailingComponent();
		if (failing != null) {
			ComponentLocation location = failing.getLocation();
			if(location != null) {
				NRCoreUtils.recordValue(attributes,"FailingComponent", location.getLocation());
			}
		}
		NRCoreUtils.recordValue(attributes, "Handled", exception.handled());
		NewRelic.noticeError(exception, attributes);
		if(callback.headers == null || callback.headers.isEmpty()) {
			callback.headers = new NRMuleHeaders();
			NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(callback.headers);
		}
		Weaver.callOriginal();
	}
	
	@Trace
	public Publisher<CoreEvent> routeEventAsync(Publisher<CoreEvent> eventPub) {
		return Weaver.callOriginal();
	}
}
