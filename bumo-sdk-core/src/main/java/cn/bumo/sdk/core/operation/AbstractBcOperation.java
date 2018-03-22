package cn.bumo.sdk.core.operation;

import cn.bumo.blockchain.adapter3.Chain;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.utils.SwallowUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import cn.bumo.access.utils.spring.StringUtils;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public abstract class AbstractBcOperation implements BcOperation{

    private int type;
    private String operationSourceAddress;
    private String operationMetadata;

    protected AbstractBcOperation(int type){
        this.type = type;
    }

    @Override
    public void buildTransaction(Chain.Transaction.Builder builder, long maxSeq) throws SdkException{
        Chain.Operation.Builder operation = builder.addOperationsBuilder();
        operation.setType(Chain.Operation.Type.valueOf(type));
        if (!StringUtils.isEmpty(operationSourceAddress))
            operation.setSourceAddress(operationSourceAddress);
        if (!StringUtils.isEmpty(operationMetadata))
            operation.setMetadata(ByteString.copyFrom(SwallowUtil.getBytes(operationMetadata)));
        buildOperation(operation);
    }

    private void buildOperation(Chain.Operation.Builder operation) throws SdkException{
        buildOperationContinue(operation);
    }

    @Override
    public BcOperation generateOperation(JSONObject originJson){
        // todo 反向生成操作对象，现没有需求，暂不实现
        return null;
    }

    /**
     * 子类继续build
     */
    protected abstract void buildOperationContinue(Chain.Operation.Builder operation);


    public String getOperationSourceAddress(){
        return operationSourceAddress;
    }

    public void setOperationSourceAddress(String operationSourceAddress){
        this.operationSourceAddress = operationSourceAddress;
    }

    public String getOperationMetadata(){
        return operationMetadata;
    }

    public void setOperationMetadata(String operationMetadata){
        this.operationMetadata = operationMetadata;
    }


}
