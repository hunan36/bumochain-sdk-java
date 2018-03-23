package cn.bumo.sdk.core.transaction;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
//import com.google.protobuf.InvalidProtocolBufferException;

import cn.bubi.baas.utils.encryption.BubiKey3;
import cn.bubi.baas.utils.http.HttpServiceException;
import cn.bumo.access.adaptation.blockchain.bc.RpcService;
import cn.bumo.access.adaptation.blockchain.bc.request.SignatureRequest;
import cn.bumo.access.adaptation.blockchain.bc.request.SubTransactionRequest;
import cn.bumo.access.adaptation.blockchain.bc.request.TransactionRequest;
import cn.bumo.access.adaptation.blockchain.exception.BlockchainException;
import cn.bumo.access.utils.io.ByteBlob;
import cn.bumo.access.utils.security.ShaUtils;
import cn.bumo.blockchain.adapter3.Chain;
import cn.bumo.sdk.core.balance.NodeManager;
import cn.bumo.sdk.core.event.bottom.TxFailManager;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.operation.BuildConsume;
import cn.bumo.sdk.core.pool.SponsorAccount;
import cn.bumo.sdk.core.seq.SequenceManager;
import cn.bumo.sdk.core.transaction.model.Digest;
import cn.bumo.sdk.core.transaction.model.Signature;
import cn.bumo.sdk.core.transaction.model.TransactionBlob;
import cn.bumo.sdk.core.transaction.model.TransactionCommittedResult;
import cn.bumo.sdk.core.transaction.model.TransactionSerializable;
import cn.bumo.sdk.core.transaction.sync.AsyncFutureTx;
import cn.bumo.sdk.core.transaction.sync.TransactionSyncManager;
import cn.bumo.sdk.core.utils.Assert;
import cn.bumo.sdk.core.utils.SwallowUtil;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 表示一笔交易执行,并不能表示事务
 * 线程不安全，多个线程不要共享一个交易操作
 */
public class Transaction{

    private static Logger logger = LoggerFactory.getLogger(Transaction.class);

    public static final int LOW_FINAL_NOTIFY_SEQ_OFFSET = 10;
    public static final int MID_FINAL_NOTIFY_SEQ_OFFSET = 20;
    public static final int HIGHT_FINAL_NOTIFY_SEQ_OFFSET = 30;

    private String sponsorAddress;// 发起人
    private long nonce;// 生成blob时再去获取
    // 调用方可以自己设定seq偏移量，一个区块偏移量对应的时间为3秒或一分钟,建议区间[2,30]
    private int finalNotifySeqOffset = MID_FINAL_NOTIFY_SEQ_OFFSET;
    private List<BcOperation> operationList = new ArrayList<>();// 操作列表
    private List<Signature> signatures = new ArrayList<>();// 签名列表
    private List<Digest> digests = new ArrayList<>();// 签名摘要列表
    private TransactionBlob transactionBlob;// blob信息

    private String txMetadata;// 交易metadata

    private final SequenceManager sequenceManager;
    private final RpcService rpcService;
    private final TransactionSyncManager transactionSyncManager;
    private final NodeManager nodeManager;
    private final TxFailManager txFailManager;

    private boolean complete = false;
    
    private long fee;
    
    

    public void setFee(long fee) {
		this.fee = fee;
	}

	/**
     * 正常发起人发起
     */
    public Transaction(String sponsorAddress, SequenceManager sequenceManager, RpcService rpcService, TransactionSyncManager transactionSyncManager, NodeManager nodeManager, TxFailManager txFailManager){
        this.sponsorAddress = sponsorAddress;
        this.sequenceManager = sequenceManager;
        this.rpcService = rpcService;
        this.transactionSyncManager = transactionSyncManager;
        this.nodeManager = nodeManager;
        this.txFailManager = txFailManager;
    }

    /**
     * 通过账户池发起
     */
    public Transaction(SponsorAccount sponsorAccount, SequenceManager sequenceManager, RpcService rpcService, TransactionSyncManager transactionSyncManager, NodeManager nodeManager, TxFailManager txFailManager){
        this(sponsorAccount.getAddress(), sequenceManager, rpcService, transactionSyncManager, nodeManager, txFailManager);
        this.signatures.add(new Signature(sponsorAccount.getPublicKey(), sponsorAccount.getPrivateKey()));
    }

    /**
     * 从blob继续交易
     */
    public Transaction(TransactionSerializable transactionSerializable, SequenceManager sequenceManager, RpcService rpcService, TransactionSyncManager transactionSyncManager, NodeManager nodeManager, TxFailManager txFailManager){
        this("", sequenceManager, rpcService, transactionSyncManager, nodeManager, txFailManager);
        this.transactionBlob = transactionSerializable.getTransactionBlob();
        if (transactionSerializable.getSignatures() != null && !transactionSerializable.getSignatures().isEmpty())
            this.signatures = transactionSerializable.getSignatures();
    }

