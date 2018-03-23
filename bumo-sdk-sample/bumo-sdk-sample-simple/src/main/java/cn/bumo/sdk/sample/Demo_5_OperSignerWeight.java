package cn.bumo.sdk.sample;

import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.operation.OperationFactory;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.model.TransactionCommittedResult;
import cn.bumo.sdk.core.utils.GsonUtil;

public class Demo_5_OperSignerWeight {

	public static void main(String[] args) throws Exception{
		SDKEngine engine = SDKEngine.getInstance();
		operSignerWeight(engine);
		System.exit(-1);
	}
	
	public static void operSignerWeight(SDKEngine engine)throws Exception {
		NewActResult nar = Demo_1_CreateAccount.newActOper(engine);
		if(nar!=null && nar.getTxHash()!=null && !nar.getTxHash().equals("")) {
			BlockchainKeyPair user = nar.getKeyPairs().get(0);
			
			System.out.println("=========================设置权限权前：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user.getBubiAddress())));
			
			BcOperation bcOperation = OperationFactory.newSetSignerWeightOperation(20);
			EvalTransaction newAcctEval = engine.getOperationService().newEvalTransaction(user.getBubiAddress());
	        TestTxResult fee = newAcctEval.buildAddOperation(bcOperation).commit();
			
			Transaction setSignerWeightTransaction = engine.getOperationService().newTransaction(user.getBubiAddress());
	        TransactionCommittedResult setSignerWeightResult = setSignerWeightTransaction
	                .buildAddOperation(bcOperation)
	                .buildAddFee(fee.getRealFee())
	                .commit(user.getPubKey(), user.getPriKey());
	        System.out.println("=========================设置权限权重后：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user.getBubiAddress())));
        
		}
	}
}
