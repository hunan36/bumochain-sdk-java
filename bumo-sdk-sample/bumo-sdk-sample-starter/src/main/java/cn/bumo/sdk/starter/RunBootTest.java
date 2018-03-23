package cn.bumo.sdk.starter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.access.utils.blockchain.SecureKeyGenerator;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.spi.BcOperationService;
import cn.bumo.sdk.core.spi.BcQueryService;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.TransactionContent;
import cn.bumo.sdk.core.transaction.model.TransactionBlob;
import cn.bumo.sdk.core.transaction.model.TransactionCommittedResult;
import cn.bumo.sdk.core.utils.GsonUtil;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
@SpringBootApplication
public class RunBootTest implements CommandLineRunner{

    public static void main(String[] args){
        SpringApplication.run(RunBootTest.class, args);
    }

    private BcOperationService operationService;
    private BcQueryService queryService;

    public RunBootTest(BcOperationService operationService, BcQueryService queryService){
        this.operationService = operationService;
        this.queryService = queryService;
    }

    private static String address = "buQdwLcW5dcWEg8cbKrFN3WKPXXwh5JTqKAH";
    private static String pub_key = "b00121a6f43b505453bf8f3ed72966a6886632a1b6bec224062762309dbc143da24a92dcf038";
    private static String pri_key = "privbUNfe7NtchzD4aJnPYSZEr3TXPU2upKemTrV26NfM7XLSY9C12po";

    @RequestMapping("create")
    public void create(){
        createAccountOperation();
    }

    @Override
    public void run(String... args) throws Exception{

        // 进行查询
        Account account = queryService.getAccount(address);
        System.out.println(GsonUtil.toJson(account));

        // 简单操作
        createAccountOperation();

        
        //System.exit(0);
    }

    /**
     * 创建账户操作
     */
    private void createAccountOperation(){
        try {
            

            BlockchainKeyPair user = SecureKeyGenerator.generateBubiKeyPair();
            System.out.println(GsonUtil.toJson(user));

            CreateAccountOperation createAccountOperation = new CreateAccountOperation.Builder()
                    .buildDestAddress(user.getBubiAddress())
                    //.buildScript("function main(input) { /*do what ever you want*/ }")
                    .buildAddInitBalance(10000000000L)
                    .buildAddMetadata("boot自定义key1", "boot自定义value1").buildAddMetadata("boot自定义key2", "boot自定义value2")
                    // 权限部分
                    .buildPriMasterWeight(15)
                    .buildPriTxThreshold(15)
                    .buildAddPriTypeThreshold(OperationTypeV3.CREATE_ACCOUNT, 8)
                    .buildAddPriTypeThreshold(OperationTypeV3.SET_METADATA, 6)
                    .buildAddPriTypeThreshold(OperationTypeV3.ISSUE_ASSET, 4)
                    .buildAddPriSigner(SecureKeyGenerator.generateBubiKeyPair().getBubiAddress(), 10)
                    .build();
            
            
            EvalTransaction evalTx = operationService.newEvalTransaction(address);
            
            TestTxResult txFee = evalTx.buildAddOperation(createAccountOperation).commit();

            
            Transaction transaction = operationService.newTransaction(address);
            TransactionBlob transactionBlob = transaction.buildAddOperation(createAccountOperation).buildAddFee(txFee.getRealFee()).generateBlob();
            String hash = transactionBlob.getHash();

            TransactionContent.put(hash, transaction);


            TransactionCommittedResult result = TransactionContent.get(hash)
                    .buildTxMetadata("交易metadata")
                    .buildAddSigner(pub_key, pri_key)
                    .commit();

            System.out.println("\n------------------------------------------------");
            System.out.println(GsonUtil.toJson(result));
        } catch (SdkException e) {
            e.printStackTrace();
        }
    }


}
