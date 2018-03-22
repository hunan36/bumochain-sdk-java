package cn.bumo.access.utils.transfer;


import cn.bumo.access.utils.concurrent.AsyncFuture;

/**
 * AsyncMessageSendable 是对异步发送操作的抽象；
 *
 * @param <TData>
 * @author 布萌
 */
public interface AsyncSendable<TSender, TData>{

    /**
     * 异步发送消息；
     *
     * @param message
     * @return
     */
    public AsyncFuture<TSender> asyncSend(TData message);

}
