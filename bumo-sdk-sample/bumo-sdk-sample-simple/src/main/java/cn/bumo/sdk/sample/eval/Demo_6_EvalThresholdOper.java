package cn.bumo.sdk.sample.eval;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.operation.OperationFactory;
import cn.bumo.sdk.core.utils.GsonUtil;
import cn.bumo.sdk.sample.Demo_1_CreateAccount;
import cn.bumo.sdk.sample.NewActResult;
/***
 * 测试评估设置门限
 * @author 布萌
 * @since 2018/3/19 下午3:53.
 *
 */
public class Demo_6_EvalThresholdOper {

	public static void main(String[] args)  throws Exception{
		SDKEngine engine = SDKEngine.getInstance().configSdk();
		operThreshold(engine);
		System.exit(-1);
	}

	public static void operThreshold(SDKEngine engine) throws Exception {
		NewActResult nar = Demo_1_CreateAccount.newActOper(engine);
		if(nar!=null && nar.getTxHash()!=null && !nar.getTxHash().equals("")) {
			
			BlockchainKeyPair user = nar.getKeyPairs().get(0);
			String userSrcAddr = user.getBubiAddress();
			
			System.out.println("=========================设置门限前：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(userSrcAddr)));
			TestTxResult feeRt = engine.getOperationService()
	            .newEvalTransaction(userSrcAddr)
	            .buildAddOperation(OperationFactory.newSetThresholdOperation(OperationTypeV3.CREATE_ACCOUNT, 14))
	            .commit();
			
			System.out.println("评估修改门限花费：" + feeRt.getRealFee());
			
			System.out.println("=========================设置门限后：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(userSrcAddr)));
		}
	}
}
