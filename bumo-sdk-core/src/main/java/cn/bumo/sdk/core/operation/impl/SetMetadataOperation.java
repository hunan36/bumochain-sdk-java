package cn.bumo.sdk.core.operation.impl;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.SetMetadata;
import cn.bumo.blockchain.adapter3.Chain;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.AbstractBcOperation;
import cn.bumo.sdk.core.operation.builder.BaseBuilder;
import cn.bumo.sdk.core.utils.Assert;

/**
 * @author 布萌
 * @since 18/03/19 下午4:53.
 * 一次metadata更新只能更新一个
 */
public class SetMetadataOperation extends AbstractBcOperation{

    private SetMetadata setMetadata;

    private SetMetadataOperation(){
        super(OperationTypeV3.SET_METADATA.intValue());
    }

    @Override
    protected void buildOperationContinue(Chain.Operation.Builder operation){
        Chain.OperationSetMetadata.Builder operationSetMetadata = Chain.OperationSetMetadata.newBuilder();
        operationSetMetadata.setKey(setMetadata.getKey());
        operationSetMetadata.setValue(setMetadata.getValue());
        if (setMetadata.getVersion() != 0) {
            operationSetMetadata.setVersion(setMetadata.getVersion() + 1);
        }
        operation.setSetMetadata(operationSetMetadata);
    }

    public static class Builder extends BaseBuilder<SetMetadataOperation, Builder>{

        @Override
        protected SetMetadataOperation newOperation(){
            return new SetMetadataOperation();
        }

        public Builder buildMetadata(SetMetadata setMetadata) throws SdkException{
            return buildTemplate(() -> operation.setMetadata = setMetadata);
        }

        public Builder buildMetadata(String key, String value) throws SdkException{
            return buildTemplate(() -> operation.setMetadata = new SetMetadata(key, value));
        }

        public Builder buildMetadata(String key, String value, long version) throws SdkException{
            return buildTemplate(() -> operation.setMetadata = new SetMetadata(key, value, version));
        }

        @Override
        public void checkPass() throws SdkException{
            Assert.notTrue(operation.setMetadata == null, SdkError.OPERATION_ERROR_SET_METADATA_EMPTY);
        }

    }

	public SetMetadata getSetMetadata() {
		return setMetadata;
	}

    
}
