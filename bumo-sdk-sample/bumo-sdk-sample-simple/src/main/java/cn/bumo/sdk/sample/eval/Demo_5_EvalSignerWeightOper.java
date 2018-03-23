package cn.bumo.sdk.sample.eval;

import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.operation.OperationFactory;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.sample.Demo_1_CreateAccount;
import cn.bumo.sdk.sample.NewActResult;

public class Demo_5_EvalSignerWeightOper {

	public static void main(String[] args) throws Exception{
		SDKEngine engine = SDKEngine.getInstance();
		operSignerWeight(engine);
		System.exit(-1);
	}
	
	public static void operSignerWeight(SDKEngine engine)throws Exception {
		NewActResult nar = Demo_1_CreateAccount.newActOper(engine);
		if(nar!=null && nar.getTxHash()!=null && !nar.getTxHash().equals("")) {
			BlockchainKeyPair user = nar.getKeyPairs().get(0);
			String userSrcAddr = user.getBubiAddress();
			EvalTransaction evalTran = engine.getOperationService().newEvalTransaction(userSrcAddr);
			TestTxResult evalRt = evalTran
	                .buildAddOperation(OperationFactory.newSetSignerWeightOperation(20))
	                .commit();
			
			System.out.println("添加权限的操作评估费用：" + evalRt.getRealFee());
        
		}
	}
}
