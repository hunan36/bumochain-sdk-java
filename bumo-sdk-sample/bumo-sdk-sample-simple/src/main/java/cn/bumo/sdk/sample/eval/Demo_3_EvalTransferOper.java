package cn.bumo.sdk.sample.eval;

import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.operation.OperationFactory;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.sample.Demo_1_CreateAccount;
import cn.bumo.sdk.sample.Demo_2_IssueAsset;
import cn.bumo.sdk.sample.IssueActResult;
import cn.bumo.sdk.sample.NewActResult;
/**
 * 转移资产
 * @author 布萌
 *
 */
public class Demo_3_EvalTransferOper {
	
	public static void main(String[] args) throws Exception {
		
		SDKEngine engine = SDKEngine.getInstance();
		transferAsset(engine);
		System.exit(-1);
	}

	private static void transferAsset(SDKEngine engine) throws Exception {
		IssueActResult issueActRt = Demo_2_IssueAsset.issueAsset(engine);
		
		
		NewActResult newActRt = Demo_1_CreateAccount.newActOper(engine);
		
		if(newActRt != null && newActRt.getTxHash() != null && !newActRt.getTxHash().equals("")) {
		
			String user1Addr = issueActRt.getIssueKeyPair().getBubiAddress();
			String user2Addr = newActRt.getKeyPairs().get(0).getBubiAddress();
			
			EvalTransaction evalTransferOper = engine.getOperationService().newEvalTransaction(user1Addr);
			TestTxResult evalRt = evalTransferOper
		        .buildAddOperation(OperationFactory.newPaymentOperation(user2Addr, user1Addr, "600001", 1))
		        .commit();
	
			System.out.println("转移资产的评估费用：" + evalRt.getRealFee());
		
		
		}
	}

}
