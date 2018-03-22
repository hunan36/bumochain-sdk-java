package cn.bumo.sdk.core.event.bottom;

import cn.bumo.access.adaptation.blockchain.bc.RpcService;
import cn.bumo.access.adaptation.blockchain.bc.response.Transaction;
import cn.bumo.access.adaptation.blockchain.bc.response.TransactionHistory;
import cn.bumo.access.adaptation.blockchain.exception.BlockchainError;
import cn.bumo.access.utils.concurrent.NamedThreadFactory;
import cn.bumo.sdk.core.event.EventBusService;
import cn.bumo.sdk.core.event.handle.AbstractEventHandler;
import cn.bumo.sdk.core.event.message.LedgerSeqEventMessage;
import cn.bumo.sdk.core.event.message.TransactionExecutedEventMessage;
import cn.bumo.sdk.core.event.source.EventSourceEnum;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exec.ExecutorsFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.bumo.access.utils.spring.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 交易失败控制器
 */
public class TxFailManager extends AbstractEventHandler<LedgerSeqEventMessage>{

    public static final long SUCCESS = 0;
    public static final long NOT_FOUND = -1;
    public static final long REPEAT_RECEIVE = 3;

    private Map<Long, Set<String>> seqHashMapping = new ConcurrentHashMap<>();// seq-hash映射
    private Map<String, Set<TransactionExecutedEventMessage>> hashMessageMapping = new ConcurrentHashMap<>();// hash-message映射

    private final RpcService rpcService;
    private final EventBusService eventBusService;

    public TxFailManager(RpcService rpcService, EventBusService eventBusService){
        super(EventSourceEnum.LEDGER_SEQ_INCREASE.getEventSource(), LedgerSeqEventMessage.class);
        this.rpcService = rpcService;
        this.eventBusService = eventBusService;
    }

    @Override
    public void processMessage(LedgerSeqEventMessage message){
        notifySeqUpdate(message.getSeq());
    }


    /**
     * 添加所有的失败事件
     */
    void addFailEventMessage(TransactionExecutedEventMessage message){
        Set<TransactionExecutedEventMessage> messageList = hashMessageMapping.computeIfAbsent(message.getHash(), hash -> new HashSet<>());
        messageList.add(message);
    }

    /**
     * 失败通知
     */
    void notifyFailEvent(TransactionExecutedEventMessage executedEventMessage){
    	ExecutorsFactory.getExecutorService().execute(new FailProcessor(rpcService, executedEventMessage));
    }

    /**
     * 指定seq添加失败hash,存储hash-message和seq-hash,由区块增长通知释放线程
     */
    public void finalNotifyFailEvent(long seq, String hash, SdkError sdkError){
        addFailEventMessage(hash, sdkError);
        Set<String> hashSet = seqHashMapping.computeIfAbsent(seq, key -> new HashSet<>());
        hashSet.add(hash);
    }

    private void addFailEventMessage(String hash, SdkError sdkError){
        TransactionExecutedEventMessage message = new TransactionExecutedEventMessage();
        message.setHash(hash);
        message.setSuccess(false);
        message.setErrorCode(String.valueOf(sdkError.getCode()));
        message.setErrorMessage(sdkError.getDescription());
        addFailEventMessage(message);
    }

    /**
     * 区块增长，释放等待线程
     */
    private void notifySeqUpdate(long newSeq){
        releaseWaitSeqThread(newSeq);
    }

    private void releaseWaitSeqThread(long waitNotifySeq){
        Set<String> waitExecutor = seqHashMapping.remove(waitNotifySeq);

        if (waitExecutor != null && !waitExecutor.isEmpty()) {
            waitExecutor.forEach(hash -> ExecutorsFactory.getExecutorService().execute(new FailProcessor(rpcService, hashMessageMapping.remove(hash))));
        }
    }


    /**
     * 失败处理器
     */
    private class FailProcessor implements Runnable{

        private final RpcService rpcService;
        private final Set<TransactionExecutedEventMessage> executedEventMessages;
        private Logger log = LoggerFactory.getLogger(FailProcessor.class);

        private FailProcessor(RpcService rpcService, Set<TransactionExecutedEventMessage> executedEventMessages){
            this.rpcService = rpcService;
            this.executedEventMessages = executedEventMessages == null ? new HashSet<>() : executedEventMessages;
        }

        private FailProcessor(RpcService rpcService, TransactionExecutedEventMessage executedEventMessage){
            this.rpcService = rpcService;
            this.executedEventMessages = new HashSet<>();
            this.executedEventMessages.add(executedEventMessage);
        }

        @Override
        public void run(){
            if (!executedEventMessages.iterator().hasNext()) {
                return;
            }
            TransactionExecutedEventMessage message = executedEventMessages.iterator().next();
            String txHash = message.getHash();
            TransactionHistory transactionHistory = rpcService.getTransactionHistoryByHash(txHash);

            long errorCode = getErrorCode(txHash, transactionHistory);

            // 没有生成交易记录，那么从错误堆中取出最合适的错误信息
            if (errorCode == NOT_FOUND) {
                TransactionExecutedEventMessage failMessage = filterBestMessage();
                eventBusService.publishEvent(EventSourceEnum.TRANSACTION_NOTIFY.getEventSource(), failMessage);
                return;
            }


            // 有交易记录生成，直接取交易记录的状态进行处理
            if (errorCode != SUCCESS) {
                String errorDesc = getErrorDesc(txHash, transactionHistory);
                if (StringUtils.isEmpty(errorDesc)) {
                    errorDesc = BlockchainError.getDescription((int) errorCode);
                    if (StringUtils.isEmpty(errorDesc)) {
                        log.warn("errorCode mapping desc not found , errorCode=" + errorCode);
                    }
                }

                message.setErrorCode(String.valueOf(errorCode));
                message.setErrorMessage(errorDesc);
                eventBusService.publishEvent(EventSourceEnum.TRANSACTION_NOTIFY.getEventSource(), message);
            }

        }

        private TransactionExecutedEventMessage filterBestMessage(){
            // 选出最合适的错误消息，1由于一定会收到errorCode3，那么它的优先级最低,其它错误有就返回
            for (TransactionExecutedEventMessage message : executedEventMessages) {
                if (Long.valueOf(message.getErrorCode()) != REPEAT_RECEIVE) {
                    return message;
                }
            }
            return executedEventMessages.iterator().next();
        }

        private long getErrorCode(String txHash, TransactionHistory transactionHistory){
            if (transactionHistory != null) {
                Transaction[] transactions = transactionHistory.getTransactions();
                if (transactions != null && transactions.length > 0) {
                    Transaction transaction = transactionHistory.getTransactions()[0];
                    log.debug("FailProcessor:check txHash," + txHash + ",result:" + transaction.getErrorCode());
                    if (txHash.equals(transaction.getHash())) {
                        return transaction.getErrorCode();
                    }
                }
            }
            return NOT_FOUND;
        }

        private String getErrorDesc(String txHash, TransactionHistory transactionHistory){
            if (transactionHistory != null) {
                Transaction[] transactions = transactionHistory.getTransactions();
                if (transactions != null && transactions.length > 0) {
                    Transaction transaction = transactionHistory.getTransactions()[0];
                    if (txHash.equals(transaction.getHash())) {
                        return transaction.getErrorDesc();
                    }
                }
            }
            return null;
        }
    }

}
