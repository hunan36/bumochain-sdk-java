package cn.bumo.sdk.core.transaction.support;

import cn.bumo.sdk.core.transaction.Transaction;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public interface TransactionContentSupport{

    void put(String hash, Transaction transaction);

    Transaction get(String hash);

}
