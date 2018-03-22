package cn.bumo.sdk.core.transaction;

import cn.bumo.sdk.core.transaction.support.MemoryTransactionContentSupport;
import cn.bumo.sdk.core.transaction.support.TransactionContentSupport;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 提供一个带缓冲区的缓存交易容器,默认4000条
 * 由于改造拥有了访问redis的能力，那么通过静态方法访问并不是好的访问方式
 * 但是考虑到兼容问题，这里并没有改造，后续不打算继续维护
 * 并不建议调用者使用此对象,不确保后续维护会重构这部分
 */
public class TransactionContent{

    private static TransactionContentSupport transactionContentSupport = new MemoryTransactionContentSupport();

    public static void put(String hash, Transaction transaction){
        transactionContentSupport.put(hash, transaction);
    }

    public static Transaction get(String hash){
        return transactionContentSupport.get(hash);
    }

    @Deprecated
    public static void changeQueueLen(int queueLen){}


    /**
     * 通过此方法进行切换，支持自定义
     */
    public static void switchSupport(TransactionContentSupport transactionContentSupport){
        TransactionContent.transactionContentSupport = transactionContentSupport;
    }

}
