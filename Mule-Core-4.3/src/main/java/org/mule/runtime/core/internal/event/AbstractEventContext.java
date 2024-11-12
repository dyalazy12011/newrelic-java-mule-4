package org.mule.runtime.core.internal.event;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.mule.runtime.api.event.EventContext;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.exception.FlowExceptionHandler;
import org.mule.runtime.core.privileged.event.BaseEventContext;
import org.reactivestreams.Publisher;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.mule.core.HeaderUtils;
import com.newrelic.mule.core.NRMuleHeaders;

@Weave
abstract class AbstractEventContext implements BaseEventContext {
	
	@NewField
	public NRMuleHeaders headers = null;
	
	public AbstractEventContext() {
		
	}
	
	public AbstractEventContext(FlowExceptionHandler exceptionHandler, int depthLevel,Optional<CompletableFuture<Void>> externalCompletion) { 
		if(this instanceof DefaultEventContext) {
			setHeaders();
		}
	}
	
	public abstract Optional<BaseEventContext> getParentContext();

	void addChildContext(final BaseEventContext childContext) {
		if(childContext != null && childContext instanceof AbstractEventContext) {
			NRMuleHeaders childHeaders = MuleUtils.getHeaders(childContext);
			if(childHeaders == null || childHeaders.isEmpty()) {
				if(headers != null) {
					if(headers.isEmpty()) {
						NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(headers);
						if(!headers.isEmpty()) {
							MuleUtils.setHeaders(childContext,headers);
						}
					} 
				}
			}
		}
		Weaver.callOriginal();
	}

	public void success() {
		if(headers != null && !headers.isEmpty()) {
			HeaderUtils.acceptHeaders(headers);
			headers.clear();
			headers = null;
		}
		try {
			Optional<BaseEventContext> parent = getParentContext();
			if(parent != null && parent.isPresent()) {
				expireParent(parent);
			}
		} catch (NullPointerException e) {
		}
		Weaver.callOriginal();
	}
	
	private void expireParent(Optional<BaseEventContext> parent) {
		if(parent != null && parent.isPresent()) {
			BaseEventContext root = parent.get().getRootContext();
			if(root instanceof AbstractEventContext) {
				((AbstractEventContext)root).headers.clear();
				((AbstractEventContext)root).headers = null;
			}
		}
	}

	public void success(CoreEvent event) {
		TypedValue<?> flowNameType = event.getVariables().get("app_feature_name");
		if (flowNameType != null) {
			String flowName = (String) flowNameType.getValue();
			NewRelic.setTransactionName("Fugu", "/flow/" + flowName);
		}
		if(headers != null && !headers.isEmpty()) {
			HeaderUtils.acceptHeaders(headers);
			headers.clear();
			headers = null;
		} else {
			
			EventContext ctx = event.getContext();
			if(AbstractEventContext.class.isInstance(ctx)) {
				AbstractEventContext bctx = (AbstractEventContext)ctx;
				if(headers != null && !headers.isEmpty()) {
					HeaderUtils.acceptHeaders(headers);
					headers.clear();
					headers = null;
				}
				try {
					Optional<BaseEventContext> parent = bctx.getParentContext();
					expireParent(parent);
				} catch (NullPointerException e) {
				}
			}
		}
		try {
			Optional<BaseEventContext> parent = getParentContext();
			expireParent(parent);
		} catch (NullPointerException e) {
		}
		Weaver.callOriginal();
	}

	public Publisher<Void> error(Throwable throwable) {
		if(headers != null && !headers.isEmpty()) {
			HeaderUtils.acceptHeaders(headers);
			headers.clear();
			headers = null;
		}
		try {
			Optional<BaseEventContext> parent = getParentContext();
			expireParent(parent);
		} catch (NullPointerException e) {
		}
		NewRelic.noticeError(throwable);
		return Weaver.callOriginal();
	}

	private void setHeaders() {
		if(headers == null) {
			headers = MuleUtils.getHeaders(getRootContext());
			if(headers == null || headers.isEmpty()) {
				try {
					BaseEventContext root = getRootContext();
					if (root != null) {
						MuleUtils.setHeaders(root);
					}
				} catch (NullPointerException e) {
				}
			}
		}
	}

}
