package cn.bumo.access.starter;

import cn.bumo.access.adaptation.blockchain.bc.RpcService;
import cn.bumo.sdk.core.balance.NodeManager;
import cn.bumo.sdk.core.event.bottom.TxFailManager;
import cn.bumo.sdk.core.event.bottom.TxMqHandleProcess;
import cn.bumo.sdk.core.pool.SponsorAccountPoolManager;
import cn.bumo.sdk.core.seq.SequenceManager;
import cn.bumo.sdk.core.seq.redis.RedisClient;
import cn.bumo.sdk.core.spi.BcOperationService;
import cn.bumo.sdk.core.spi.BcOperationServiceImpl;
import cn.bumo.sdk.core.spi.BcQueryService;
import cn.bumo.sdk.core.spi.BcQueryServiceImpl;
import cn.bumo.sdk.core.transaction.TransactionContent;
import cn.bumo.sdk.core.transaction.support.RedisTransactionContentSupport;
import cn.bumo.sdk.core.transaction.support.TransactionContentSupport;
import cn.bumo.sdk.core.transaction.sync.TransactionSyncManager;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
@Configurable
public class BCAutoConfigurationSpi{

    /**
     * 提供操作功能
     */
    @Bean
    @ConditionalOnMissingBean
    public BcOperationService bcOperationService(SequenceManager bcSequenceManager, RpcService bcRpcService,
                                                 TransactionSyncManager bcTransactionSyncManager, NodeManager bcNodeManager,
                                                 TxFailManager txFailManager, SponsorAccountPoolManager sponsorAccountPoolManager,
                                                 BlockchainProperties blockchainProperties, RedisClient bcRedisClient,
                                                 TxMqHandleProcess txMqHandleProcess){
        BcOperationService bcOperationService = new BcOperationServiceImpl(bcSequenceManager, bcRpcService, bcTransactionSyncManager, bcNodeManager, txFailManager, sponsorAccountPoolManager);
        // 这里初始化账户池
        BlockchainProperties.SponsorAccountPoolConfig accountPoolConfig = blockchainProperties.getAccountPool();
//        if (accountPoolConfig.isEnable()) {
//            sponsorAccountPoolManager.initPool(bcOperationService,
//                    accountPoolConfig.getAddress(), accountPoolConfig.getPublicKey(), accountPoolConfig.getPrivateKey(),
//                    accountPoolConfig.getPoolSize(), accountPoolConfig.getFilePath(), accountPoolConfig.getSponsorAccountMark());
//        }
        // 切换到redis support
        if (blockchainProperties.getRedisSeq().isEnable()) {
            TransactionContentSupport redisSupport = new RedisTransactionContentSupport(bcRedisClient, bcOperationService);
            TransactionContent.switchSupport(redisSupport);
        }
        return bcOperationService;
    }

    /**
     * 提供查询功能
     */
    @Bean
    @ConditionalOnMissingBean
    public BcQueryService bcQueryService(RpcService bcRpcService){
        return new BcQueryServiceImpl(bcRpcService);
    }

}
