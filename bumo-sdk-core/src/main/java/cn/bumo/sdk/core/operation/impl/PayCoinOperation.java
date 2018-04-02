package cn.bumo.sdk.core.operation.impl;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.PayCoin;
import cn.bumo.access.utils.spring.StringUtils;
import cn.bumo.blockchain.adapter3.Chain;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.AbstractBcOperation;
import cn.bumo.sdk.core.operation.builder.BaseBuilder;
import cn.bumo.sdk.core.utils.Assert;

/**
 * @author 布萌
 * @since 18/03/16 上午10:02.
 * 支付BU币
 */
public class PayCoinOperation extends AbstractBcOperation{
    private PayCoin payCoin = new PayCoin();

    private PayCoinOperation(){
        super(OperationTypeV3.PAY_COIN.intValue());
    }

    @Override
    protected void buildOperationContinue(Chain.Operation.Builder operation){
        Chain.OperationPayCoin.Builder operationPayCoin = Chain.OperationPayCoin.newBuilder();
        operationPayCoin.setDestAddress(payCoin.getDestAddress());
        operationPayCoin.setAmount(payCoin.getAmount());
		if(!StringUtils.isEmpty(payCoin.getInput())){
        	operationPayCoin.setInput(payCoin.getInput());
    	}
        operation.setPayCoin(operationPayCoin);
        
    }

    public static class Builder extends BaseBuilder<PayCoinOperation, Builder>{
    	private PayCoin payCoin;
        @Override
        protected PayCoinOperation newOperation(){
        	PayCoinOperation payCoinOperation = new PayCoinOperation();
        	this.payCoin = payCoinOperation.payCoin;
            return payCoinOperation;
        }

        public Builder buildTargetAddress(String targetAddress) throws SdkException{
            return buildTemplate(() -> {
		            	operation.payCoin.setDestAddress(targetAddress);
            		});
        }

        public Builder buildAmount(long amount) throws SdkException{
            return buildTemplate(() -> {
            			operation.payCoin.setAmount(amount);
            		});
        }
        
        public Builder buildInput(String input) throws SdkException{
            return buildTemplate(() -> {
            			operation.payCoin.setInput(input);
            		});
        }


        @Override
        public void checkPass() throws SdkException{
            Assert.notEmpty(operation.payCoin.getDestAddress(), SdkError.OPERATION_ERROR_NOT_DESC_ADDRESS);
            Assert.gtZero(operation.payCoin.getAmount(), SdkError.OPERATION_ERROR_PAYMENT_COIN_ZERO);
            //Assert.notNull(operation.payCoin.getInput(), SdkError.OPERATION_ERROR_NOT_INPUT);
        }
        
    }
    public PayCoin getPayCoin() {
		return payCoin;
	}
}
