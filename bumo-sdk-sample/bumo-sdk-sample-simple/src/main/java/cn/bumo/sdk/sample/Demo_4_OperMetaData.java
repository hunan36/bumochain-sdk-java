package cn.bumo.sdk.sample;

import cn.bumo.access.adaptation.blockchain.bc.response.operation.SetMetadata;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.operation.OperationFactory;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.utils.GsonUtil;

public class Demo_4_OperMetaData {

	public static void main(String[] args) throws Exception {
		SDKEngine engine = SDKEngine.getInstance();
		operMetaData(engine);
		System.exit(-1);
	}

	
	public static void operMetaData(SDKEngine engine) throws Exception  {
		
		NewActResult nar = Demo_1_CreateAccount.newActOper(engine);
		
		if(nar!=null && nar.getTxHash()!=null && !nar.getTxHash().equals("")) {
			BlockchainKeyPair user = nar.getKeyPairs().get(0);
			
			BcOperation bcOperation  = OperationFactory.newSetMetadataOperation("key1", "value1");
			EvalTransaction newAcctEval = engine.getOperationService().newEvalTransaction(user.getBubiAddress());
	        TestTxResult fee = newAcctEval.buildAddOperation(bcOperation).commit();
			Transaction newMetadataTransaction = engine.getOperationService().newTransaction(user.getBubiAddress());
	        newMetadataTransaction
	                .buildAddOperation(bcOperation)
	                .buildAddFee(fee.getRealFee())
	                .buildAddSigner(user.getPubKey(), user.getPriKey())
	                .commit();
	        
	        System.out.println("=========================设置元数据：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user.getBubiAddress())));
	        
	        SetMetadata setMetadata = engine.getQueryService().getAccount(user.getBubiAddress(), "key1");
            setMetadata.setValue("这是新设置的value1");
            
            bcOperation  = OperationFactory.newUpdateSetMetadataOperation(setMetadata);
			newAcctEval = engine.getOperationService().newEvalTransaction(user.getBubiAddress());
	        fee = newAcctEval.buildAddOperation(bcOperation).commit();
	        Transaction updateMetadataTransaction = engine.getOperationService().newTransaction(user.getBubiAddress());
            updateMetadataTransaction
                    .buildAddOperation(bcOperation)
                    .buildAddFee(fee.getRealFee())
                    .buildAddSigner(user.getPubKey(), user.getPriKey())
                    .commit();
	        
            System.out.println("=========================修改元数据后：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user.getBubiAddress())));
		}
	}
}
