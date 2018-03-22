package cn.bumo.sdk.core.balance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
//import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bubi.baas.utils.http.HttpServiceException;
import cn.bubi.baas.utils.http.agent.HttpServiceAgent;
import cn.bubi.baas.utils.http.agent.ServiceEndpoint;
import cn.bumo.access.adaptation.blockchain.bc.RpcService;
import cn.bumo.access.adaptation.blockchain.bc.request.SubTransactionRequest;
import cn.bumo.access.adaptation.blockchain.bc.request.test.TestTXReq;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.adaptation.blockchain.bc.response.Hello;
import cn.bumo.access.adaptation.blockchain.bc.response.TransactionHistory;
import cn.bumo.access.adaptation.blockchain.bc.response.converter.ServiceResponse;
import cn.bumo.access.adaptation.blockchain.bc.response.ledger.Ledger;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.SetMetadata;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.access.adaptation.blockchain.exception.BlockchainException;
import cn.bumo.sdk.core.balance.model.RpcServiceConfig;
import cn.bumo.sdk.core.balance.model.RpcServiceContent;
import cn.bumo.sdk.core.exception.ExceptionUtil;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.exec.ExecutorsFactory;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 负载访问底层节点
 * 负载策略：取最高区块节点进行访问
 */
public class RpcServiceLoadBalancer implements RpcService{

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceLoadBalancer.class);

    private RpcService rpcServiceProxy;

    public RpcServiceLoadBalancer(List<RpcServiceConfig> rpcServiceConfigs, NodeManager nodeManager){
        if (rpcServiceConfigs == null || rpcServiceConfigs.isEmpty())
            throw new BlockchainException("Origin RpcServiceConfig at least one!！");

        List<RpcServiceContent> rpcServiceContents = rpcServiceConfigs.stream().map(rpcServiceConfig -> {
            HttpServiceAgent.clearMemoryCache();
            ServiceEndpoint serviceEndpoint = new ServiceEndpoint(rpcServiceConfig.getHost(), rpcServiceConfig.getPort(), rpcServiceConfig.isHttps());
            RpcService rpcService = HttpServiceAgent.createService(RpcService.class, serviceEndpoint);
            return new RpcServiceContent(rpcServiceConfig.getHost(), rpcService);
        }).collect(Collectors.toList());

        this.rpcServiceProxy = (RpcService) Proxy.newProxyInstance(RpcServiceLoadBalancer.class.getClassLoader(), new Class[] {RpcService.class},
                new RpcServiceInterceptor(rpcServiceContents, nodeManager));
    }

    @Override
    public Account getAccount(String address){
        return rpcServiceProxy.getAccount(address);
    }

    @Override
    public Hello hello(){
        return rpcServiceProxy.hello();
    }

    @Override
    public SetMetadata getAccountMetadata(String address, String key){
        return rpcServiceProxy.getAccountMetadata(address, key);
    }

    @Override
    public Ledger getLedger(){
        return rpcServiceProxy.getLedger();
    }

    @Override
    public Ledger getLedgerBySeq(long seq){
        return rpcServiceProxy.getLedgerBySeq(seq);
    }

    @Override
    public String submitTransaction(SubTransactionRequest request){
        return rpcServiceProxy.submitTransaction(request);
    }

    @Override
    public TransactionHistory getTransactionHistoryByAddress(String address){
        return rpcServiceProxy.getTransactionHistoryByAddress(address);
    }

    @Override
    public TransactionHistory getTransactionHistoryBySeq(Long seq, int start, int limit){
        return rpcServiceProxy.getTransactionHistoryBySeq(seq, start, limit);
    }

    @Override
    public TransactionHistory getTransactionHistoryByHash(String hash){
        return rpcServiceProxy.getTransactionHistoryByHash(hash);
    }

    @Override
    public ServiceResponse getTransactionResultByHash(String hash){
        return rpcServiceProxy.getTransactionResultByHash(hash);
    }

    private class RpcServiceInterceptor implements InvocationHandler{

        private Map<String, RpcService> hostRpcMapping = new HashMap<>();
        private NodeManager nodeManager;


        RpcServiceInterceptor(List<RpcServiceContent> originRpcServiceContent, NodeManager nodeManager){
            originRpcServiceContent.forEach(rpcServiceContent -> this.hostRpcMapping.put(rpcServiceContent.getHost(), rpcServiceContent.getRpcService()));
            this.nodeManager = nodeManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
            Set<String> usedHosts = new HashSet<>();
            String firstHost = nodeManager.getLastHost();
            usedHosts.add(firstHost);

            Object result;
            String useHost = firstHost;

            while (true) {
                try {
                    result = doInvoke(useHost, method, args);
                    break;
                } catch (Throwable t) {
                    useHost = processThrowable(usedHosts, useHost, t);
                }
            }

            return result;
        }

        private String processThrowable(final Set<String> usedHosts, String useHost, Throwable t) throws Throwable{
        	cn.bumo.access.utils.spring.Assert.notNull(t, "Throwable must not null!");
            

            if (needTryAgain(t)) {

                Set<String> allHosts = new HashSet<>(nodeManager.getAllHosts());
                usedHosts.forEach(allHosts:: remove);

                if (allHosts.iterator().hasNext()) {
                    String nowHost = allHosts.iterator().next();
                    LOGGER.error("route host " + useHost + " error. now switch to host : " + nowHost + " , usedHosts : " + usedHosts);
                    LOGGER.error("router fail error:", t);
                    usedHosts.add(nowHost);
                    return nowHost;
                }
            }

            throw t;
        }

        private boolean needTryAgain(Throwable t){
            return t instanceof HttpServiceException && t.getCause() != null && !(t.getCause() instanceof BlockchainException);
        }

        private Object doInvoke(String useHost, Method method, Object[] args) throws Throwable{
            try {
                // 访问底层节点
                RpcService rpcService = hostRpcMapping.get(useHost);
                if (rpcService == null) {
                    LOGGER.warn("router host : " + useHost + " ,but hostRpcMapping not found. hostRpcMapping keys:" + hostRpcMapping.keySet());
                    throw new SdkException(SdkError.EVENT_ERROR_ROUTER_HOST_FAIL);
                }

                LOGGER.info("load balance call rpc service router host : " + useHost + " , method : " + method.getName());

                // 增加了超时连接时间控制
                Future future = ExecutorsFactory.getExecutorService().submit(() -> method.invoke(rpcService, args));
                return future.get(30, TimeUnit.SECONDS);
                // todo 使用同步提交看一下性能
//                return method.invoke(rpcService, args);
            } catch (Exception e) {
                if (e instanceof TimeoutException) {
                    throw new SdkException(SdkError.RPC_INVOKE_ERROR_TIMEOUT);
                }
                throw ExceptionUtil.unwrapThrowable(e);
            }

        }
    }

	@Override
	public TestTxResult testTransaction(TestTXReq request) {
		return rpcServiceProxy.testTransaction(request);
	}

}
