package cn.bumo.sdk.core.balance.model;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class RpcServiceConfig{

    private String host;
    private int port;
    private boolean https = false;

    public RpcServiceConfig(String host, int port){
        this.host = host;
        this.port = port;
    }

    public RpcServiceConfig(String host, int port, boolean https){
        this.host = host;
        this.port = port;
        this.https = https;
    }

    public boolean isHttps(){
        return https;
    }

    public void setHttps(boolean https){
        this.https = https;
    }

    public String getHost(){
        return host;
    }

    public void setHost(String host){
        this.host = host;
    }

    public int getPort(){
        return port;
    }

    public void setPort(int port){
        this.port = port;
    }
}
