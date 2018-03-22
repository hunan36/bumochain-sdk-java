package cn.bumo.sdk.core.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.bumo.access.adaptation.blockchain.bc.RpcService;
import cn.bumo.access.utils.PropUtil;
import cn.bumo.sdk.core.balance.NodeManager;
import cn.bumo.sdk.core.balance.RpcServiceLoadBalancer;
import cn.bumo.sdk.core.balance.model.RpcServiceConfig;
import cn.bumo.sdk.core.event.EventBusService;
import cn.bumo.sdk.core.event.SimpleEventBusService;
import cn.bumo.sdk.core.event.bottom.BlockchainMqHandler;
import cn.bumo.sdk.core.event.bottom.TxFailManager;
import cn.bumo.sdk.core.event.bottom.TxMqHandleProcess;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.pool.SponsorAccountPoolManager;
import cn.bumo.sdk.core.pool.defaults.DefaultSponsorAccountConfig;
import cn.bumo.sdk.core.pool.defaults.DefaultSponsorAccountFactory;
import cn.bumo.sdk.core.seq.AbstractSequenceManager;
import cn.bumo.sdk.core.seq.SimpleSequenceManager;
import cn.bumo.sdk.core.seq.redis.DistributedLock;
import cn.bumo.sdk.core.seq.redis.RedisClient;
import cn.bumo.sdk.core.seq.redis.RedisConfig;
import cn.bumo.sdk.core.seq.redis.RedisSequenceManager;
import cn.bumo.sdk.core.spi.BcOperationService;
import cn.bumo.sdk.core.spi.BcOperationServiceImpl;
import cn.bumo.sdk.core.spi.BcQueryService;
import cn.bumo.sdk.core.spi.BcQueryServiceImpl;
import cn.bumo.sdk.core.transaction.TransactionContent;
import cn.bumo.sdk.core.transaction.model.Signature;
import cn.bumo.sdk.core.transaction.support.RedisTransactionContentSupport;
import cn.bumo.sdk.core.transaction.support.TransactionContentSupport;
import cn.bumo.sdk.core.transaction.sync.TransactionSyncManager;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 提供基础配置能力
 */
public class SDKEngine{

    private BcOperationService operationService;
    private BcQueryService queryService;
    private NodeManager nodeManager;
    private RpcService rpcService;
    private EventBusService eventBusService;
    private AbstractSequenceManager sequenceManager;
    private TransactionSyncManager transactionSyncManager;
    private SponsorAccountPoolManager sponsorAccountPoolManager;
    private SDKProperties sdkProperties = new SDKProperties();
    
    volatile private static SDKEngine instance = null;
    private SDKEngine(){}
    public static SDKEngine getInstance() {
    	try {  
    		synchronized(SDKEngine.class){
    			if(instance == null){
    				instance = new SDKEngine();
    			}
    		}
    	} catch(Exception e)  {
    		// ignore ..
    	}
    	return instance;
    }

