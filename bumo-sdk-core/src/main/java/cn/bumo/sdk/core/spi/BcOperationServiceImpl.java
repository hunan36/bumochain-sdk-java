package cn.bumo.sdk.core.spi;

import cn.bumo.access.adaptation.blockchain.bc.RpcService;
import cn.bumo.sdk.core.balance.NodeManager;
import cn.bumo.sdk.core.event.bottom.TxFailManager;
import cn.bumo.sdk.core.pool.SponsorAccountPoolManager;
import cn.bumo.sdk.core.seq.SequenceManager;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.model.TransactionSerializable;
import cn.bumo.sdk.core.transaction.sync.TransactionSyncManager;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class BcOperationServiceImpl implements BcOperationService{

    private SequenceManager sequenceManager;
    private RpcService rpcService;
    private TransactionSyncManager transactionSyncManager;
    private NodeManager nodeManager;
    private TxFailManager txFailManager;
    private SponsorAccountPoolManager sponsorAccountPoolManager;

    public BcOperationServiceImpl(SequenceManager sequenceManager, RpcService rpcService, TransactionSyncManager transactionSyncManager, NodeManager nodeManager, TxFailManager txFailManager, SponsorAccountPoolManager sponsorAccountPoolManager){
        this.sequenceManager = sequenceManager;
        this.rpcService = rpcService;
        this.transactionSyncManager = transactionSyncManager;
        this.nodeManager = nodeManager;
        this.txFailManager = txFailManager;
        this.sponsorAccountPoolManager = sponsorAccountPoolManager;
    }

    @Override
    public Transaction newTransactionByAccountPool(){
        return new Transaction(sponsorAccountPoolManager.getRichSponsorAccount(), sequenceManager, rpcService, transactionSyncManager, nodeManager, txFailManager);
    }

    @Override
    public Transaction newTransaction(String sponsorAddress){
        return new Transaction(sponsorAddress, sequenceManager, rpcService, transactionSyncManager, nodeManager, txFailManager);
    }

    @Override
    public Transaction continueTransaction(TransactionSerializable transactionSerializable){
        return new Transaction(transactionSerializable, sequenceManager, rpcService, transactionSyncManager, nodeManager, txFailManager);
    }

	@Override
	public EvalTransaction newEvalTransaction(String sponsorAddress) {
		return new EvalTransaction(sequenceManager,rpcService,sponsorAddress);
	}
    
    

}
