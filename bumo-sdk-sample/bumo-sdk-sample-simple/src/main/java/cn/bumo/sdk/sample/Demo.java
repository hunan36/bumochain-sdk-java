package cn.bumo.sdk.sample;

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

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class Demo{

    //    创始者
    private static String address = "buQdwLcW5dcWEg8cbKrFN3WKPXXwh5JTqKAH";
    private static String publicKey = "b00121a6f43b505453bf8f3ed72966a6886632a1b6bec224062762309dbc143da24a92dcf038";
    private static String privateKey = "privbUNfe7NtchzD4aJnPYSZEr3TXPU2upKemTrV26NfM7XLSY9C12po";

    public static void main(String[] args) throws SdkException{

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
        //InitBalanceEnable默认初始为true
        sdkProperties.setInitBalanceEnable(true);
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
                    //.buildScript("function main(input) { /*do what ever you want*/ }")//创建合约账户必须有这一行
                    .buildAddInitBalance(10000000000000L)
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
            
            EvalTransaction newAcctEval = operationService.newEvalTransaction(address);
	          TestTxResult testTx = newAcctEval.buildAddOperation(createAccountOperation).commit();

	          long fee = testTx.getRealFee();


            TransactionCommittedResult result = transaction.buildAddOperation(createAccountOperation)
                    .buildTxMetadata("交易metadata")
                    .buildAddSigner(publicKey, privateKey)
                    .buildAddFee(fee)
                    .commit();

            System.out.println("\n------------------------------------------------");
            System.out.println(GsonUtil.toJson(result));
        } catch (SdkException e) {
            e.printStackTrace();
        }
    }

}