    public SDKEngine configSdk() throws SdkException{
    	
    	try {
    		sdkProperties = PropUtil.newInstanceByConf( SDKProperties.class,"/config.properties");
		} catch (Exception e) {
			e.printStackTrace();
		}
        // 解析原生配置参数
        List<RpcServiceConfig> rpcServiceConfigList = Stream.of(sdkProperties.getIps().split(","))
                .map(ip -> {
                    if (!ip.contains(":") || ip.length() < 5) {
                        return null;
                    }
                    return new RpcServiceConfig(ip.split(":")[0], Integer.valueOf(ip.split(":")[1]));
                })
                .filter(Objects:: nonNull).collect(Collectors.toList());

        // 1 配置nodeManager
        nodeManager = new NodeManager(rpcServiceConfigList);

        // 2 配置rpcService
        rpcService = new RpcServiceLoadBalancer(rpcServiceConfigList, nodeManager);

        // 3配置内部消息总线
        eventBusService = new SimpleEventBusService();

        // 4 配置mq以及配套设施 可以配置多个节点监听，收到任意监听结果均可处理
        TxFailManager txFailManager = new TxFailManager(rpcService, eventBusService);


        //  配置transactionSyncManager
        transactionSyncManager = new TransactionSyncManager();
        transactionSyncManager.init();

        sponsorAccountPoolManager = new SponsorAccountPoolManager(new DefaultSponsorAccountFactory(new DefaultSponsorAccountConfig(){
            @Override
            public List<BcOperation> provideBcOperations(String address) throws SdkException{
                return super.provideBcOperations(address);
            }

            @Override
            public List<Signature> provideSignature() throws SdkException{
                return super.provideSignature();
            }
        }));

        TxMqHandleProcess mqHandleProcess = new TxMqHandleProcess(txFailManager, eventBusService, transactionSyncManager, sponsorAccountPoolManager);
        for (String uri : sdkProperties.getEventUtis().split(",")) {
            new BlockchainMqHandler(uri, mqHandleProcess, eventBusService).init();
        }

        // 5 配置seqManager
        RedisClient redisClient = null;
        if (sdkProperties.isRedisSeqManagerEnable()) {
            // 使用redis
            RedisConfig redisConfig = new RedisConfig(sdkProperties.getHost(), sdkProperties.getPort(), sdkProperties.getPassword(), sdkProperties.getDatabase());

            List<RedisConfig> redisConfigs = new ArrayList<>();
            redisConfigs.add(redisConfig);
            redisClient = new RedisClient(redisConfigs);
            redisClient.init();
            DistributedLock distributedLock = new DistributedLock(redisClient.getPool());

            sequenceManager = new RedisSequenceManager(rpcService, redisClient, distributedLock);
        } else {
            //        使用内存
            sequenceManager = new SimpleSequenceManager(rpcService);
            sequenceManager.init();
        }


        // 7 配置事件总线
        eventBusService.addEventHandler(nodeManager);
        eventBusService.addEventHandler(txFailManager);
        eventBusService.addEventHandler(sequenceManager);
        eventBusService.addEventHandler(transactionSyncManager);
        eventBusService.addEventHandler(sponsorAccountPoolManager);

        // 8 初始化spi
        BcOperationService operationService = new BcOperationServiceImpl(sequenceManager, rpcService, transactionSyncManager, nodeManager, txFailManager, sponsorAccountPoolManager);
        if (sdkProperties.isAccountPoolEnable()) {
            sponsorAccountPoolManager.initPool(operationService, sdkProperties.getAddress(), sdkProperties.getPublicKey(), sdkProperties.getPrivateKey(), sdkProperties.getSize(), sdkProperties.getPoolFilepath(), sdkProperties.getMark());
        }

        if (sdkProperties.isRedisSeqManagerEnable()) {
            TransactionContentSupport redisSupport = new RedisTransactionContentSupport(redisClient, operationService);
            TransactionContent.switchSupport(redisSupport);
        }

        BcQueryService queryService = new BcQueryServiceImpl(rpcService);

        this.operationService = operationService;
        this.queryService = queryService;
        
        return this;
    }

    public BcOperationService getOperationService(){
        return operationService;
    }

    public BcQueryService getQueryService(){
        return queryService;
    }

    public NodeManager getNodeManager(){
        return nodeManager;
    }

    public RpcService getRpcService(){
        return rpcService;
    }

    public EventBusService getEventBusService(){
        return eventBusService;
    }

    public AbstractSequenceManager getSequenceManager(){
        return sequenceManager;
    }

    public TransactionSyncManager getTransactionSyncManager(){
        return transactionSyncManager;
    }

    public SponsorAccountPoolManager getSponsorAccountPoolManager(){
        return sponsorAccountPoolManager;
    }
	public SDKProperties getSdkProperties() {
		return sdkProperties;
	}
    
}
