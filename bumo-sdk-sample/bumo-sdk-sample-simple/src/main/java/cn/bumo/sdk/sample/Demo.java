package cn.bumo.sdk.sample;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.access.utils.blockchain.SecureKeyGenerator;
import cn.bumo.sdk.core.config.SDKConfig;
import cn.bumo.sdk.core.config.SDKProperties;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.spi.BcOperationService;
import cn.bumo.sdk.core.spi.BcQueryService;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.model.TransactionCommittedResult;
import cn.bumo.sdk.core.utils.GsonUtil;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class Demo{

    //    创始者
    private static String address = "a0012ea403227b861289ed5fcedd30e51e85ef7397ebc6";
    private static String publicKey = "b001e9fd31a0fc25af3123f67575cdd0c6b8c2192eead9f58728a3fb46accdc0faa67f";
    private static String privateKey = "c0018335e8c3e34cceaa24027207792318bc388bea443b53d5ba9e00e5adb6739bb61b";

    public static void main(String[] args) throws SdkException{

        String eventUtis = "ws://192.168.10.100:7053,ws://192.168.10.110:7053,ws://192.168.10.120:7053,ws://192.168.10.130:7053";
        String ips = "192.168.10.100:29333,192.168.10.110:29333,192.168.10.120:29333,192.168.10.130:29333";

        SDKConfig config = new SDKConfig();
        SDKProperties sdkProperties = new SDKProperties();
        sdkProperties.setEventUtis(eventUtis);
        sdkProperties.setIps(ips);
        sdkProperties.setAccountPoolEnable(true);
        sdkProperties.setAddress(address);
        sdkProperties.setPublicKey(publicKey);
        sdkProperties.setPrivateKey(privateKey);
        sdkProperties.setSize(12);
        sdkProperties.setMark("test-demo-config");
        sdkProperties.setRedisSeqManagerEnable(true);
        sdkProperties.setHost("192.168.10.73");
        sdkProperties.setPort(10379);
        sdkProperties.setPassword("bubi888");

        sdkProperties.setDatabase("0");
        config.configSdk(sdkProperties);

        // 进行查询
        BcQueryService queryService = config.getQueryService();
        Account account = queryService.getAccount(address);
        System.out.println(GsonUtil.toJson(account));

        // 简单操作
        createAccountOperation(config.getOperationService());

    }

    /**
     * 创建账户操作
     */
    private static void createAccountOperation(BcOperationService operationService){
        try {
            Transaction transaction = operationService.newTransaction(address);

            BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
            System.out.println(GsonUtil.toJson(keyPair));
            //SDK封装一个CreateContactAcccountOperation（合约账号）
            CreateAccountOperation createAccountOperation = new CreateAccountOperation.Builder()
                    .buildDestAddress(keyPair.getBubiAddress())
                    .buildScript("function main(input) { /*do what ever you want*/ }")//创建合约账户必须有这一行
                    .buildAddMetadata("key1", "自定义value1").buildAddMetadata("key2", "自定义value2")
                    // 权限部分
                    .buildPriMasterWeight(15)
                    .buildPriTxThreshold(15)
                    .buildAddPriTypeThreshold(OperationTypeV3.CREATE_ACCOUNT, 8)
                    .buildAddPriTypeThreshold(OperationTypeV3.SET_METADATA, 6)
                    .buildAddPriTypeThreshold(OperationTypeV3.ISSUE_ASSET, 4)
                    .buildAddPriSigner(SecureKeyGenerator.generateBubiKeyPair().getBubiAddress(), 10)
                    //没有签名列表。。。。
                    .build();

            TransactionCommittedResult result = transaction.buildAddOperation(createAccountOperation)
                    .buildTxMetadata("交易metadata")
                    .buildAddSigner(publicKey, privateKey)
                    .commit();

            System.out.println("\n------------------------------------------------");
            System.out.println(GsonUtil.toJson(result));
        } catch (SdkException e) {
            e.printStackTrace();
        }
    }

}
