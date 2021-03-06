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

        //创建普通用户
        createAccountOperation(config.getOperationService());

        // 创建合约账户:走严格的权限及门限设置
        createContractAccountOperation(config.getOperationService());

    }

    /**
     * 创建普通账户操作
     */
    private static void createAccountOperation(BcOperationService operationService){
        try {

            BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
            System.out.println("key pair" + GsonUtil.toJson(keyPair));

            CreateAccountOperation createAccountOper = new CreateAccountOperation.Builder()
                    .buildDestAddress(keyPair.getBubiAddress())
                    .buildAddInitBalance(100000L)
                    //.buildAddInitInput("")
                    // metadatas 描述元数据
                    .buildAddMetadata("key1", "自定义value1").buildAddMetadata("key2", "自定义value2")
                    //权限部分：针对合约的创建必须要求权限为0，交易门限为1
                    .buildPriMasterWeight(15) //默认的要生成账户的权限值
                    .buildPriTxThreshold(15)  //签名账户列表中不指定门限值默认的操作门限值
                    // 签名账户列表中特定指定的的操作权限值 共6种操作 1-创建账户2-发布资产3-发布资产4-设置元数据5-操作关联账户权限6-设置门限
                    .buildAddPriTypeThreshold(OperationTypeV3.CREATE_ACCOUNT, 8)
                    .buildAddPriTypeThreshold(OperationTypeV3.SET_METADATA, 6)
                    .buildAddPriTypeThreshold(OperationTypeV3.ISSUE_ASSET, 4)
                    .build();

            EvalTransaction newAcctEval = operationService.newEvalTransaction(address);
            TestTxResult testTx = newAcctEval.buildAddOperation(createAccountOper).commit();

            long fee = testTx.getRealFee();
            System.out.println("normal txfee:" + fee);

            //创建账户交易
            Transaction transaction = operationService.newTransaction(address);
            TransactionCommittedResult result = transaction.buildAddOperation(createAccountOper)
                    .buildTxMetadata("交易metadata")
                    .buildAddSigner(publicKey, privateKey)
                    .buildAddFee(fee)
                    .commit();

            System.out.println("\n------------------------------------------------");
            System.out.println(GsonUtil.toJson(result));
        } catch (SdkException e) {
            System.out.println(e);
            System.out.println("errorCode:" + e.getErrorCode() + ",errorMsg:" + e.getErrorMessage());
        }
    }

    /**
     * 创建合约账户操作
     */
    private static void createContractAccountOperation(BcOperationService operationService){
        try {

            BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
            System.out.println("key pair" + GsonUtil.toJson(keyPair));

            CreateAccountOperation createAccountOper = new CreateAccountOperation.Builder()
                    // 要生成的账号的地址
                    .buildDestAddress(keyPair.getBubiAddress())
                     //合约脚本,严格的语法检查
                    .buildScript("\"use strict\";function init(bar){  /*init whatever you want*/  return;}function main(input){  let para = JSON.parse(input);  if (para.do_foo)  {    let x = {      'hello' : 'world'    };  }}function query(input){   return input;}")
                    .buildAddInitBalance(100000L)
                    .buildAddInitInput("")
                    // metadatas 描述元数据
                    .buildAddMetadata("key1", "自定义value1").buildAddMetadata("key2", "自定义value2")
                     //权限部分：针对合约的创建必须要求权限为0，交易门限为1
                    .buildPriMasterWeight(0) //默认的要生成账户的权限值
                    .buildPriTxThreshold(1)  //签名账户列表中不指定门限值默认的操作门限值
                    .build();

            EvalTransaction newAcctEval = operationService.newEvalTransaction(address);
            TestTxResult testTx = newAcctEval.buildAddOperation(createAccountOper).commit();

            long fee = testTx.getRealFee();
            System.out.println("contract txfee:" + fee);

            //创建账户交易
            Transaction transaction = operationService.newTransaction(address);
            TransactionCommittedResult result = transaction.buildAddOperation(createAccountOper)
                    .buildTxMetadata("交易metadata")
                    .buildAddSigner(publicKey, privateKey)
                    .buildAddFee(fee)
                    .commit();

            System.out.println("\n------------------------------------------------");
            System.out.println(GsonUtil.toJson(result));
        } catch (SdkException e) {
            System.out.println(e);
            System.out.println("errorCode:" + e.getErrorCode() + ",errorMsg:" + e.getErrorMessage());
        }
    }

}
