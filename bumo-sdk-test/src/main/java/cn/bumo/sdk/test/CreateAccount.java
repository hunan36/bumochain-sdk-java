package cn.bumo.sdk.test;

import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.access.utils.blockchain.SecureKeyGenerator;
import cn.bumo.sdk.core.config.SDKConfig;
import cn.bumo.sdk.core.config.SDKProperties;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 提供参数创建账户的能力
 * java -cp sdk-test-2.0.0.beta3.jar cn.bubi.sdk.test.CreateAccount
 * {eventUtis} {ips} {address} {publicKey} {privateKey}
 */
public class CreateAccount{

    private static Logger logger = LoggerFactory.getLogger(CreateAccount.class);

    public static void main(String[] args) throws SdkException{

        if (args.length != 5) {
            throw new IllegalArgumentException("参数错误");
        }

        String eventUtis = args[0];
        String ips = args[1];
        String address = args[2];
        String publicKey = args[3];
        String privateKey = args[4];

        SDKConfig config = new SDKConfig();
        SDKProperties sdkProperties = new SDKProperties();
        sdkProperties.setEventUtis(eventUtis);
        sdkProperties.setIps(ips);
        sdkProperties.setAccountPoolEnable(false);
        sdkProperties.setRedisSeqManagerEnable(false);
        config.configSdk(sdkProperties);

        Transaction transaction = config.getOperationService().newTransaction(address);

        BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();

        CreateAccountOperation createAccountOperation = new CreateAccountOperation.Builder()
                .buildDestAddress(keyPair.getBubiAddress())
                .buildAddMetadata("$$$system auto insert$$$", "sdk init account")
                .buildPriMasterWeight(99)
                .buildPriTxThreshold(99)
                .buildOperationMetadata("sdk init account")//
                .build();

        transaction.buildAddOperation(createAccountOperation)
                .buildTxMetadata("sdk init account")
                .buildAddSigner(publicKey, privateKey)
                .commit();

        logger.info("new account : \n" + keyPair);
        System.exit(0);
    }
}
