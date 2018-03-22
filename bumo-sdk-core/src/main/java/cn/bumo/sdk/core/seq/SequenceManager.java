package cn.bumo.sdk.core.seq;

import cn.bumo.sdk.core.exception.SdkException;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public interface SequenceManager{

    /**
     * 初始化
     */
    void init();

    /**
     * 销毁
     */
    void destroy();

    /**
     * 获取指定区块链地址的下一个可提交的交易序列号；
     */
    long getSequenceNumber(String address) throws SdkException;

    /**
     * 重置，提交失败时调用
     */
    void reset(String address);
    /**
     * 回复数据
     * @param address
     */
    void restore(String address,long oldVal);

}
