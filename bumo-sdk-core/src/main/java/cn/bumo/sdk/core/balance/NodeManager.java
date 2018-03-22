package cn.bumo.sdk.core.balance;

import cn.bumo.access.adaptation.blockchain.bc.RpcService;
import cn.bubi.baas.utils.http.agent.HttpServiceAgent;
import cn.bubi.baas.utils.http.agent.ServiceEndpoint;
import cn.bumo.sdk.core.balance.model.RpcServiceConfig;
import cn.bumo.sdk.core.event.handle.AbstractEventHandler;
import cn.bumo.sdk.core.event.message.LedgerSeqEventMessage;
import cn.bumo.sdk.core.event.source.EventSourceEnum;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.transaction.model.HashType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.bumo.access.utils.spring.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 节点管理器，控制所有节点的访问优先级，动态路由
 */
public class NodeManager extends AbstractEventHandler<LedgerSeqEventMessage>{

    private Logger logger = LoggerFactory.getLogger(NodeManager.class);

    private final Object lock = new Object();
    private final Set<String> hosts;
    private volatile String host;
    private volatile long seq;

    private HashType hashType = HashType.SHA256;// 默认sha256

    // 指定一个默认的host作为初始值
    public NodeManager(List<RpcServiceConfig> rpcServiceConfigs) throws SdkException{
        super(EventSourceEnum.LEDGER_SEQ_INCREASE.getEventSource(), LedgerSeqEventMessage.class);
        this.hosts = rpcServiceConfigs.stream().map(RpcServiceConfig:: getHost).collect(Collectors.toSet());

        for (RpcServiceConfig rpcServiceConfig : rpcServiceConfigs) {
            try {
                logger.info("node manager init try host :" + rpcServiceConfig.getHost());
                HttpServiceAgent.clearMemoryCache();
                ServiceEndpoint serviceEndpoint = new ServiceEndpoint(rpcServiceConfig.getHost(), rpcServiceConfig.getPort(), rpcServiceConfig.isHttps());
                RpcService rpcService = HttpServiceAgent.createService(RpcService.class, serviceEndpoint);
                this.seq = rpcService.getLedger().getHeader().getSeq();
                this.host = rpcServiceConfig.getHost();
                this.hashType = HashType.getHashType(rpcService.hello().getHashType());
                break;
            } catch (Exception e) {
                logger.error("node manager init found Exception:", e);
            }
        }

        if (StringUtils.isEmpty(host)) {
            throw new SdkException(SdkError.NODE_MANAGER_INIT_ERROR);
        }
    }

    /**
     * 获得所有节点
     */
    public Set<String> getAllHosts(){
        return Collections.unmodifiableSet(hosts);
    }

    /**
     * 获得最高节点
     */
    public String getLastHost(){
        return host;
    }

    /**
     * 获得最新的seq
     */
    public long getLastSeq(){
        return seq;
    }

    public HashType getCurrentSupportHashType(){
        return hashType;
    }

    /**
     * 通知更新
     */
    public void notifySeqUpdate(String host, long newSeq){
        synchronized (lock) {
            if (seq < newSeq) {
                seq = newSeq;
                this.host = host;
            }
        }
    }

    @Override
    public void processMessage(LedgerSeqEventMessage message){
        notifySeqUpdate(message.getHost(), message.getSeq());
    }

}
