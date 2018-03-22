package cn.bumo.sdk.core.event.message;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class LedgerSeqEventMessage implements EventMessage{

    private String host;// 节点host

    private long seq;// 当前seq


    public String getHost(){
        return host;
    }

    public void setHost(String host){
        this.host = host;
    }

    public long getSeq(){
        return seq;
    }

    public void setSeq(long seq){
        this.seq = seq;
    }
}
