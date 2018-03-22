package cn.bumo.access.adaptation.blockchain.bc.response.test;

import com.alibaba.fastjson.annotation.JSONField;
/***
 * 评估费用
 * @author 布萌
 *
 */
public class TestTxResult {
	
	private String hash;
	private String logs;
	@JSONField(name="query_rets")
	private String queryRets;
	@JSONField(name="real_fee")
	private long realFee;
	
	private String stat;
	private TXEnv[] txs;
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getLogs() {
		return logs;
	}
	public void setLogs(String logs) {
		this.logs = logs;
	}
	public String getQueryRets() {
		return queryRets;
	}
	public void setQueryRets(String queryRets) {
		this.queryRets = queryRets;
	}
	public long getRealFee() {
		return realFee;
	}
	public void setRealFee(long realFee) {
		this.realFee = realFee;
	}
	public String getStat() {
		return stat;
	}
	public void setStat(String stat) {
		this.stat = stat;
	}
	public TXEnv[] getTxs() {
		return txs;
	}
	public void setTxs(TXEnv[] txs) {
		this.txs = txs;
	}
	
	
}
