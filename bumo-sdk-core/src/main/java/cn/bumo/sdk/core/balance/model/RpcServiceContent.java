package cn.bumo.sdk.core.balance.model;

import cn.bumo.access.adaptation.blockchain.bc.RpcService;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class RpcServiceContent{

    private String host;
    private RpcService rpcService;

    public RpcServiceContent(String host, RpcService rpcService){
        this.host = host;
        this.rpcService = rpcService;
    }

    public String getHost(){
        return host;
    }

    public void setHost(String host){
        this.host = host;
    }

    public RpcService getRpcService(){
        return rpcService;
    }

    public void setRpcService(RpcService rpcService){
        this.rpcService = rpcService;
    }
}
