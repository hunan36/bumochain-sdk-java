package cn.bumo.sdk.sample;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.operation.OperationFactory;
import cn.bumo.sdk.core.utils.GsonUtil;

public class Demo_6_OperThreshold {

	public static void main(String[] args)  throws Exception{
		SDKEngine engine = SDKEngine.getInstance().configSdk();
		operThreshold(engine);
		System.exit(-1);
	}

	public static void operThreshold(SDKEngine engine) throws Exception {
		NewActResult nar = Demo_1_CreateAccount.newActOper(engine);
		if(nar!=null && nar.getTxHash()!=null && !nar.getTxHash().equals("")) {
			BlockchainKeyPair user = nar.getKeyPairs().get(0);
			System.out.println("=========================设置门限前：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user.getBubiAddress())));
			engine.getOperationService()
            .newTransaction(user.getBubiAddress())
            .buildAddOperation(OperationFactory.newSetThresholdOperation(OperationTypeV3.CREATE_ACCOUNT, 14))
            .buildAddFee(500000)
            .commit(user.getPubKey(), user.getPriKey());
			
			System.out.println("=========================设置门限后：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user.getBubiAddress())));
		}
	}
}
