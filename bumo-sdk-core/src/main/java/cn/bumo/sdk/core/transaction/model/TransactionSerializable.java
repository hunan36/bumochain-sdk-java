package cn.bumo.sdk.core.transaction.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class TransactionSerializable implements Serializable{

    private static final long serialVersionUID = 5370283195105108177L;

    private TransactionBlob transactionBlob;
    private List<Signature> signatures;
    private Long fee;

    public TransactionSerializable(){
    }

    public TransactionSerializable(TransactionBlob transactionBlob, List<Signature> signatures, long fee){
        this.transactionBlob = transactionBlob;
        this.signatures = signatures;
        this.fee = fee;

    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public TransactionBlob getTransactionBlob(){
        return transactionBlob;
    }

    public void setTransactionBlob(TransactionBlob transactionBlob){
        this.transactionBlob = transactionBlob;
    }

    public List<Signature> getSignatures(){
        return signatures;
    }

    public void setSignatures(List<Signature> signatures){
        this.signatures = signatures;
    }
}
