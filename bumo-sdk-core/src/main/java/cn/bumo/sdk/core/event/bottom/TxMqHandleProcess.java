package cn.bumo.sdk.core.event.bottom;

import cn.bumo.sdk.core.event.EventBusService;
import cn.bumo.sdk.core.event.message.TransactionExecutedEventMessage;
import cn.bumo.sdk.core.event.source.EventSourceEnum;
import cn.bumo.sdk.core.pool.SponsorAccountPoolManager;
import cn.bumo.sdk.core.transaction.sync.TransactionSyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 针对收到重复通知以及确认错误结果做的处理器,多个监听线程应共享一个后置处理器
 */
public class TxMqHandleProcess{

    private final Logger logger = LoggerFactory.getLogger(TxMqHandleProcess.class);

    private LimitQueue<String> successQueue = new LimitQueue<>(300);
    // 前置失败队列，去重错误通知
    private LimitQueue<String> failQueue = new LimitQueue<>(300);
    private final Object lock = new Object();

    private TxFailManager txFailManager;
    private EventBusService eventBusService;
    private TransactionSyncManager transactionSyncManager;
    private SponsorAccountPoolManager sponsorAccountPoolManager;

    public TxMqHandleProcess(TxFailManager txFailManager, EventBusService eventBusService, TransactionSyncManager transactionSyncManager, SponsorAccountPoolManager sponsorAccountPoolManager){
        this.txFailManager = txFailManager;
        this.eventBusService = eventBusService;
        this.transactionSyncManager = transactionSyncManager;
        this.sponsorAccountPoolManager = sponsorAccountPoolManager;
    }

    void process(TransactionExecutedEventMessage executedEventMessage){

        String txHash = executedEventMessage.getHash();

        if (!transactionSyncManager.isLocalData(txHash)) {
            // 对于异步调用,直接释放address
            sponsorAccountPoolManager.notifyRecover(executedEventMessage.getSponsorAddress());
            return;
        }

        // 去重，过滤
        synchronized (lock) {
            txFailManager.addFailEventMessage(executedEventMessage);

            // 队列存在，则直接返回
            if (successQueue.exist(txHash)) {
                logger.debug("successQueue exist txHash : " + txHash + " , ignore.");
                return;
            }
            if (failQueue.exist(txHash)) {
                logger.debug("failQueue exist txHash : " + txHash + " , ignore.");
                return;
            }

            if (!executedEventMessage.getSuccess()) {
                if (notRepeatErrorCode(executedEventMessage.getErrorCode())) {
                    failQueue.offer(txHash);
                    txFailManager.notifyFailEvent(executedEventMessage);
                }
                return;
            }

            // 成功，入成功队列，发通知
            successQueue.offer(txHash);
        }

        eventBusService.publishEvent(EventSourceEnum.TRANSACTION_NOTIFY.getEventSource(), executedEventMessage);
    }

    private boolean notRepeatErrorCode(String errorCode){
        return !(TxFailManager.REPEAT_RECEIVE == Long.valueOf(errorCode));
    }

}
