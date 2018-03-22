package cn.bumo.sdk.sample.eval;

import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.impl.IssueAssetOperation;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.utils.GsonUtil;
import cn.bumo.sdk.sample.Demo_1_CreateAccount;
import cn.bumo.sdk.sample.NewActResult;
/***
 * 发行资产
 * @author 布萌
 *
 */
public class Demo_2_EvalIssueAssetOper {
	
	public static void main(String[] args) throws Exception {
		
		SDKEngine engine = SDKEngine.getInstance().configSdk();
		evalIssueAsset(engine);
		System.exit(-1);
	}

	
	public static void evalIssueAsset(SDKEngine engine) {
		try {
			NewActResult newActResult = Demo_1_CreateAccount.newActOper(engine);
			String newActAddr = newActResult.getKeyPairs().get(0).getBubiAddress();
			
			Account beforeIssueAssetAct = engine.getQueryService().getAccount(newActAddr);
			
			System.out.println("发行资产前的新建账户信息：\n " + GsonUtil.toJson(beforeIssueAssetAct));
			

			EvalTransaction issueTransaction = engine.getOperationService().newEvalTransaction(newActAddr);
			
			TestTxResult feeResult = issueTransaction
			        .buildAddOperation(new IssueAssetOperation.Builder().buildAmount(500).buildAssetCode("600001").build())
			        .commit();
	
			
			
			System.out.println("发行资产评估费用：\n " + feeResult.getRealFee());
			
		} catch (SdkException e) {
			e.printStackTrace();
		}
		
	}
}
