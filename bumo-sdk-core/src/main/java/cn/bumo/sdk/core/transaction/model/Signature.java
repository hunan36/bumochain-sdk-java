package cn.bumo.sdk.core.transaction.model;

import java.io.Serializable;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class Signature implements Serializable{

    private static final long serialVersionUID = -2116313967723283193L;
    private String publicKey;// 公钥
    private String privateKey;// 私钥


    public Signature(String publicKey, String privateKey){
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getPublicKey(){
        return publicKey;
    }

    public void setPublicKey(String publicKey){
        this.publicKey = publicKey;
    }

    public String getPrivateKey(){
        return privateKey;
    }

    public void setPrivateKey(String privateKey){
        this.privateKey = privateKey;
    }
}
