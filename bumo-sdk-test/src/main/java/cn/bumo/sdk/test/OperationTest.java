package cn.bumo.sdk.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.adaptation.blockchain.bc.response.TransactionHistory;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.SetMetadata;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.access.utils.blockchain.SecureKeyGenerator;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.operation.OperationFactory;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.operation.impl.IssueAssetOperation;
import cn.bumo.sdk.core.operation.impl.PaymentOperation;
import cn.bumo.sdk.core.operation.impl.SetMetadataOperation;
import cn.bumo.sdk.core.operation.impl.SetSignerWeightOperation;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.TransactionContent;
import cn.bumo.sdk.core.transaction.model.TransactionBlob;
import cn.bumo.sdk.core.transaction.model.TransactionCommittedResult;
import cn.bumo.sdk.core.utils.GsonUtil;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 所有正常进行的操作测试
 */
public class OperationTest extends TestConfig{

    @Test
    public void query(){

        Account account = getQueryService().getAccount(address);

        LOGGER.info(GsonUtil.toJson(account));

    }

    @Test
    @Ignore("redis test")
    public void redisTest1() throws InterruptedException{
        redisTest();
    }

    @Test
    @Ignore("redis test")
    public void redisTest2() throws InterruptedException{
        redisTest();
    }

    private void redisTest() throws InterruptedException{
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                createAccount();
            }
        });
        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                createAccount();
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

    }

    /**
     * 1创建账户
     */
    @Test
    public void createAccount(){

        Transaction transaction = getOperationService().newTransaction(address);

        BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
        LOGGER.info(GsonUtil.toJson(keyPair));
        try {
            CreateAccountOperation createAccountOperation = new CreateAccountOperation.Builder()
                    .buildDestAddress(keyPair.getBubiAddress())
                    .buildAddInitBalance(10000000000000L)
                    //.buildScript("function main(input) { /*do what ever you want*/ }")
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

            // 可以拿到blob,让前端签名
            TransactionBlob blob = transaction
                    .buildAddOperation(createAccountOperation)
                    .buildAddFee(fee.getRealFee())
                    // 调用方可以在这里设置一个预期的区块偏移量，1个区块偏移量=3秒或1分钟，可以用3s进行推断，最快情况1分钟=20个区块偏移量
                    .buildFinalNotifySeqOffset(Transaction.HIGHT_FINAL_NOTIFY_SEQ_OFFSET)
                    .generateBlob();

            String hash = blob.getHash();
            TransactionContent.put(hash, transaction);


            //            try {
            //                // 模拟用户操作等待
            //                TimeUnit.SECONDS.sleep(65);
            //            } catch (InterruptedException e) {
            //                e.printStackTrace();
            //            }

            Transaction transactionRedisRead = TransactionContent.get(hash);

            // 签名完成之后可以继续提交
            TransactionCommittedResult result = transactionRedisRead
                    .buildAddSigner(publicKey, privateKey)
                    //.buildAddDigest("公钥",new byte[]{}) 可以让前端的签名在这里加进来
                    .commit();
            resultProcess(result, "创建账号状态:");

        } catch (SdkException e) {
            e.printStackTrace();
        }

        Account account = getQueryService().getAccount(keyPair.getBubiAddress());
        LOGGER.info("新建的账号:" + GsonUtil.toJson(account));
        Assert.assertNotNull("新建的账号不能查询到", account);
    }


    /**
     * 2,3发行和转移资产操作
     */
    @Test
    public void AssetOperation(){

        String assetCode = "asset-code";
        long amount = 100;
        long transferAmount = 9;
        try {
            BlockchainKeyPair user1 = createAccountOperation();
            
            IssueAssetOperation issueAssetOperation =  new IssueAssetOperation.Builder().buildAmount(amount).buildAssetCode(assetCode).build();
            EvalTransaction newAcctEval = getOperationService().newEvalTransaction(user1.getBubiAddress());
            TestTxResult fee = newAcctEval.buildAddOperation(issueAssetOperation).commit();

            Transaction issueTransaction = getOperationService().newTransaction(user1.getBubiAddress());

            issueTransaction
                    .buildAddOperation(issueAssetOperation)
                    .buildAddFee(fee.getRealFee())
                    .buildAddSigner(user1.getPubKey(), user1.getPriKey())
                    .commit();

            Account account = getQueryService().getAccount(user1.getBubiAddress());
            LOGGER.info("user1资产:" + GsonUtil.toJson(account.getAssets()));
            Assert.assertNotNull("发行资产不能为空", account.getAssets());

            
            
            BlockchainKeyPair user2 = createAccountOperation();
            PaymentOperation paymentOperation = OperationFactory.newPaymentOperation(user2.getBubiAddress(), user1.getBubiAddress(), assetCode, transferAmount);
            fee = getOperationService().newEvalTransaction(user1.getBubiAddress()).buildAddOperation(paymentOperation).commit();
            Transaction transferTransaction = getOperationService().newTransaction(user1.getBubiAddress());
            transferTransaction
                    .buildAddOperation(paymentOperation)
                    .buildAddFee(fee.getRealFee())
                    .buildAddSigner(user1.getPubKey(), user1.getPriKey())
                    .commit();


            Account account2 = getQueryService().getAccount(user2.getBubiAddress());
            LOGGER.info("account2:" + GsonUtil.toJson(account2));

            LOGGER.info("user2资产:" + GsonUtil.toJson(account2.getAssets()));
            Assert.assertNotNull("转移资产没有收到", account2.getAssets());
            Assert.assertEquals("转移资产数量错误", 9, account2.getAssets()[0].getAmount());
        } catch (SdkException e) {
            e.printStackTrace();
        }
    }


    /**
     * 4设置和修改metadata
     */
    @Test
    public void updateMetadata(){
        try {
            BlockchainKeyPair user = createAccountOperation();

            String key1 = "boot自定义key1";
            String key2 = "boot自定义key2";

            SetMetadata setMetadata = getQueryService().getAccount(user.getBubiAddress(), key1);
            setMetadata.setValue("这是新设置的value1");
            SetMetadataOperation setMetadataOperation = OperationFactory.newUpdateSetMetadataOperation(setMetadata);
            EvalTransaction newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            TestTxResult fee = newAcctEval.buildAddOperation(setMetadataOperation).commit();
            Transaction updateMetadataTransaction = getOperationService().newTransaction(user.getBubiAddress());
            
            updateMetadataTransaction
                    .buildAddOperation(setMetadataOperation)
                    .buildAddFee(fee.getRealFee())
                    .buildAddSigner(user.getPubKey(), user.getPriKey())
                    .commit();

            Account account = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("修改metadata结果:" + GsonUtil.toJson(account.getMetadatas()));
            Assert.assertTrue("修改metadata1结果,失败", Arrays.stream(account.getMetadatas())
                    .anyMatch(setMetadata1 -> "这是新设置的value1".equals(setMetadata1.getValue())));


            setMetadataOperation = OperationFactory.newSetMetadataOperation("newMetadataKey2", "newMetadataValue2");
            
            newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            fee = newAcctEval.buildAddOperation(setMetadataOperation).commit();
            Transaction newMetadataTransaction = getOperationService().newTransaction(user.getBubiAddress());
            newMetadataTransaction
                    .buildAddOperation(setMetadataOperation)
                    .buildAddFee(fee.getRealFee())
                    .buildAddSigner(user.getPubKey(), user.getPriKey())
                    .commit();

            Account account2 = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("新建metadata结果:" + GsonUtil.toJson(account2.getMetadatas()));
            Assert.assertTrue("新建metadata结果,失败", Arrays.stream(account2.getMetadatas())
                    .anyMatch(setMetadata1 -> "newMetadataValue2".equals(setMetadata1.getValue())));


            SetMetadata setMetadata2 = getQueryService().getAccount(user.getBubiAddress(), key2);
            setMetadata2.setValue("这是新设置的value222");
            
            setMetadataOperation = OperationFactory.newSetMetadataOperation(setMetadata2.getKey(), setMetadata2.getValue(), setMetadata2.getVersion());
            newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            fee = newAcctEval.buildAddOperation(setMetadataOperation).commit();
            Transaction updateMetadataTransaction2 = getOperationService().newTransaction(user.getBubiAddress());
            updateMetadataTransaction2
                    .buildAddOperation(setMetadataOperation)
                    .buildAddFee(fee.getRealFee())
                    .buildAddSigner(user.getPubKey(), user.getPriKey())
                    .commit();

            Account account3 = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("修改metadata2结果:" + GsonUtil.toJson(account3.getMetadatas()));
            Assert.assertTrue("修改metadata2结果,失败", Arrays.stream(account3.getMetadatas())
                    .anyMatch(setMetadata1 -> "这是新设置的value222".equals(setMetadata1.getValue())));
        } catch (SdkException e) {
            e.printStackTrace();
        }

    }

    /**
     * 5设置/修改权重
     */
    @Test
    public void setSignerWeight(){
        try {
            BlockchainKeyPair user = createAccountOperation();

            BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
            
            SetSignerWeightOperation setSignerWeightOperation = OperationFactory.newSetSignerWeightOperation(keyPair.getBubiAddress(), 8);
            EvalTransaction newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            TestTxResult fee = newAcctEval.buildAddOperation(setSignerWeightOperation).commit();
            getOperationService()
                    .newTransaction(user.getBubiAddress())
                    .buildAddFee(fee.getRealFee())
                    .buildAddOperation(setSignerWeightOperation)
                    .commit(user.getPubKey(), user.getPriKey());

            Account account = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("增加一个签名人权重8:" + GsonUtil.toJson(account.getPriv()));
            Assert.assertTrue("增加一个签名人权重8,失败", account.getPriv().getSigners().stream()
                    .anyMatch(signer -> signer.getAddress().equals(keyPair.getBubiAddress()) && signer.getWeight() == 8));
            
            setSignerWeightOperation = OperationFactory.newSetSignerWeightOperation(20);
            newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            fee = newAcctEval.buildAddOperation(setSignerWeightOperation).commit();
            Transaction setSignerWeightTransaction = getOperationService().newTransaction(user.getBubiAddress());
            TransactionCommittedResult setSignerWeightResult = setSignerWeightTransaction
            		.buildAddFee(fee.getRealFee())
                    .buildAddOperation(setSignerWeightOperation)
                    .commit(user.getPubKey(), user.getPriKey());

            resultProcess(setSignerWeightResult, "修改权重结果状态:");

            Account account2 = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("修改权重到20:" + GsonUtil.toJson(account2.getPriv()));
            Assert.assertEquals("修改权重到20,失败", 20, account2.getPriv().getMasterWeight());


            setSignerWeightOperation = OperationFactory.newSetSignerWeightOperation(keyPair.getBubiAddress(), 0);
            
            newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            fee = newAcctEval.buildAddOperation(setSignerWeightOperation).commit();
            getOperationService()
                    .newTransaction(user.getBubiAddress())
                    .buildAddFee(fee.getRealFee())
                    .buildAddOperation(setSignerWeightOperation)
                    .commit(user.getPubKey(), user.getPriKey());

            Account account3 = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("移除一个签名人:" + GsonUtil.toJson(account3.getPriv()));
            Assert.assertTrue("移除一个签名人,失败", account3.getPriv().getSigners().stream()
                    .noneMatch(signer -> signer.getAddress().equals(keyPair.getBubiAddress())));
        } catch (SdkException e) {
            e.printStackTrace();
        }

    }

    /**
     * 6设置/修改门限
     */
    @Test
    public void setThreshold(){
        try {
            BlockchainKeyPair user = createAccountOperation();
            
            
            BcOperation bcOperation = OperationFactory.newSetThresholdOperation(14);
            EvalTransaction newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            TestTxResult fee = newAcctEval.buildAddOperation(bcOperation).commit();
            getOperationService()
                    .newTransaction(user.getBubiAddress())
                    .buildAddFee(fee.getRealFee())
                    .buildAddOperation(bcOperation)
                    .commit(user.getPubKey(), user.getPriKey());

            Account account = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("更新交易门限到14:" + GsonUtil.toJson(account.getPriv()));
            Assert.assertEquals("更新交易门限到14,失败", 14, account.getPriv().getThreshold().getTxThreshold());
            
            bcOperation = OperationFactory.newSetThresholdOperation(OperationTypeV3.CREATE_ACCOUNT, 10);
            
            newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            fee = newAcctEval.buildAddOperation(bcOperation).commit();
            getOperationService()
                    .newTransaction(user.getBubiAddress())
                    .buildAddFee(fee.getRealFee())
                    .buildAddOperation(bcOperation)
                    .commit(user.getPubKey(), user.getPriKey());

            Account account2 = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("更新创建账号1到10:" + GsonUtil.toJson(account2.getPriv()));
            Assert.assertTrue("更新创建账号1到10,失败", account2.getPriv().getThreshold().getTypeThresholds().stream()
                    .anyMatch(typeThreshold -> typeThreshold.getType() == 1 && typeThreshold.getThreshold() == 10));

            
            bcOperation = OperationFactory.newSetThresholdOperation(OperationTypeV3.SET_THRESHOLD, 2);
            
            newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            fee = newAcctEval.buildAddOperation(bcOperation).commit();
            getOperationService()
                    .newTransaction(user.getBubiAddress())
                    .buildAddFee(fee.getRealFee())
                    .buildAddOperation(bcOperation)
                    .commit(user.getPubKey(), user.getPriKey());

            Account account3 = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("新增设置门限6到2:" + GsonUtil.toJson(account3.getPriv()));
            Assert.assertTrue("新增设置门限6到2,失败", account3.getPriv().getThreshold().getTypeThresholds().stream()
                    .anyMatch(typeThreshold -> typeThreshold.getType() == 6 && typeThreshold.getThreshold() == 2));

        } catch (SdkException e) {
            e.printStackTrace();
        }

    }


    /**
     * 合约调用
     * 不太会写，随便写个
     */
    @Test
    public void invokeContract(){
        try {
            BlockchainKeyPair user = createAccountOperation();
            BlockchainKeyPair user2 = createAccountOperation();

            TransactionCommittedResult result = getOperationService()
                    .newTransaction(user.getBubiAddress())
                    .buildAddOperation(OperationFactory.newInvokeContractOperation(user2.getBubiAddress(), "inputdata"))
                    .commit(user.getPubKey(), user.getPriKey());

            resultProcess(result, "合约调用。。。");

            TransactionHistory transactionHistory = getQueryService().getTransactionHistoryByHash(result.getHash());
            Assert.assertEquals("合约调用失败", 0, transactionHistory.getTransactions()[0].getErrorCode());
        } catch (SdkException e) {
            e.printStackTrace();
        }
    }

}
