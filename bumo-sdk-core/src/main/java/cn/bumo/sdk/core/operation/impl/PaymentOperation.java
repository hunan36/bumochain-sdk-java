package cn.bumo.sdk.core.operation.impl;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.Asset;
import cn.bumo.access.adaptation.blockchain.bc.response.Key;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.Payment;
import cn.bumo.access.utils.spring.StringUtils;
import cn.bumo.blockchain.adapter3.Chain;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.AbstractBcOperation;
import cn.bumo.sdk.core.operation.builder.BaseBuilder;
import cn.bumo.sdk.core.utils.Assert;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 资产转移
 */
public class PaymentOperation extends AbstractBcOperation{
	
	private Payment payment = new Payment();

    private PaymentOperation(){
        super(OperationTypeV3.PAYMENT.intValue());
    }

    @Override
    protected void buildOperationContinue(Chain.Operation.Builder operation){
        Chain.OperationPayment.Builder operationPayment = Chain.OperationPayment.newBuilder();
        operationPayment.setDestAddress(payment.getDestAddress());

        Chain.Asset.Builder asset = Chain.Asset.newBuilder();
        Chain.AssetKey.Builder assetKey = Chain.AssetKey.newBuilder();
        assetKey.setIssuer(payment.getAsset().getKey().getIssuer());
        assetKey.setCode(payment.getAsset().getKey().getCode());
        asset.setKey(assetKey);
        asset.setAmount(payment.getAsset().getAmount());
        operationPayment.setAsset(asset);
        if(!StringUtils.isEmpty(payment.getInput())){
            operationPayment.setInput(payment.getInput());
        }
        operation.setPayment(operationPayment);
    }

    public static class Builder extends BaseBuilder<PaymentOperation, Builder>{
    	private Payment payment;
    	
    	
        @Override
        protected PaymentOperation newOperation(){
        	PaymentOperation paymentOperation = new PaymentOperation();
        	this.payment = paymentOperation.payment;
        	Asset asset = new Asset();
        	Key key = new Key();
        	asset.setKey(key);
        	this.payment.setAsset(asset);
            return paymentOperation;
        }

        public Builder buildTargetAddress(String targetAddress) throws SdkException{
            return buildTemplate(() -> {
            		operation.payment.setDestAddress(targetAddress);
            	});
        }

        public Builder buildAmount(long amount) throws SdkException{
            return buildTemplate(() -> {
            		this.payment.getAsset().setAmount(amount);
            	});
        }

        public Builder buildIssuerAddress(String issuerAddress) throws SdkException{
            return buildTemplate(() -> {
            	payment.getAsset().getKey().setIssuer(issuerAddress);
            });
        }

        public Builder buildAssetCode(String assetCode) throws SdkException{
            return buildTemplate(() ->{ 
            		payment.getAsset().getKey().setCode(assetCode);
            	});
        }

        public Builder buildInput(String input) throws SdkException{
            return buildTemplate(() -> {
                operation.payment.setInput(input);
            });
        }

        @Override
        public void checkPass() throws SdkException{
            Assert.notEmpty(payment.getDestAddress(), SdkError.OPERATION_ERROR_NOT_DESC_ADDRESS);
            Assert.notEmpty(payment.getAsset().getKey().getIssuer(), SdkError.OPERATION_ERROR_ISSUE_SOURCE_ADDRESS);
            Assert.notEmpty(payment.getAsset().getKey().getCode(), SdkError.OPERATION_ERROR_ISSUE_CODE);
            Assert.gtZero(payment.getAsset().getAmount(), SdkError.OPERATION_ERROR_PAYMENT_AMOUNT_ZERO);
        }
    }

	public Payment getPayment() {
		return payment;
	}
    
    

}
