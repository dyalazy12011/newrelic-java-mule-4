package org.mule.runtime.core.privileged.processor.chain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.mule.core.NRCoreUtils;

@Weave(type=MatchType.BaseClass)
class AbstractMessageProcessorChain {
	
	@NewField
	protected String chainName = "Unknown";
	
	AbstractMessageProcessorChain(final String name, final Optional<ProcessingStrategy> processingStrategyOptional,final List<Processor> processors) {
		if(name != null && !name.isEmpty()) {
			chainName = name;
		}
	}	

	@Trace(dispatcher=true)
	public CoreEvent process(final CoreEvent event) {
		TypedValue<?> flowNameType = event.getVariables().get("app_feature_name");
		if (flowNameType != null) {
			String flowName = (String) flowNameType.getValue();
			NewRelic.setTransactionName("Fugu", "/flow/" + flowName);
		}
		Map<String, Object> attributes = new HashMap<String, Object>();
		NRCoreUtils.recordCoreEvent("Input", event, attributes);
		NRCoreUtils.recordValue(attributes, "Chain", chainName);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName(new String[] {"Custom","MuleProcessorChain",getClass().getSimpleName(),"process",chainName});
		CoreEvent retValue = Weaver.callOriginal();
		NRCoreUtils.recordCoreEvent("Returned", retValue, attributes);
		traced.addCustomAttributes(attributes);
		return retValue;
	}
	
}
