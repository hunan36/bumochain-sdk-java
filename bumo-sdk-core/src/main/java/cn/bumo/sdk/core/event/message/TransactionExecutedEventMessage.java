package cn.bumo.sdk.core.event.message;

public class TransactionExecutedEventMessage implements EventMessage{

    private String hash;			// 交易hash

    private String sponsorAddress;	// 发起人账户地址

    private long sequenceNumber;	// 交易序列号

    private Boolean success;		// 成功失败标志位

    private String errorCode;		// 交易错误码
    private String errorMessage;	// 交易错误消息


    public String getErrorMessage(){
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage){
        this.errorMessage = errorMessage;
    }

    public String getHash(){
        return hash;
    }

    public void setHash(String hash){
        this.hash = hash;
    }

    public String getErrorCode(){
        return errorCode;
    }

    public void setErrorCode(String errorCode){
        this.errorCode = errorCode;
    }

    public Boolean getSuccess(){
        return success;
    }

    public void setSuccess(Boolean success){
        this.success = success;
    }

    public String getSponsorAddress(){
        return sponsorAddress;
    }

    public void setSponsorAddress(String sponsorAddress){
        this.sponsorAddress = sponsorAddress;
    }

    public long getSequenceNumber(){
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber){
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof TransactionExecutedEventMessage)) return false;

        TransactionExecutedEventMessage that = (TransactionExecutedEventMessage) o;

        if (getSequenceNumber() != that.getSequenceNumber()) return false;
        if (getHash() != null ? !getHash().equals(that.getHash()) : that.getHash() != null) return false;
        if (getSponsorAddress() != null ? !getSponsorAddress().equals(that.getSponsorAddress()) : that.getSponsorAddress() != null)
            return false;
        if (getSuccess() != null ? !getSuccess().equals(that.getSuccess()) : that.getSuccess() != null) return false;
        if (getErrorCode() != null ? !getErrorCode().equals(that.getErrorCode()) : that.getErrorCode() != null)
            return false;
        return getErrorMessage() != null ? getErrorMessage().equals(that.getErrorMessage()) : that.getErrorMessage() == null;
    }

    @Override
    public int hashCode(){
        int result = getHash() != null ? getHash().hashCode() : 0;
        result = 31 * result + (getSponsorAddress() != null ? getSponsorAddress().hashCode() : 0);
        result = 31 * result + (int) (getSequenceNumber() ^ (getSequenceNumber() >>> 32));
        result = 31 * result + (getSuccess() != null ? getSuccess().hashCode() : 0);
        result = 31 * result + (getErrorCode() != null ? getErrorCode().hashCode() : 0);
        result = 31 * result + (getErrorMessage() != null ? getErrorMessage().hashCode() : 0);
        return result;
    }

    @Override
    public String toString(){
        return "TransactionExecutedEventMessage{" +
                "hash='" + hash + '\'' +
                ", sponsorAddress='" + sponsorAddress + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", success=" + success +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

}
