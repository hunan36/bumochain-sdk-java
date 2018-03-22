package cn.bumo.sdk.sample;

import com.alibaba.fastjson.JSON;

import cn.bumo.access.adaptation.blockchain.bc.request.test.ReqOperation;
import cn.bumo.access.adaptation.blockchain.bc.request.test.ReqSubTransaction;
import cn.bumo.access.adaptation.blockchain.bc.request.test.ReqTransactionJson;
import cn.bumo.access.adaptation.blockchain.bc.request.test.TestTXReq;
import cn.bumo.access.adaptation.blockchain.bc.response.Operation;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.PayCoin;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.sdk.core.config.SDKEngine;

/**
 * 
 * @author 布萌
 * @since 2018/3/16 上午10:48.
 * TODO
 *
 */
public class Demo_8_TestTX {

	public static void main(String[] args) throws Exception {
		SDKEngine engine = SDKEngine.getInstance().configSdk();
		testTX(engine);
	}
	
	public static void testTX(SDKEngine engine) throws Exception {
		try {
			String user1Addr = Demo_1_CreateAccount.getNewActAddr(engine);
			long now = engine.getQueryService().getAccount(user1Addr).getNonce();
			long future = now + 1;
			String user2Addr = Demo_1_CreateAccount.getNewActAddr(engine);
			
			
			PayCoin paycoin = new PayCoin();
			paycoin.setAmount(10000);
			paycoin.setDestAddress(user2Addr);
			
			
			
			ReqOperation oper = new ReqOperation();
			oper.setType(7);
			oper.setPayCoin(paycoin);
			
			Operation[] operations = new Operation[1];
			operations[0] = oper;
			
			ReqSubTransaction rst = new ReqSubTransaction();
			rst.setFee(0);
			rst.setNonce(future);
			rst.setSourceAddress(user1Addr);
			rst.setOperations(operations);
			
			
			ReqTransactionJson rtJson = new ReqTransactionJson();
			rtJson.setReqSubTransaction(rst);
			ReqTransactionJson[] rtJsons = new ReqTransactionJson[1];
			rtJsons[0] = rtJson;
			
			TestTXReq testTxReq = new TestTXReq();
			testTxReq.setItems(rtJsons);
			
			System.out.println(JSON.toJSONString(testTxReq));
			
			TestTxResult testTran = engine.getInstance().getQueryService().testTransaction(testTxReq);
			
			
			System.out.println(JSON.toJSONString(testTran));
		} catch (Exception e) {
		}
	}

}
