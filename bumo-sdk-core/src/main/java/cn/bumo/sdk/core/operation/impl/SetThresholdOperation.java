package cn.bumo.sdk.core.operation.impl;

import java.util.ArrayList;
import java.util.List;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.TypeThreshold;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.SetThreshold;
import cn.bumo.blockchain.adapter3.Chain;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.AbstractBcOperation;
import cn.bumo.sdk.core.operation.builder.BaseBuilder;
import cn.bumo.sdk.core.utils.Assert;

/**
 * @author 布萌
 * @since 18/03/19 下午3:44.添加设置门限的对象
 * 
 */
public class SetThresholdOperation extends AbstractBcOperation{

    private static final long UNMODIFIED = -1;// 如果不想修改需要设置为-1

    private long txThreshold = UNMODIFIED;
    private List<TypeThreshold> typeThresholds = new ArrayList<>();// 0删除
    //### new Add @18/03/19 下午3:44.
    private SetThreshold setThreshold = new SetThreshold(); 

    private SetThresholdOperation(){
        super(OperationTypeV3.SET_THRESHOLD.intValue());
    }

    @Override
    protected void buildOperationContinue(Chain.Operation.Builder operation){
        Chain.OperationSetThreshold.Builder operationSetThreshold = Chain.OperationSetThreshold.newBuilder();
        operationSetThreshold.setTxThreshold(txThreshold);

        typeThresholds.forEach(typeThreshold -> {
            Chain.OperationTypeThreshold.Builder typeThresholdBuilder = Chain.OperationTypeThreshold.newBuilder();
            typeThresholdBuilder
                    .setType(Chain.Operation.Type.forNumber(Integer.valueOf("" + typeThreshold.getType())));
            typeThresholdBuilder.setThreshold(typeThreshold.getThreshold());
            operationSetThreshold.addTypeThresholds(typeThresholdBuilder);
        });

        operation.setSetThreshold(operationSetThreshold);
    }


    public static class Builder extends BaseBuilder<SetThresholdOperation, Builder>{
    	private SetThreshold setThreshold;
        @Override
        protected SetThresholdOperation newOperation(){
        	SetThresholdOperation setThresholdOperation = new SetThresholdOperation();
        	this.setThreshold = setThresholdOperation.setThreshold;
            return setThresholdOperation;
        }


        public Builder buildTxThreshold(long txThreshold) throws SdkException{
            return buildTemplate(() -> 
            	{
            		operation.txThreshold = txThreshold;
            		operation.setThreshold.setTxThreshold(txThreshold);
            	});
        }

        public Builder buildAddTypeThreshold(OperationTypeV3 type, long threshold) throws SdkException{
            return buildTemplate(() -> {
                Assert.notNull(type, SdkError.OPERATION_ERROR_TX_THRESHOLD_TYPE_NOT_NULL);
                operation.typeThresholds.add(new TypeThreshold(type.intValue(), threshold));
                
                //添加到对象中
                TypeThreshold[] a = new TypeThreshold[operation.typeThresholds.size()];
                operation.typeThresholds.toArray(a);
                operation.setThreshold.setTypeThresholds(a);
            });
        }

        @Override
        public void checkPass() throws SdkException{
            Assert.notTrue(operation.txThreshold == UNMODIFIED && operation.typeThresholds.isEmpty(), SdkError.OPERATION_ERROR_SET_THRESHOLD);
            Assert.gteExpect(operation.txThreshold, UNMODIFIED, SdkError.OPERATION_ERROR_TX_THRESHOLD_LT_ZERO);
            Assert.checkCollection(operation.typeThresholds, typeThreshold -> Assert.gteZero(typeThreshold.getThreshold(), SdkError.OPERATION_ERROR_TX_THRESHOLD_TYPE_LT_ZERO));
        }

    }


	public SetThreshold getSetThreshold() {
		return setThreshold;
	}
    
}
