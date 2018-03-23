package cn.bumo.sdk.sample;

import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.operation.OperationFactory;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.utils.GsonUtil;
/**
 * 转移资产
 * @author 布萌
 *
 */
public class Demo_3_transfer {
	
	public static void main(String[] args) throws Exception {
		
		SDKEngine engine = SDKEngine.getInstance();
		transferAsset(engine);
		System.exit(-1);
	}

	private static void transferAsset(SDKEngine engine) throws Exception {
		IssueActResult iar = Demo_2_IssueAsset.issueAsset(engine);
		
		
		NewActResult nar = Demo_1_CreateAccount.newActOper(engine);
		
		if(nar != null && nar.getTxHash() != null && !nar.getTxHash().equals("")) {
			//账户1 ： 转移方的账户地址 
			String user1Addr = iar.getIssueKeyPair().getBubiAddress();
			//账户2：接受方的账户地址
			String user2Addr = nar.getKeyPairs().get(0).getBubiAddress();
			
			System.out.println("=========================发行资产前账户1：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user1Addr)));
			System.out.println("=========================发行资产前账户2：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user2Addr)));
			BcOperation bcOperation = OperationFactory.newPaymentOperation(user2Addr, user1Addr, "600001", 1);
			EvalTransaction newAcctEval = engine.getOperationService().newEvalTransaction(user1Addr);
	        TestTxResult fee = newAcctEval.buildAddOperation(bcOperation).commit();
			Transaction transferTransaction = engine.getOperationService().newTransaction(user1Addr);
			
			transferTransaction
		        .buildAddOperation(bcOperation)
		        .buildAddFee(fee.getRealFee())
		        .buildAddSigner(iar.getIssueKeyPair().getPubKey(), iar.getIssueKeyPair().getPriKey())
		        .commit();
			
			System.out.println("=========================发行资产前账户1：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user1Addr)));
			System.out.println("=========================发行资产前账户2：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user2Addr)));
		
		
		}
	}

}
