package cn.bumo.sdk.sample;

import java.util.ArrayList;
import java.util.List;

import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
/***
 * 
 * @author daikai
 *
 */
public class NewActResult {
	//元素[0]存放的是要生成的KeyPair 其他位置都是签名列表中的KeyPair
	private List<BlockchainKeyPair> keyPairs = null; 
	//交易hash值
	private String txHash = null;
	
	public NewActResult() {
		keyPairs = new ArrayList<>(); 
	}
	public List<BlockchainKeyPair> getKeyPairs() {
		return keyPairs;
	}
	public NewActResult setKeyPairs(List<BlockchainKeyPair> keyPairs) {
		this.keyPairs = keyPairs;
		return this;
	}
	
	public NewActResult addKeyPair(BlockchainKeyPair keyPair) {
		this.keyPairs.add(keyPair);
		return this;
	}
	public String getTxHash() {
		return txHash;
	}
	public NewActResult setTxHash(String txHash) {
		this.txHash = txHash;
		return this;
	}
	
	public static NewActResult newCreateAccountResult() {
		return new NewActResult();
	}
	
	
}
