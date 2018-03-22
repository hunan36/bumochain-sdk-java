package cn.bumo.sdk.core.pool.defaults;

import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.pool.SponsorAccountConfig;
import cn.bumo.sdk.core.transaction.model.Signature;

import java.util.Collections;
import java.util.List;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class DefaultSponsorAccountConfig implements SponsorAccountConfig{

    private static final String ACCOUNT_KEY_MARK = "$$$SponsorAccountPoolMark$$$";

    @Override
    public CreateAccountOperation createAccountOperation(String address, String publicKey, String privateKey, String accountMark) throws SdkException{
        return createAccountOperation(address, accountMark);
    }

    protected CreateAccountOperation createAccountOperation(String descAddress, String accountMark) throws SdkException{
        return new CreateAccountOperation.Builder()
                .buildDestAddress(descAddress)
                .buildAddMetadata(getAccountMarkKey(), accountMark)
                .buildPriMasterWeight(100)
                .buildPriTxThreshold(100)
                .build();
    }

    protected String getAccountMarkKey(){
        return ACCOUNT_KEY_MARK;
    }

    @Override
    public List<BcOperation> provideBcOperations(String address) throws SdkException{
        return Collections.emptyList();
    }

    @Override
    public List<Signature> provideSignature() throws SdkException{
        return Collections.emptyList();
    }

}
