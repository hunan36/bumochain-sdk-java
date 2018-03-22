package cn.bumo.sdk.sample;


import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
/***
 * 
 * @author 布萌
 *
 */
public class IssueActResult {
	//发行人的KeyPair 可能存在问题 仅仅测试使用
	private BlockchainKeyPair issueKeyPair = null;
	//发行资产交易hash
	private String txHash = null;
	
	
	public String getTxHash() {
		return txHash;
	}
	public IssueActResult setTxHash(String txHash) {
		this.txHash = txHash;
		return this;
	}
	
	public static IssueActResult newIssueActResult() {
		return new IssueActResult();
	}
	public BlockchainKeyPair getIssueKeyPair() {
		return issueKeyPair;
	}
	public IssueActResult setIssueKeyPair(BlockchainKeyPair issueKeyPair) {
		this.issueKeyPair = issueKeyPair;
		return this;
		
	}
	
}
