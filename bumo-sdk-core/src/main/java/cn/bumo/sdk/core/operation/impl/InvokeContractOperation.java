package cn.bumo.sdk.core.operation.impl;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.blockchain.adapter3.Chain;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.AbstractBcOperation;
import cn.bumo.sdk.core.operation.builder.BaseBuilder;
import cn.bumo.sdk.core.utils.Assert;
import cn.bumo.access.utils.spring.StringUtils;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 调用合约并不是一个独立的操作
 */
public class InvokeContractOperation extends AbstractBcOperation{

    private String destAddress;  // 合约地址
    private String inputData; // 合约执行参数

    private InvokeContractOperation(){
        super(OperationTypeV3.PAYMENT.intValue());
    }// 合约调用使用转帐类型


    @Override
    protected void buildOperationContinue(Chain.Operation.Builder operation){
        Chain.OperationPayment.Builder operationPayment = Chain.OperationPayment.newBuilder();
        operationPayment.setDestAddress(destAddress);
        if (!StringUtils.isEmpty(inputData)) operationPayment.setInput(inputData);
        operation.setPayment(operationPayment);
    }


    public static class Builder extends BaseBuilder<InvokeContractOperation, Builder>{

        @Override
        protected InvokeContractOperation newOperation(){
            return new InvokeContractOperation();
        }

        public Builder buildDestAddress(String destAddress) throws SdkException{
            return buildTemplate(() -> operation.destAddress = destAddress);
        }

        public Builder buildInputData(String inputData) throws SdkException{
            return buildTemplate(() -> operation.inputData = inputData);
        }

        @Override
        public void checkPass() throws SdkException{
            Assert.notEmpty(operation.destAddress, SdkError.OPERATION_ERROR_NOT_CONTRACT_ADDRESS);
        }
    }

}
