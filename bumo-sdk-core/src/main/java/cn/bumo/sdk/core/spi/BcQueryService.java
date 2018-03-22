package cn.bumo.sdk.core.spi;

import cn.bumo.access.adaptation.blockchain.bc.request.test.TestTXReq;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.adaptation.blockchain.bc.response.TransactionHistory;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.SetMetadata;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 区块链查询服务
 */
public interface BcQueryService{

    /**
     * 获得账户信息
     *
     * @param address 账户地址
     */
    Account getAccount(String address);

    /**
     * 获得指定账户metadata的value
     *
     * @param address 账户地址
     * @param key     metadata的key
     */
    SetMetadata getAccount(String address, String key);

    /**
     * 获得交易历史
     *
     * @param hash txHash
     */

    TransactionHistory getTransactionHistoryByHash(String hash);

    /**
     * TODO 评估费用
     * @author 布萌
     * @since 18/03/16 下午3:44. 
     * @param request
     * @return
     */
    TestTxResult testTransaction(TestTXReq request);
    
    
}
