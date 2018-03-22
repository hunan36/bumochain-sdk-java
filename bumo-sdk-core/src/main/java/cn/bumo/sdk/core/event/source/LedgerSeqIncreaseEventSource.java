package cn.bumo.sdk.core.event.source;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class LedgerSeqIncreaseEventSource implements EventSource{

    public static final String CODE = "LEDGER_SEQ_INCREASE_EVENT_SOURCE";

    @Override
    public String getCode(){
        return CODE;
    }

    @Override
    public String getName(){
        return "区块seq增加事件";
    }

}
