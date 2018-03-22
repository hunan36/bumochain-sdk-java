package cn.bumo.sdk.core.operation.impl;

import java.util.ArrayList;
import java.util.List;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.common.Signer;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.SetSignerWeight;
import cn.bumo.blockchain.adapter3.Chain;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.AbstractBcOperation;
import cn.bumo.sdk.core.operation.builder.BaseBuilder;
import cn.bumo.sdk.core.utils.Assert;

/**
 * @author 布萌
 * @since 18/03/19 下午4:23.
 * 添加设置权重
 */
public class SetSignerWeightOperation extends AbstractBcOperation{

    private static final long UNMODIFIED = -1;// 如果不想修改需要设置为-1

    private long masterWeight = UNMODIFIED;
    private List<Signer> signers = new ArrayList<>();// 0删除
    
    //###添加设置权重 18/03/19 下午4:23.
    private SetSignerWeight setSignerWeight = new SetSignerWeight();

    private SetSignerWeightOperation(){
        super(OperationTypeV3.SET_SIGNER_WEIGHT.intValue());
    }

    @Override
    protected void buildOperationContinue(Chain.Operation.Builder operation){
        Chain.OperationSetSignerWeight.Builder operationSetSignerWeight = Chain.OperationSetSignerWeight.newBuilder();
        operationSetSignerWeight.setMasterWeight(masterWeight);
        signers.forEach(signer -> {
            Chain.Signer.Builder sign = Chain.Signer.newBuilder();
            sign.setAddress(signer.getAddress());
            sign.setWeight(signer.getWeight());
            operationSetSignerWeight.addSigners(sign);
        });

        operation.setSetSignerWeight(operationSetSignerWeight);
    }


    public static class Builder extends BaseBuilder<SetSignerWeightOperation, Builder>{
    	private SetSignerWeight setSignerWeight ;
        @Override
        protected SetSignerWeightOperation newOperation(){
        	SetSignerWeightOperation setSignerWeightOperation = new SetSignerWeightOperation();
        	this.setSignerWeight = setSignerWeightOperation.setSignerWeight;
            return setSignerWeightOperation;
        }

        public Builder buildMasterWeight(long masterWeight) throws SdkException{
            return buildTemplate(() -> {
            		operation.masterWeight = masterWeight;
            		operation.setSignerWeight.setMasterWeight(masterWeight);
            	});
        }

        public Builder buildAddSigner(String address, long weight) throws SdkException{
            return buildTemplate(() -> {
            		operation.signers.add(new Signer(address, weight));
            		Signer[] signers = new Signer[operation.signers.size()];
            		operation.signers.toArray(signers);
            		
            		operation.setSignerWeight.setSigners(signers);
            		
            	});
        }

        @Override
        public void checkPass() throws SdkException{
            Assert.notTrue(operation.masterWeight == UNMODIFIED && operation.signers.isEmpty(), SdkError.OPERATION_ERROR_SET_SIGNER_WEIGHT);
            Assert.gteExpect(operation.masterWeight, UNMODIFIED, SdkError.OPERATION_ERROR_MASTER_WEIGHT_LT_ZERO);
            Assert.checkCollection(operation.signers, signer -> {
                Assert.notEmpty(signer.getAddress(), SdkError.OPERATION_ERROR_SET_SIGNER_ADDRESS_NOT_EMPTY);
                Assert.gteZero(signer.getWeight(), SdkError.OPERATION_ERROR_SINGER_WEIGHT_LT_ZERO);
            });
        }

    }


	public SetSignerWeight getSetSignerWeight() {
		return setSignerWeight;
	}
    
    
}
