package cn.bumo.sdk.starter;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.access.utils.blockchain.SecureKeyGenerator;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.spi.BcOperationService;
import cn.bumo.sdk.core.spi.BcQueryService;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.TransactionContent;
import cn.bumo.sdk.core.transaction.model.TransactionBlob;
import cn.bumo.sdk.core.transaction.model.TransactionCommittedResult;
import cn.bumo.sdk.core.utils.GsonUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;

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

    private static String address = "a00236e253497d93a2f4386aa57490476e80a2621b0f0d";

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


        System.exit(0);
    }

    /**
     * 创建账户操作
     */
    private void createAccountOperation(){
        try {
            Transaction transaction = operationService.newTransactionByAccountPool();

            BlockchainKeyPair user = SecureKeyGenerator.generateBubiKeyPair();
            System.out.println(GsonUtil.toJson(user));

            CreateAccountOperation createAccountOperation = new CreateAccountOperation.Builder()
                    .buildDestAddress(user.getBubiAddress())
                    .buildScript("function main(input) { /*do what ever you want*/ }")
                    .buildAddMetadata("boot自定义key1", "boot自定义value1").buildAddMetadata("boot自定义key2", "boot自定义value2")
                    // 权限部分
                    .buildPriMasterWeight(15)
                    .buildPriTxThreshold(15)
                    .buildAddPriTypeThreshold(OperationTypeV3.CREATE_ACCOUNT, 8)
                    .buildAddPriTypeThreshold(OperationTypeV3.SET_METADATA, 6)
                    .buildAddPriTypeThreshold(OperationTypeV3.ISSUE_ASSET, 4)
                    .buildAddPriSigner(SecureKeyGenerator.generateBubiKeyPair().getBubiAddress(), 10)
                    .build();


            TransactionBlob transactionBlob = transaction.buildAddOperation(createAccountOperation).generateBlob();
            String hash = transactionBlob.getHash();

            TransactionContent.put(hash, transaction);


            TransactionCommittedResult result = TransactionContent.get(hash)
                    .buildTxMetadata("交易metadata")
                    .commit();

            System.out.println("\n------------------------------------------------");
            System.out.println(GsonUtil.toJson(result));
        } catch (SdkException e) {
            e.printStackTrace();
        }
    }


}
