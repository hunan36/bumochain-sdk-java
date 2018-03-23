package cn.bumo.access.adaptation.blockchain.bc.response;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 资产
 *
 * @author 布萌
 */
public class Asset{
    private long amount;
    private Key key;

    public long getAmount(){
        return amount;
    }

    public void setAmount(long amount){
        this.amount = amount;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }
}
