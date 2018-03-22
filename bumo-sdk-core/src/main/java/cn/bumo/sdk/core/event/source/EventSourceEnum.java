package cn.bumo.sdk.core.event.source;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public enum EventSourceEnum{

    LEDGER_SEQ_INCREASE(new LedgerSeqIncreaseEventSource()),
    TRANSACTION_NOTIFY(new TransactionNotifyEventSource()),

    ;
    private EventSource eventSource;

    EventSourceEnum(EventSource eventSource){
        this.eventSource = eventSource;
    }

    public EventSource getEventSource(){
        return eventSource;
    }
}
