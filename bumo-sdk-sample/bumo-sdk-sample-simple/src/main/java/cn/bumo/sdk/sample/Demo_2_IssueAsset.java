package cn.bumo.sdk.sample;

import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.operation.impl.IssueAssetOperation;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.model.TransactionCommittedResult;
import cn.bumo.sdk.core.utils.GsonUtil;
/***
 * 发行资产
 * @author 布萌
 *
 */
public class Demo_2_IssueAsset {
	
	public static void main(String[] args) throws Exception {
		SDKEngine engine = SDKEngine.getInstance();
		issueAsset(engine);
		System.exit(-1);
	}

	
	public static IssueActResult issueAsset(SDKEngine engine) {
		try {
			NewActResult newActResult = Demo_1_CreateAccount.newActOper(engine);
			String newActAddr = newActResult.getKeyPairs().get(0).getBubiAddress();
			System.out.println("发行资产前的新建账户信息：\n " + GsonUtil.toJson(engine.getQueryService().getAccount(newActAddr)));
			
			
			BcOperation bcOperation = new IssueAssetOperation.Builder().buildAmount(500).buildAssetCode("600001").build();
			EvalTransaction newAcctEval = engine.getOperationService().newEvalTransaction(engine.getSdkProperties().getAddress());
	        TestTxResult fee = newAcctEval.buildAddOperation(bcOperation).commit();
			
			Transaction issueTransaction = engine.getOperationService().newTransaction(newActAddr);
			
			TransactionCommittedResult tcr = issueTransaction
			        .buildAddOperation(bcOperation)
			        .buildAddFee(fee.getRealFee())
			        .buildAddSigner(newActResult.getKeyPairs().get(1).getPubKey(), newActResult.getKeyPairs().get(1).getPriKey())
			        .buildAddSigner(newActResult.getKeyPairs().get(2).getPubKey(), newActResult.getKeyPairs().get(2).getPriKey())
			        .commit();
			
			System.out.println("发行资产后的新建账户信息：\n " + GsonUtil.toJson(engine.getQueryService().getAccount(newActAddr)));
			
			return  IssueActResult.newIssueActResult().setTxHash(tcr.getHash()).setIssueKeyPair(newActResult.getKeyPairs().get(0));
		} catch (SdkException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
