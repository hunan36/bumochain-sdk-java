package cn.bumo.sdk.core.operation.impl;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.Asset;
import cn.bumo.access.adaptation.blockchain.bc.response.Property;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.Payment;
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

    private String targetAddress;
    private long amount;
    private String issuerAddress;
    private String assetCode;

    private PaymentOperation(){
        super(OperationTypeV3.PAYMENT.intValue());
    }

    @Override
    protected void buildOperationContinue(Chain.Operation.Builder operation){
        Chain.OperationPayment.Builder operationPayment = Chain.OperationPayment.newBuilder();
        operationPayment.setDestAddress(targetAddress);

        Chain.Asset.Builder asset = Chain.Asset.newBuilder();
        Chain.AssetKey.Builder assetKey = Chain.AssetKey.newBuilder();
        assetKey.setIssuer(issuerAddress);
        assetKey.setCode(assetCode);
        asset.setKey(assetKey);
        asset.setAmount(amount);
        operationPayment.setAsset(asset);
        operation.setPayment(operationPayment);
    }

    public static class Builder extends BaseBuilder<PaymentOperation, Builder>{
    	private Payment payment;
    	
    	
        @Override
        protected PaymentOperation newOperation(){
        	PaymentOperation paymentOperation = new PaymentOperation();
        	this.payment = paymentOperation.payment;
        	Asset asset = new Asset();
        	Property property = new Property();
        	asset.setProperty(property);
        	this.payment.setAsset(asset);
            return paymentOperation;
        }

        public Builder buildTargetAddress(String targetAddress) throws SdkException{
            return buildTemplate(() -> {
            		operation.targetAddress = targetAddress;
            		operation.payment.setDestAddress(targetAddress);
            	});
        }

        public Builder buildAmount(long amount) throws SdkException{
            return buildTemplate(() -> {
            		operation.amount = amount;
//            		Asset asset = operation.payment.getAsset();
//            		asset.setAmount(amount);
//            		operation.payment.setAsset(asset);
            		this.payment.getAsset().setAmount(amount);
            	});
        }

        public Builder buildIssuerAddress(String issuerAddress) throws SdkException{
            return buildTemplate(() -> {
            	operation.issuerAddress = issuerAddress;
            	//Asset asset = operation.payment.getAsset();
            	//asset.getProperty()
            	payment.getAsset().getProperty().setIssuer(issuerAddress);
            });
        }

        public Builder buildAssetCode(String assetCode) throws SdkException{
            return buildTemplate(() ->{ 
            		operation.assetCode = assetCode;
            		payment.getAsset().getProperty().setCode(assetCode);
            	});
        }

        @Override
        public void checkPass() throws SdkException{
            Assert.notEmpty(operation.targetAddress, SdkError.OPERATION_ERROR_NOT_DESC_ADDRESS);
            Assert.notEmpty(operation.issuerAddress, SdkError.OPERATION_ERROR_ISSUE_SOURCE_ADDRESS);
            Assert.notEmpty(operation.assetCode, SdkError.OPERATION_ERROR_ISSUE_CODE);
            Assert.gtZero(operation.amount, SdkError.OPERATION_ERROR_PAYMENT_AMOUNT_ZERO);
        }
    }

	public Payment getPayment() {
		return payment;
	}
    
    

}
