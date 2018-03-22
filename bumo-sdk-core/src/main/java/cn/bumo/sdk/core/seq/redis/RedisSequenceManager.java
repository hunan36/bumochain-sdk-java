package cn.bumo.sdk.core.seq.redis;

import cn.bumo.access.adaptation.blockchain.bc.RpcService;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.adaptation.blockchain.exception.BlockchainError;
import cn.bumo.sdk.core.event.message.TransactionExecutedEventMessage;
import cn.bumo.sdk.core.event.source.EventSourceEnum;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.seq.AbstractSequenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.bumo.access.utils.spring.StringUtils;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 基于redis管理seq，实现集群共享
 */
public class RedisSequenceManager extends AbstractSequenceManager{

    private final Logger logger = LoggerFactory.getLogger(RedisSequenceManager.class);

    private RpcService rpcService;
    private RedisClient redisClient;
    private DistributedLock distributedLock;

    public RedisSequenceManager(RpcService rpcService, RedisClient redisClient, DistributedLock distributedLock){
        super(EventSourceEnum.TRANSACTION_NOTIFY.getEventSource(), TransactionExecutedEventMessage.class);
        this.rpcService = rpcService;
        this.redisClient = redisClient;
        this.distributedLock = distributedLock;
    }

    @Override
    public long getSequenceNumber(String address) throws SdkException{
        String lockIdentifier = distributedLock.lockWithTimeout(address, 60 * 1000, 30 * 1000);
        if (StringUtils.isEmpty(lockIdentifier)) {
            throw new SdkException(SdkError.REDIS_ERROR_LOCK_TIMEOUT);
        }

        try {
            Long currentSeq = redisClient.getSeq(address);
            if (currentSeq == null) {
                currentSeq = getSeqByAddress(address);
            }

            long useSeq = currentSeq + 1;
            redisClient.setSeq(address, useSeq);

            return useSeq;
        } finally {
            distributedLock.releaseLock(address, lockIdentifier);
        }
    }

    private long getSeqByAddress(String address) throws SdkException{
        Account account = rpcService.getAccount(address);
        if (account == null) {
            throw new SdkException(BlockchainError.TARGET_NOT_EXIST);
        }
        return account.getNonce();
    }

    @Override
    public void reset(String address){
        redisClient.deleteSeq(address);
    }

    /**
     * 实际上这个处理通知应该查询hash之后确认再删除address对应的seq
     * 但是如果这么做，网络通信会产生很大的延迟时间差，如果间隔期有其他线程获取该address对应的seq
     * 那么可能会出现seq错误问题，具体还要看这个hash到底生成没有来确认
     * 现在就采用直接清理缓存，那么只要保证清理的够快，时间窗口很小，一定程度上解决上述问题
     * 这个问题目前并没有完美的解决方案
     * todo
     */
    @Override
    public void processMessage(TransactionExecutedEventMessage message){
        if (!message.getSuccess()) {
            reset(message.getSponsorAddress());
        }
    }

	@Override
	public void restoreBack(String address,long oldVal) {
		redisClient.setSeq(address, oldVal);
	}
}