    public Transaction buildAddSigner(String publicKey, String privateKey) throws SdkException{
        return buildTemplate(() -> this.signatures.add(new Signature(publicKey, privateKey)));
    }
    /***
     * TODO
     * @param fee
     * @return
     * @throws SdkException
     */
    public Transaction buildAddFee(long fee) throws SdkException{
        return buildTemplate(() -> this.setFee(fee));
    }
    
    public Transaction buildAddDigest(String publicKey, byte[] originDigest) throws SdkException{
        return buildTemplate(() -> digests.add(new Digest(publicKey, originDigest)));
    }

    public Transaction buildFinalNotifySeqOffset(int offset) throws SdkException{
        return buildTemplate(() -> finalNotifySeqOffset = offset);
    }

    public Transaction buildAddOperation(BcOperation operation) throws SdkException{
        return buildTemplate(() -> {
            if (operation != null) {
                operationList.add(operation);
            }
        });
    }

    public Transaction buildTxMetadata(String txMetadata) throws SdkException{
        return buildTemplate(() -> this.txMetadata = txMetadata);
    }

    private Transaction buildTemplate(BuildConsume buildConsume) throws SdkException{
        checkCanExecute();
        buildConsume.build();
        return this;
    }

    private void checkCanExecute() throws SdkException{
        Assert.notTrue(complete, SdkError.TRANSACTION_ERROR_STATUS);
    }

    /**
     * 只有一个签名的快速访问方法
     */
    public TransactionCommittedResult commit(String publicKey, String privateKey) throws SdkException{
        buildAddSigner(publicKey, privateKey);
        return commit();
    }

    public TransactionCommittedResult commit() throws SdkException{
        return commit(true);
    }

    public TransactionCommittedResult commit(boolean sync) throws SdkException{
        if (transactionBlob == null) {
            generateBlob();
        }
        return submit(sync);
    }

    /**
     * 通过交易也可以重置发起人seq
     */
    public void resetSponsorAddress(){
        sequenceManager.reset(sponsorAddress);
    }

    /**
     * 针对blob需要前端签名所提供出的blob获得方法
     */
    public TransactionBlob generateBlob() throws SdkException{
        checkGeneratorBlobStatus();
        nonce = sequenceManager.getSequenceNumber(sponsorAddress);
        transactionBlob = generateTransactionBlob();
        return transactionBlob;
    }

    public TransactionSerializable forSerializable(){
        return new TransactionSerializable(transactionBlob, signatures);
    }

    public TransactionBlob getTransactionBlob() throws SdkException{
        Assert.notTrue(transactionBlob == null, SdkError.TRANSACTION_ERROR_BLOB_NOT_NULL);
        return transactionBlob;
    }

    private TransactionCommittedResult submit(boolean sync) throws SdkException{
        complete();
        checkCommitStatus();
        TransactionCommittedResult committedResult = new TransactionCommittedResult();
        String txHash = transactionBlob.getHash();
        logger.debug("提交交易txHash=" + txHash);
        AsyncFutureTx txFuture = new AsyncFutureTx(txHash);
        try {
            transactionSyncManager.addAsyncFutureTx(txFuture);

            try {
                SubTransactionRequest subTransactionRequest = getSubTransactionRequest();
                verifyPre(subTransactionRequest);
                rpcService.submitTransaction(subTransactionRequest);
            } catch (RuntimeException e) {
                // 同步重置seq
                sequenceManager.reset(sponsorAddress);
                if (e instanceof HttpServiceException && e.getCause() instanceof BlockchainException) {
                    throw new SdkException((BlockchainException) e.getCause());
                }
                throw e;
            }

            if (sync) {

                txFuture.await();

                if (!success(txFuture.getErrorCode())) {
                    throw new SdkException(Integer.valueOf(txFuture.getErrorCode()), txFuture.getErrorMessage());
                }

            }

            committedResult.setHash(txHash);
        } catch (Exception e) {
            try {
                throw e;
            } catch (InterruptedException e1) {
                logger.error("submit transaction found InterruptedException:", e1);
            }
        } finally {
            transactionSyncManager.remove(txFuture);
        }

        return committedResult;

    }

    private boolean success(String errorCode){
        String success = "0";
        return errorCode == null || success.equals(errorCode);
    }


