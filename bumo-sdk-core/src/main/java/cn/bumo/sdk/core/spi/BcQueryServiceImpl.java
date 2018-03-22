package cn.bumo.sdk.core.spi;

import cn.bumo.access.utils.spring.StringUtils;

import cn.bumo.access.adaptation.blockchain.bc.RpcService;
import cn.bumo.access.adaptation.blockchain.bc.request.test.TestTXReq;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.adaptation.blockchain.bc.response.TransactionHistory;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.SetMetadata;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class BcQueryServiceImpl implements BcQueryService{

    private RpcService rpcService;

    public BcQueryServiceImpl(RpcService rpcService){
        this.rpcService = rpcService;
    }


    @Override
    public Account getAccount(String address){
        if (StringUtils.isEmpty(address)) {
            throw new IllegalArgumentException("query account method address must not null!");
        }
        return rpcService.getAccount(address);
    }

    @Override
    public SetMetadata getAccount(String address, String key){
        if (StringUtils.isEmpty(address) || StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("query account method address and key must not null!");
        }
        return rpcService.getAccountMetadata(address, key);
    }

    @Override
    public TransactionHistory getTransactionHistoryByHash(String hash){
        if (StringUtils.isEmpty(hash)) {
            throw new IllegalArgumentException("query getTransactionHistoryByHash method hash must not null!");
        }
        return rpcService.getTransactionHistoryByHash(hash);
    }

    /**
     * TODO 评估费用
     */
	@Override
	public TestTxResult testTransaction(TestTXReq request) {
		
		//TODO check args
		return rpcService.testTransaction(request);
	}

}
