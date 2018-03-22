package cn.bumo.sdk.core.pool;

import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.transaction.model.Signature;

import java.util.List;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 提供对账户池新建时的帐户操作能力
 */
public interface SponsorAccountConfig{
    /**
     * 账户池调用此方法进行获取创建账户操作
     *
     * @param address     地址
     * @param publicKey   公钥
     * @param privateKey  私钥
     * @param accountMark 账户标记
     */
    CreateAccountOperation createAccountOperation(String address, String publicKey, String privateKey, String accountMark) throws SdkException;

    /**
     * 对帐户添加其它操作
     *
     * @param address 账户池地址
     */
    List<BcOperation> provideBcOperations(String address) throws SdkException;

    /**
     * 如果有其它操作，这里时提供的额外提交签名信息
     */
    List<Signature> provideSignature() throws SdkException;

}
