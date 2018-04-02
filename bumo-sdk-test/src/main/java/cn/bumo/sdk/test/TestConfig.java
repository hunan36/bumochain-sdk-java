package cn.bumo.sdk.test;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.access.utils.blockchain.SecureKeyGenerator;
import cn.bumo.sdk.core.config.SDKConfig;
import cn.bumo.sdk.core.config.SDKProperties;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.spi.BcOperationService;
import cn.bumo.sdk.core.spi.BcQueryService;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.model.TransactionCommittedResult;
import cn.bumo.sdk.core.utils.GsonUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 测试部分主要测试了1负载能力，2正常操作，3部分常见异常
 */
public abstract class TestConfig{

    static Logger LOGGER = LoggerFactory.getLogger(TestConfig.class);

    static final String address = "buQdwLcW5dcWEg8cbKrFN3WKPXXwh5JTqKAH";
    static final String publicKey = "b00121a6f43b505453bf8f3ed72966a6886632a1b6bec224062762309dbc143da24a92dcf038";
    static final String privateKey = "privbUNfe7NtchzD4aJnPYSZEr3TXPU2upKemTrV26NfM7XLSY9C12po";

    private static BcOperationService operationService;
    private static BcQueryService queryService;

    @BeforeClass
    public static void configSdk() throws SdkException{

        String eventUtis = "ws://192.168.7.51:36003,ws://192.168.7.52:36003,ws://192.168.7.53:36003,ws://192.168.7.54:36003";
        String ips = "192.168.7.51:36002,192.168.7.52:36002,192.168.7.53:36002,192.168.7.54:36002";

        SDKConfig config = new SDKConfig();
        SDKProperties sdkProperties = new SDKProperties();
        sdkProperties.setEventUtis(eventUtis);
        sdkProperties.setIps(ips);
        sdkProperties.setAddress(address);
        sdkProperties.setPublicKey(publicKey);
        sdkProperties.setPrivateKey(privateKey);
        sdkProperties.setRedisSeqManagerEnable(true);
        sdkProperties.setHost("192.168.10.73");
        sdkProperties.setPort(10379);
        sdkProperties.setPassword("bubi888");
        sdkProperties.setDatabase("0");
        config.configSdk(sdkProperties);

        TestConfig.operationService = config.getOperationService();
        TestConfig.queryService = config.getQueryService();
    }

    /**
     * 创建账户操作
     */
    BlockchainKeyPair createAccountOperation() throws SdkException{

        Transaction transaction = getOperationService().newTransaction(address);

        BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
        LOGGER.info(GsonUtil.toJson(keyPair));

        CreateAccountOperation createAccountOperation = new CreateAccountOperation.Builder()
                .buildDestAddress(keyPair.getBubiAddress())
                //.buildScript("function main(input) { /*do what ever you want*/ }")
                .buildAddInitBalance(1000000000000000000L)
                .buildAddMetadata("boot自定义key1", "boot自定义value1").buildAddMetadata("boot自定义key2", "boot自定义value2")
                // 权限部分
                .buildPriMasterWeight(15)
                .buildPriTxThreshold(15)
                .buildAddPriTypeThreshold(OperationTypeV3.CREATE_ACCOUNT, 8)
                .buildAddPriTypeThreshold(OperationTypeV3.SET_METADATA, 6)
                .buildAddPriTypeThreshold(OperationTypeV3.ISSUE_ASSET, 4)
                .buildAddPriSigner(SecureKeyGenerator.generateBubiKeyPair().getBubiAddress(), 10)
                .buildOperationMetadata("操作metadata")// 这个操作应该最后build
                .build();
        
        EvalTransaction newAcctEval = getOperationService().newEvalTransaction(address);
        
        TestTxResult fee = newAcctEval.buildAddOperation(createAccountOperation).commit();

        TransactionCommittedResult result = transaction.buildAddOperation(createAccountOperation)
                .buildTxMetadata("交易metadata")
                .buildAddSigner(publicKey, privateKey)
                .buildAddFee(fee.getRealFee())
                .commit();

        resultProcess(result, "创建账号状态:");

        Account account = getQueryService().getAccount(keyPair.getBubiAddress());
        LOGGER.info("新建的账号:" + GsonUtil.toJson(account));
        Assert.assertNotNull("新建的账号不能查询到", account);

        return keyPair;
    }

    void resultProcess(TransactionCommittedResult result, String debugMessage){
        Assert.assertNotNull("result must not null", result);
        Assert.assertNotNull("交易hash不能为空", result.getHash());
        LOGGER.info(debugMessage + GsonUtil.toJson(result));
    }

    static BcOperationService getOperationService(){
        return operationService;
    }

    static BcQueryService getQueryService(){
        return queryService;
    }

}
