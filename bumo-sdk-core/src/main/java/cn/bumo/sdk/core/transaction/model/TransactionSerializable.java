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

    public TransactionSerializable(){
    }

    public TransactionSerializable(TransactionBlob transactionBlob, List<Signature> signatures){
        this.transactionBlob = transactionBlob;
        this.signatures = signatures;
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
