package cn.bumo.sdk.core.event.source;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class TransactionNotifyEventSource implements EventSource{

    public static final String CODE = "TRANSACTION_NOTIFY_EVENT_SOURCE";

    @Override
    public String getCode(){
        return CODE;
    }

    @Override
    public String getName(){
        return "交易通知事件源";
    }

}
