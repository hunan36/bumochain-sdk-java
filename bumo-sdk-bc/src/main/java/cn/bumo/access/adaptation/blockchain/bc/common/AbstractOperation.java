package cn.bumo.access.adaptation.blockchain.bc.common;

public abstract class AbstractOperation {
	private int type;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
