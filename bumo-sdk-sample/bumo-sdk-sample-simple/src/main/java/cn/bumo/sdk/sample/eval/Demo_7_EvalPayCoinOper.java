package cn.bumo.sdk.sample.eval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.operation.impl.PayCoinOperation;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.sample.Demo_1_CreateAccount;

/***
 * 评估支付BU币
 * @author 布萌
 * @since 2018/3/19 下午2:58.
 *
 */
public class Demo_7_EvalPayCoinOper {
	private static Logger logger = LoggerFactory.getLogger(Demo_7_EvalPayCoinOper.class);

	public static void main(String[] args) throws Exception {
		SDKEngine engine = SDKEngine.getInstance();
		evalPayCoinOper(engine);
		System.exit(-1);
	}

	public static void evalPayCoinOper(SDKEngine engine)throws Exception {
		String user1Addr = Demo_1_CreateAccount.getNewActAddr(engine);
		String user2Addr = Demo_1_CreateAccount.getNewActAddr(engine);
		String user3Addr = Demo_1_CreateAccount.getNewActAddr(engine);
		
		EvalTransaction transaction = engine.getOperationService().newEvalTransaction(user1Addr);
		
		BcOperation payCoin = new PayCoinOperation.Builder().buildTargetAddress(user2Addr).buildAmount(10000).build();
		BcOperation payCoin2 = new PayCoinOperation.Builder().buildTargetAddress(user3Addr).buildAmount(100000).build();
		
		TestTxResult evalRt = transaction.buildAddOperation(payCoin).buildAddOperation(payCoin2).commit();	
		
		logger.info("评估结果："+ JSON.toJSONString(evalRt));
		System.out.println("评估该交易的实际费用：" + evalRt.getRealFee());
	}
}
