package cn.bumo.sdk.core.seq;

import cn.bumo.sdk.core.event.handle.AbstractEventHandler;
import cn.bumo.sdk.core.event.message.TransactionExecutedEventMessage;
import cn.bumo.sdk.core.event.source.EventSource;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public abstract class AbstractSequenceManager extends AbstractEventHandler<TransactionExecutedEventMessage> implements SequenceManager{

    public AbstractSequenceManager(EventSource eventSource, Class<TransactionExecutedEventMessage> messageClass){
        super(eventSource, messageClass);
    }

    @Override
    public void init(){
    }

    @Override
    public void destroy(){
    }

	@Override
	public void restore(String address,long oldVal) {
		restoreBack(address,oldVal);
	}

	public abstract void restoreBack(String address,long oldVal);
}
