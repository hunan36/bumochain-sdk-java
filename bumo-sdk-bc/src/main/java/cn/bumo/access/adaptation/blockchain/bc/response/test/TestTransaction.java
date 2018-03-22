package cn.bumo.access.adaptation.blockchain.bc.response.test;

import cn.bumo.access.adaptation.blockchain.bc.response.Transaction;
/***
 * 评估费用
 * @author 布萌
 *
 */
public class TestTransaction extends Transaction {
	private TestTxResult result;

	public TestTxResult getResult() {
		return result;
	}

	public void setResult(TestTxResult result) {
		this.result = result;
	}
	
}
