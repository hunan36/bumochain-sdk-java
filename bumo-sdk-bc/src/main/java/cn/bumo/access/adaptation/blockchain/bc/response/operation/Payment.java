package cn.bumo.access.adaptation.blockchain.bc.response.operation;

import cn.bumo.access.adaptation.blockchain.bc.response.Asset;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * 转移资产
 *
 * @author 布萌
 */
public class Payment{
    private String metadata;
    @JSONField(name = "dest_address")
    private String destAddress;
    private Asset asset;
    private String input;

    public String getMetadata(){
        return metadata;
    }

    public void setMetadata(String metadata){
        this.metadata = metadata;
    }

    public String getDestAddress(){
        return destAddress;
    }

    public void setDestAddress(String destAddress){
        this.destAddress = destAddress;
    }

    public Asset getAsset(){
        return asset;
    }

    public void setAsset(Asset asset){
        this.asset = asset;
    }

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}
    
}
