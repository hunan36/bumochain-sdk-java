package cn.bumo.sdk.core.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.RpcService;
import cn.bumo.access.adaptation.blockchain.bc.request.test.ReqOperation;
import cn.bumo.access.adaptation.blockchain.bc.request.test.ReqSubTransaction;
import cn.bumo.access.adaptation.blockchain.bc.request.test.ReqTransactionJson;
import cn.bumo.access.adaptation.blockchain.bc.request.test.TestTXReq;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.operation.BuildConsume;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.operation.impl.IssueAssetOperation;
import cn.bumo.sdk.core.operation.impl.PayCoinOperation;
import cn.bumo.sdk.core.operation.impl.PaymentOperation;
import cn.bumo.sdk.core.operation.impl.SetMetadataOperation;
import cn.bumo.sdk.core.operation.impl.SetSignerWeightOperation;
import cn.bumo.sdk.core.operation.impl.SetThresholdOperation;
import cn.bumo.sdk.core.seq.SequenceManager;
import cn.bumo.sdk.core.utils.Assert;

/**
 * 评估费用交易
 * @author 布萌
 * @since 18/03/16 下午4:42.
 */
public class EvalTransaction{

    private static Logger logger = LoggerFactory.getLogger(EvalTransaction.class);
    
    private final RpcService rpcService;
    private final SequenceManager sequenceManager;
    private ReqSubTransaction subTransaction;
    private ReqTransactionJson reqTransactionJson = new ReqTransactionJson();
    private final ReqTransactionJson[] items = new ReqTransactionJson[1];
    private ReqOperation[] opers = null;
    private final TestTXReq request  = new TestTXReq();
    private String sponsorAddress;

	/**
     * 正常发起人发起
     */
    public EvalTransaction(SequenceManager sequenceManager,RpcService rpcService,String sponsorAddress){
    	this.sequenceManager = sequenceManager;
    	this.rpcService = rpcService;
        this.sponsorAddress = sponsorAddress;
        try {
			buildInit();
		} catch (SdkException e) {
			logger.info("构建子交易失败！");
		}
    }
    
    private final EvalTransaction buildInit() throws SdkException{
        return buildTemplate(() -> {
        	subTransaction= new ReqSubTransaction();
        	subTransaction.setFee(0);
        	subTransaction.setSourceAddress(sponsorAddress);
        	long now = sequenceManager.getSequenceNumber(sponsorAddress);
        	sequenceManager.restore(sponsorAddress, now-1);
        	subTransaction.setNonce(now);
        });
    }

    public EvalTransaction buildAddOperation(BcOperation operation) throws SdkException{
        return buildTemplate(() -> {
            subTransaction.setOperations(createOperArray(operation));
        });
    }
    private ReqOperation[] createOperArray(BcOperation operation) {
    	if (operation != null) {
    		ReqOperation op = new ReqOperation();
    		int now = 0;
    		int future = now + 1;
    		ReqOperation[] temp = new ReqOperation[future];
        	if(opers != null) {
        		now = opers.length;
        		future = now + 1;
        		temp = new ReqOperation[future];
        		System.arraycopy(opers, 0, temp, 0, now);
        	}
        	
        	if(operation instanceof CreateAccountOperation) {
        		CreateAccountOperation createAccountOperation = (CreateAccountOperation)operation;
        		op.setCreateAccount(createAccountOperation.getCreateAccount());
        		op.setType(OperationTypeV3.CREATE_ACCOUNT.intValue());
        	}
        	
        	if(operation instanceof IssueAssetOperation) {
        		IssueAssetOperation issueAssetOperation = (IssueAssetOperation)operation;
        		op.setIssueAsset(issueAssetOperation.getIssueAsset());
        		op.setType(OperationTypeV3.ISSUE_ASSET.intValue());
        	}
        	
        	if(operation instanceof PaymentOperation) {
        		PaymentOperation paymentOperation = (PaymentOperation)operation;
        		op.setPayment(paymentOperation.getPayment());
        		op.setType(OperationTypeV3.PAYMENT.intValue());
        	}
        	
        	if(operation instanceof SetMetadataOperation) {
        		SetMetadataOperation setMetadataOperation = (SetMetadataOperation)operation;
        		op.setSetMetadata(setMetadataOperation.getSetMetadata());
        		op.setType(OperationTypeV3.SET_METADATA.intValue());
        	}
        	
        	if(operation instanceof SetSignerWeightOperation) {
        		SetSignerWeightOperation setThresholdOperation = (SetSignerWeightOperation)operation;
        		op.setSetSignerWeight(setThresholdOperation.getSetSignerWeight());
        		op.setType(OperationTypeV3.SET_SIGNER_WEIGHT.intValue());
        	}
        	
        	if(operation instanceof SetThresholdOperation) {
        		SetThresholdOperation setThresholdOperation = (SetThresholdOperation)operation;
        		op.setSetThreshold(setThresholdOperation.getSetThreshold());
        		op.setType(OperationTypeV3.SET_THRESHOLD.intValue());
        	}
        	
    		if(operation instanceof PayCoinOperation) {
    			PayCoinOperation payCoinOperation = (PayCoinOperation)operation;
    			op.setPayCoin(payCoinOperation.getPayCoin());
    			op.setType(OperationTypeV3.PAY_COIN.intValue());
    		}
    		
    		temp[now] = op;
    		opers = temp;
        	op = null;
        }
    	return opers;
    }
    
    private final EvalTransaction  build() throws SdkException {
    	checkBeforeCommit();
    	reqTransactionJson.setReqSubTransaction(subTransaction);
    	items[0] = reqTransactionJson;
    	request.setItems(items);
    	return this;
    }
    
    private void checkBeforeCommit() throws SdkException{
        Assert.notEmpty(sponsorAddress, SdkError.TRANSACTION_ERROR_SPONSOR);
        Assert.notNull(subTransaction.getOperations(), SdkError.OPERATION_ERROR_TEST_OPER);
    }

    private EvalTransaction buildTemplate(BuildConsume buildConsume) throws SdkException{
        buildConsume.build();
        return this;
    }


    public TestTxResult commit() throws SdkException{
    	build();
    	System.out.println("请求参数："+JSON.toJSONString(request));
    	return rpcService.testTransaction(request);
            
      }
    
    
}
