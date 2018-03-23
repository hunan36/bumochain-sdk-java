package cn.bumo.sdk.core.operation.impl;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.IssueAsset;
import cn.bumo.blockchain.adapter3.Chain;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.AbstractBcOperation;
import cn.bumo.sdk.core.operation.builder.BaseBuilder;
import cn.bumo.sdk.core.utils.Assert;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class IssueAssetOperation extends AbstractBcOperation{

    private long amount;
    private String assetCode;
    private IssueAsset issueAsset = new IssueAsset();

    private IssueAssetOperation(){
        super(OperationTypeV3.ISSUE_ASSET.intValue());
    }

    @Override
    protected void buildOperationContinue(Chain.Operation.Builder operation){

        Chain.OperationIssueAsset.Builder operationIssueAsset = Chain.OperationIssueAsset.newBuilder();

        Chain.Asset.Builder asset = Chain.Asset.newBuilder();
        Chain.AssetKey.Builder assetKey = Chain.AssetKey.newBuilder();
        assetKey.setCode(assetCode);

        asset.setKey(assetKey);
        asset.setAmount(amount);
        operationIssueAsset.setCode(assetCode);
        operationIssueAsset.setAmount(amount);

        operation.setIssueAsset(operationIssueAsset);
    }


    public static class Builder extends BaseBuilder<IssueAssetOperation, Builder>{
    	
    	private IssueAsset issueAsset;
        @Override
        protected IssueAssetOperation newOperation(){
        	IssueAssetOperation issueAssetOperation = new IssueAssetOperation();
        	this.issueAsset = issueAssetOperation.issueAsset;
            return issueAssetOperation;
        }

        public Builder buildAmount(long amount) throws SdkException{
            return buildTemplate(() -> {
            	operation.amount = amount;
            	this.issueAsset.setAmount(amount);
            });
        }

        public Builder buildAssetCode(String assetCode) throws SdkException{
            return buildTemplate(() ->{ 
            		operation.assetCode = assetCode;
            		this.issueAsset.setCode(assetCode);
            	});
        }

        @Override
        public void checkPass() throws SdkException{
            Assert.notEmpty(operation.assetCode, SdkError.OPERATION_ERROR_ISSUE_CODE);
            Assert.gteZero(operation.amount, SdkError.OPERATION_ERROR_ISSUE_AMOUNT_ZERO);
        }

    }


	public IssueAsset getIssueAsset() {
		return issueAsset;
	}
    
}
