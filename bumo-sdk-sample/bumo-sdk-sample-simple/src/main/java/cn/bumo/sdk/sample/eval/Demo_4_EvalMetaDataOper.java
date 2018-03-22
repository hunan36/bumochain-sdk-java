package cn.bumo.sdk.sample.eval;

import cn.bumo.access.adaptation.blockchain.bc.response.operation.SetMetadata;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.operation.OperationFactory;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.sample.Demo_1_CreateAccount;
import cn.bumo.sdk.sample.NewActResult;

public class Demo_4_EvalMetaDataOper {

	public static void main(String[] args) throws Exception {
		SDKEngine engine = SDKEngine.getInstance().configSdk();
		operMetaData(engine);
		System.exit(-1);
	}

	
	public static void operMetaData(SDKEngine engine) throws Exception  {
		
		NewActResult nar = Demo_1_CreateAccount.newActOper(engine);
		
		if(nar!=null && nar.getTxHash()!=null && !nar.getTxHash().equals("")) {
			BlockchainKeyPair user = nar.getKeyPairs().get(0);
			String userSrcAddr = user.getBubiAddress();
			
			EvalTransaction mataDataEval = engine.getOperationService().newEvalTransaction(userSrcAddr);
			TestTxResult evalRt  = mataDataEval.buildAddOperation(OperationFactory.newSetMetadataOperation("key1", "value1")).commit();
			System.out.println("设置元数据的评估费用："+ evalRt.getRealFee());
	        
	        SetMetadata setMetadata = engine.getQueryService().getAccount(user.getBubiAddress(), "key1");
            setMetadata.setValue("这是新设置的value1");
            mataDataEval = engine.getOperationService().newEvalTransaction(userSrcAddr);
            mataDataEval.buildAddOperation(OperationFactory.newUpdateSetMetadataOperation(setMetadata)).commit();
            System.out.println("修改元数据的评估费用："+ evalRt.getRealFee());
		}
	}
}
