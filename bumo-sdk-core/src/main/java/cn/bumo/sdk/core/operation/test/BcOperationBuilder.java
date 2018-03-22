package cn.bumo.sdk.core.operation.test;

import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.operation.impl.PayCoinOperation;

/**
 * @author 布萌
 * @since 18/03/16 上午10:02.
 * 
 */
@Deprecated
public class BcOperationBuilder {
	/**
	 * 1.创建账户
	 * @return
	 */
	public static CreateAccountOperation.Builder newCreateAccountBuilder() {
		return new CreateAccountOperation.Builder();
	}
	/***
	 * 7.支付BU币的操作
	 * @return
	 */
    public static PayCoinOperation.Builder newPayCoinBuilder() {
    	return new PayCoinOperation.Builder();
    }

}
