package cn.bumo.sdk.core.operation;

import cn.bumo.blockchain.adapter3.Chain;
import cn.bumo.sdk.core.exception.SdkException;
import com.alibaba.fastjson.JSONObject;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public interface BcOperation{

    /**
     * 整合操作
     */
    void buildTransaction(Chain.Transaction.Builder builder, long lastSeq) throws SdkException;

    /**
     * 反序列化操作
     */
    BcOperation generateOperation(JSONObject originJson);


}
