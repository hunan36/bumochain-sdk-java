package cn.bumo.access.starter;

import cn.bumo.access.adaptation.blockchain.bc.RpcService;
import cn.bumo.sdk.core.balance.NodeManager;
import cn.bumo.sdk.core.balance.RpcServiceLoadBalancer;
import cn.bumo.sdk.core.balance.model.RpcServiceConfig;
import cn.bumo.sdk.core.event.EventBusService;
import cn.bumo.sdk.core.event.SimpleEventBusService;
import cn.bumo.sdk.core.event.bottom.BlockchainMqHandler;
import cn.bumo.sdk.core.event.bottom.TxFailManager;
import cn.bumo.sdk.core.event.bottom.TxMqHandleProcess;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.pool.SponsorAccountConfig;
import cn.bumo.sdk.core.pool.SponsorAccountFactory;
import cn.bumo.sdk.core.pool.SponsorAccountPoolManager;
import cn.bumo.sdk.core.pool.defaults.DefaultSponsorAccountConfig;
import cn.bumo.sdk.core.pool.defaults.DefaultSponsorAccountFactory;
import cn.bumo.sdk.core.seq.AbstractSequenceManager;
import cn.bumo.sdk.core.seq.SequenceManager;
import cn.bumo.sdk.core.seq.SimpleSequenceManager;
import cn.bumo.sdk.core.seq.redis.DistributedLock;
import cn.bumo.sdk.core.seq.redis.RedisClient;
import cn.bumo.sdk.core.seq.redis.RedisSequenceManager;
import cn.bumo.sdk.core.transaction.sync.TransactionSyncManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 自动配置类，详细类说明参考simple项目的config类
 */
@Configuration
@EnableConfigurationProperties(BlockchainProperties.class)
public class BCAutoConfiguration{

    /**
     * 配置组件
     */
    @Bean
    public SponsorAccountPoolManager sponsorAccountPoolManager(SponsorAccountFactory sponsorAccountFactory, EventBusService eventBusService){
        SponsorAccountPoolManager sponsorAccountPoolManager = new SponsorAccountPoolManager(sponsorAccountFactory);
        eventBusService.addEventHandler(sponsorAccountPoolManager);
        return sponsorAccountPoolManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public SponsorAccountConfig sponsorAccountConfig(){
        return new DefaultSponsorAccountConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public SponsorAccountFactory sponsorAccountFactory(SponsorAccountConfig sponsorAccountConfig){
        return new DefaultSponsorAccountFactory(sponsorAccountConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public NodeManager bcNodeManager(BlockchainProperties blockchainProperties, EventBusService eventBusService) throws SdkException{
        List<RpcServiceConfig> bcRpcServiceConfigs = blockchainProperties.getNode().converterRpcServiceConfig();
        NodeManager nodeManager = new NodeManager(bcRpcServiceConfigs);
        eventBusService.addEventHandler(nodeManager);
        return nodeManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcService bcRpcService(BlockchainProperties blockchainProperties, NodeManager bcNodeManager) throws SdkException{
        return new RpcServiceLoadBalancer(blockchainProperties.getNode().converterRpcServiceConfig(), bcNodeManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public TxFailManager bcTxFailManager(RpcService bcRpcService, EventBusService eventBusService){
        TxFailManager txFailManager = new TxFailManager(bcRpcService, eventBusService);
        eventBusService.addEventHandler(txFailManager);
        return txFailManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public TxMqHandleProcess bcTxMqHandleProcess(BlockchainProperties blockchainProperties, TxFailManager bcTxFailManager,
                                                 EventBusService eventBusService, TransactionSyncManager transactionSyncManager,
                                                 SponsorAccountPoolManager sponsorAccountPoolManager) throws SdkException{
        TxMqHandleProcess bcTxMqHandleProcess = new TxMqHandleProcess(bcTxFailManager, eventBusService, transactionSyncManager, sponsorAccountPoolManager);

        // 初始化mq监听
        List<String> uris = blockchainProperties.getEvent().converterUri();
        for (String uri : uris) {
            new BlockchainMqHandler(uri, bcTxMqHandleProcess, eventBusService).init();
        }

        return bcTxMqHandleProcess;
    }

    @Bean(initMethod = "init")
    public RedisClient bcRedisClient(BlockchainProperties blockchainProperties){
        if (blockchainProperties.getRedisSeq().isEnable()) {
            return new RedisClient(blockchainProperties.getRedisSeq().getRedis());
        }
        return null;
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public SequenceManager bcSequenceManager(RpcService bcRpcService, EventBusService bcEventBusService, RedisClient bcRedisClient, BlockchainProperties blockchainProperties){
        AbstractSequenceManager sequenceManager;
        if (blockchainProperties.getRedisSeq().isEnable()) {
            DistributedLock distributedLock = new DistributedLock(bcRedisClient.getPool());
            sequenceManager = new RedisSequenceManager(bcRpcService, bcRedisClient, distributedLock);
        } else {
            sequenceManager = new SimpleSequenceManager(bcRpcService);
        }

        bcEventBusService.addEventHandler(sequenceManager);
        return sequenceManager;
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public TransactionSyncManager bcTransactionSyncManager(EventBusService eventBusService){
        TransactionSyncManager transactionSyncManager = new TransactionSyncManager();
        eventBusService.addEventHandler(transactionSyncManager);
        return transactionSyncManager;
    }


    @Bean
    @ConditionalOnMissingBean
    public EventBusService bcEventBusService(){
        return new SimpleEventBusService();
    }

}
