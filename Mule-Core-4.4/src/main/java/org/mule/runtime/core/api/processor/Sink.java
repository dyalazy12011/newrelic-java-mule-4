package org.mule.runtime.core.api.processor;

import org.mule.runtime.core.api.construct.BackPressureReason;
import org.mule.runtime.core.api.event.CoreEvent;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type=MatchType.Interface)
public abstract class Sink {

	@Trace(excludeFromTransactionTrace=true)
	public BackPressureReason emit(CoreEvent event) {
		BackPressureReason returned = Weaver.callOriginal();
		
		return returned;
	}
	
	@Trace(async=true,excludeFromTransactionTrace=true)
	public void accept(final CoreEvent event) {
		TypedValue<?> flowNameType = event.getVariables().get("app_feature_name");
		if (flowNameType != null) {
			String flowName = (String) flowNameType.getValue();
			NewRelic.setTransactionName("Fugu", "/flow/" + flowName);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName(new String[] {"Custom","Sink",getClass().getSimpleName(),"accept"});
		Weaver.callOriginal();
	}
}
