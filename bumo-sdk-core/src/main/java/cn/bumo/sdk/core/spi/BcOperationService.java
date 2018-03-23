package cn.bumo.sdk.core.spi;

import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.model.TransactionSerializable;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 区块链操作服务
 */
public interface BcOperationService{

    /**
     * 使用账户池开启一笔交易
     *
     * @see cn.bubi.sdk.core.operation.OperationFactory 操作工厂
     */
//    Transaction newTransactionByAccountPool();

    /**
     * 开启一笔交易
     *
     * @param sponsorAddress 发起人
     * @see cn.bubi.sdk.core.operation.OperationFactory 操作工厂
     */
    Transaction newTransaction(String sponsorAddress);

    /**
     * 继续一笔交易
     *
     * @param transactionSerializable 序列化对象
     */
    Transaction continueTransaction(TransactionSerializable transactionSerializable);
    /***
     * 创建一个评估交易操作
     * @return
     */
    EvalTransaction newEvalTransaction(String sponsorAddress);
    

}
