package cn.bumo.sdk.core.transaction.model;

import cn.bumo.access.utils.io.ByteBlob;

import java.io.Serializable;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class TransactionBlob implements Serializable{

    private static final long serialVersionUID = -2993044303975412391L;
    private String hash;
    private ByteBlob bytesBlob;

    public TransactionBlob(byte[] bytes, HashType hashType){
        this.bytesBlob = ByteBlob.wrap(bytes);
        this.hash = hashType.hash2Hex(bytes);
    }

    public String getHash(){
        return hash;
    }

    public ByteBlob getBytes(){
        return bytesBlob;
    }

    public String getHex(){
        return bytesBlob.toHexString();
    }

}
