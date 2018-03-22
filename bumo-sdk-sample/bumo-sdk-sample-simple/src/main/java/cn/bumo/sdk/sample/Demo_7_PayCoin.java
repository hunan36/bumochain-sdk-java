package cn.bumo.sdk.sample;

import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.operation.OperationFactory;
import cn.bumo.sdk.core.utils.GsonUtil;
/**
 * 
 * @author 布萌
 * @since 2018/3/16 上午10:48.
 * 支付BU币
 *
 */
public class Demo_7_PayCoin {

	public static void main(String[] args) throws Exception {
		SDKEngine engine = SDKEngine.getInstance().configSdk();
		payCoin(engine);
		System.exit(-1);
	}

	private static void payCoin(SDKEngine engine)  throws Exception {
		try {
			NewActResult newActResult_1 = Demo_1_CreateAccount.newActOper(engine);
			
			String txHash_1 = newActResult_1.getTxHash();
			if(txHash_1 == null || txHash_1.equals("") ) {
				System.out.println("账户一创建失败");
				return ;
			}
			
			NewActResult newActResult_2 = Demo_1_CreateAccount.newActOper(engine);
			String txHash_2 = newActResult_2.getTxHash();
			if(txHash_2 == null || txHash_2.equals("") ) {
				System.out.println("账户二创建失败");
				return ;
			}
			BlockchainKeyPair user1 = newActResult_1.getKeyPairs().get(0);
			String user1Addr = user1.getBubiAddress();
			String user2Addr = newActResult_2.getKeyPairs().get(0).getBubiAddress();
			
			System.out.println("支付BU币之前：\n");
			System.out.println("账户一：\n " + GsonUtil.toJson(engine.getQueryService().getAccount(user1Addr)));
			System.out.println("账户二：\n " + GsonUtil.toJson(engine.getQueryService().getAccount(user2Addr)));
			//...
			engine.getOperationService()
	            .newTransaction(user1Addr)
	            .buildAddOperation(OperationFactory.newPayCoinOperation(user2Addr, 2000))
	            .buildAddFee(500000)
	            .commit(user1.getPubKey(), user1.getPriKey());
			
			System.out.println("支付BU币之前：\n");
			System.out.println("账户一：\n " + GsonUtil.toJson(engine.getQueryService().getAccount(user1Addr)));
			System.out.println("账户二：\n " + GsonUtil.toJson(engine.getQueryService().getAccount(user2Addr)));
			
		} catch (Exception e) {
		}
	}
}