    private TransactionBlob generateTransactionBlob() throws SdkException{
    	
        Chain.Transaction.Builder builder = Chain.Transaction.newBuilder();
        if (txMetadata != null) {
            builder.setMetadata(ByteString.copyFromUtf8(Hex.encodeHexString(txMetadata.getBytes())));
        }
        builder.setSourceAddress(sponsorAddress);
        builder.setNonce(nonce);
        builder.setFee(fee);
        

        long specifiedSeq = nodeManager.getLastSeq() + finalNotifySeqOffset;
        logger.debug("specified seq:" + specifiedSeq);

        for (BcOperation bcOperation : operationList) {
            bcOperation.buildTransaction(builder, specifiedSeq);
        }

        cn.bumo.blockchain.adapter3.Chain.Transaction transaction = builder.build();
        logger.debug("transaction:" + transaction);
        byte[] bytesBlob = transaction.toByteArray();
        
        TransactionBlob transactionBlob = new TransactionBlob(bytesBlob, nodeManager.getCurrentSupportHashType());

        // 设置最长等待超时通知
        txFailManager.finalNotifyFailEvent(specifiedSeq, transactionBlob.getHash(), SdkError.TRANSACTION_ERROR_TIMEOUT);

        return transactionBlob;
    }

    private SubTransactionRequest getSubTransactionRequest() throws SdkException{
        TransactionRequest tranRequest = new TransactionRequest();
        tranRequest.setSignatures(getSignatures(transactionBlob.getBytes()));
        tranRequest.setTransactionBlob(transactionBlob.getHex());
        TransactionRequest[] transactionRequests = new TransactionRequest[1];
        transactionRequests[0] = tranRequest;
        SubTransactionRequest subTranRequest = new SubTransactionRequest();
        subTranRequest.setItems(transactionRequests);
        return subTranRequest;
    }

    private SignatureRequest[] getSignatures(ByteBlob byteBlob) throws SdkException{
        List<SignatureRequest> signatureRequests = new ArrayList<>();
        for (Signature signature : signatures) {
            SignatureRequest signatureRequest = new SignatureRequest();
            signatureRequest.setPublicKey(signature.getPublicKey());
            SwallowUtil.swallowException(() -> {
                signatureRequest.setSignData(Hex.encodeHexString(ShaUtils.signV3(byteBlob.toBytes(), signature.getPrivateKey(), signature.getPublicKey())));// 将签名信息转换为16进制
            }, SdkError.SIGNATURE_ERROR_PUBLIC_PRIVATE);
            signatureRequests.add(signatureRequest);
        }
        digests.forEach(digest -> {
            SignatureRequest signatureRequest = new SignatureRequest();
            signatureRequest.setPublicKey(digest.getPublicKey());
            signatureRequest.setSignData(Hex.encodeHexString(digest.getOriginDigest()));// // 将签名信息转换为16进制
            signatureRequests.add(signatureRequest);
        });
        return signatureRequests.toArray(new SignatureRequest[signatureRequests.size()]);
    }

    private void verifyPre(SubTransactionRequest subTransactionRequest) throws SdkException{
        TransactionRequest transactionRequest = subTransactionRequest.getItems()[0];
        for (SignatureRequest signatureRequest : transactionRequest.getSignatures()) {
            SwallowUtil.swallowException(() -> {
                boolean thisSignatureResult = BubiKey3.verify(transactionBlob.getBytes().toBytes(), Hex.decodeHex(signatureRequest.getSignData().toCharArray()), signatureRequest.getPublicKey());
                Assert.isTrue(thisSignatureResult, SdkError.EVENT_ERROR_SIGNATURE_VERIFY_FAIL);
            }, SdkError.EVENT_ERROR_SIGNATURE_VERIFY_FAIL);
        }
    }

    private void complete() throws SdkException{
        Assert.notTrue(complete, SdkError.TRANSACTION_ERROR_STATUS);
        complete = true;
    }

    private void checkGeneratorBlobStatus() throws SdkException{
        Assert.notEmpty(sponsorAddress, SdkError.TRANSACTION_ERROR_SPONSOR);
        Assert.isNull(transactionBlob, SdkError.TRANSACTION_ERROR_BLOB_REPEAT_GENERATOR);
        Assert.notTrue(operationList.isEmpty(), SdkError.TRANSACTION_ERROR_OPERATOR_NOT_EMPTY);
        Assert.notTrue(complete, SdkError.TRANSACTION_ERROR_STATUS);
    }

    private void checkCommitStatus() throws SdkException{
        Assert.notTrue(signatures.isEmpty() && digests.isEmpty(), SdkError.TRANSACTION_ERROR_SIGNATURE);
        Assert.checkCollection(signatures, signature -> Assert.notEmpty(signature.getPublicKey(), SdkError.TRANSACTION_ERROR_PUBLIC_KEY_NOT_EMPTY));
        Assert.checkCollection(signatures, signature -> Assert.notEmpty(signature.getPrivateKey(), SdkError.TRANSACTION_ERROR_PRIVATE_KEY_NOT_EMPTY));
        Assert.notTrue(transactionBlob == null, SdkError.TRANSACTION_ERROR_BLOB_NOT_NULL);
        Assert.isTrue(complete, SdkError.TRANSACTION_ERROR_STATUS);
        Assert.notTrue(fee == 0, SdkError.TRANSACTION_ERROR_FEE_NOT_ZERO);
    }
}
