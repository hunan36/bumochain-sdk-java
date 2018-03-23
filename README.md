# Access-SDK使用说明

Access-SDK 使用说明

---
## 目录
 1. [前言](#前言)
 2. [概览](#概览)
 3. [配置说明](#配置说明)  
    - [1简单的config配置使用](#1简单的config配置使用)  
    - [2基于Spring Boot的starter方式自动配置](#2基于spring-boot的starter方式自动配置)  
 4. [使用说明](#使用说明)  
    - [1创建账户操作](#1创建账户操作)  
    - [2发行和转移资产操作](#2发行和转移资产操作)  
    - [3设置和修改metadata](#3设置和修改metadata)  
    - [4设置 修改权重](#4设置-修改权重)  
    - [5设置 修改门限](#5设置-修改门限)  
    - [6合约调用](#6合约调用)  
    - [7业务分支返回形式](#7业务分支返回形式)  
    - [8支付BU币] 
    - [9-15评估费用]


----------


## <div id="1">前言</div>  ##
本SDK目的为便捷的使用Java语法访问区块链，屏蔽底层细节。最大限度的方便应用开发者访问区块链。本SDK并无额外抽象与转换，所有的特性，抽象都以底层描述为准。


----------
## 概览 ##
现主要说明SDK功能  
1提供负载访问底层的能力  
2对底层通知的监听，同步交易  
3对底层支持的操作便捷创建，生成blob  
4本SDK的交易发起人seq默认采用内存管理，如果使用方应用集群，那么必须配置seq服务，SDK所支持的seq服务内置依赖redis，使用方可以根据自身情况实现自定义seq服务  


----------
## 配置说明 ##
SDK本身无任何依赖框架，使用时载入配置即可运行，考虑到实际应用中的web项目大部分都与Spring框架融合，现提供了sdk-starter项目供使用方快速使用。关于具体的项目配置实践可以参考sdk-sample项目，该项目提供了简单的配置访问与Spring Boot的接入访问两种方式的简单项目搭建。



> 使用前必读：由于使用了监听地址映射访问地址(也就是配置代码里的eventUtis和ips)，所以监听地址列表对应的ip或者域名必须要在访问地址里也有，否则负载失败会导致访问不了区块链服务！

> 其它说明
1JDK需要1.8以上
<dependency>
  <groupId>cn.bumo.access.sdk</groupId>
  <artifactId>bumo-sdk-core</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>


### 1简单的config配置使用
``` java

public void config() throws SdkException{
    String address = "a0012ea403227b861289ed5fcedd30e51e85ef7397ebc6";
    String publicKey = "b001e9fd31a0fc25af3123f67575cdd0c6b8c2192eead9f58728a3fb46accdc0faa67f";
    String privateKey = "c0018335e8c3e34cceaa24027207792318bc388bea443b53d5ba9e00e5adb6739bb61b";

    String eventUtis = "ws://192.168.10.100:7053,ws://192.168.10.110:7053,ws://192.168.10.120:7053,ws://192.168.10.130:7053";
    String ips = "192.168.10.100:29333,192.168.10.110:29333,192.168.10.120:29333,192.168.10.130:29333";

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
    //InitBalanceEnable默认初始为true
    sdkProperties.setInitBalanceEnable(true);
    config.configSdk(sdkProperties);

    // 完成配置获得spi
    config.getOperationService();
    config.getQueryService();

}


```

> 基于简单配置引入sdk-core依赖即可，如：
```java
<dependency>
    <groupId>cn.bubi.access.sdk</groupId>
    <artifactId>sdk-core</artifactId>
    <version>${access-sdk.version}</version>
</dependency>
```

> 如果想自定义配置参考SDKConfig类的配置方法，可以自行配置

### 2基于Spring Boot的starter方式自动配置

在application.properties中配置参数，如：
```ruby 

# sdk config
blockchain.event.uri=ws://192.168.10.100:7053,ws://192.168.10.110:7053,ws://192.168.10.120:7053,ws://192.168.10.130:7053
blockchain.node.ip=192.168.10.100:29333,192.168.10.110:29333,192.168.10.120:29333,192.168.10.130:29333


# redis seq管理的支持 这个功能默认是关闭的
blockchain.redis-seq.enable=true
blockchain.redis-seq.redis[0].host=192.168.10.73
blockchain.redis-seq.redis[0].port=10379
blockchain.redis-seq.redis[0].password=bubi888
```
还需要项目依赖引入sdk-starter
```java
<dependency>
    <groupId>cn.bubi.access.sdk</groupId>
    <artifactId>sdk-starter</artifactId>
    <version>${access-sdk-starter.version}</version>
</dependency>
```

> 在完成上述配置之后，通过使用BcOperationService和BcQueryService对象来完成具体操作。如果项目并未采用Spring Boot框架，可以参考Config类将相应对象以xml形式托管到Spring容器中管理，这里不再提供xml配置。

----------
## 使用说明  ##
完成配置之后，即可进行相应的操作了。这里列举所有支持的操作。也可以参考项目sdk-test查看所有操作的单元测试。

### 1创建账户操作

```java
/**
 * 1创建账户
 */
@Test
    public void createAccount(){

        Transaction transaction = getOperationService().newTransaction(address);

        BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
        LOGGER.info(GsonUtil.toJson(keyPair));
        try {
            BcOperation bcOperation = new CreateAccountOperation.Builder()
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
            TestTxResult fee = newAcctEval.buildAddOperation(bcOperation).commit();

            // 可以拿到blob,让前端签名
            TransactionBlob blob = transaction
                    .buildAddOperation(bcOperation)
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
```

> 需要注意的通过操作对象获得的交易对象Transaction本身是线程不安全的，不要多线程引用。并且所有的操作对象和Transaction本身都是有状态的，不可以重复使用，每一个操作完成就可以丢弃了，如有新操作需要新建操作对象，交易对象也是如此。


> 提示：这里做了个让前端签名的模拟操作，如果调用方有需要可以参考。注意generateBlob()方法要先调用，这样生成了blob之后可以通过getTransactionBlob()方法再次从交易对象中拿到blob，但是如果没有生成blob就直接调用getTransactionBlob()会得到异常.



### 2发行和转移资产操作
```java
/**
 * 2发行和转移资产操作
 */
 @Test
    public void AssetOperation(){

        String assetCode = "asset-code";
        long amount = 100;
        long transferAmount = 9;
        try {
            BlockchainKeyPair user1 = createAccountOperation();
            
            BcOperation bcOperation =  new IssueAssetOperation.Builder().buildAmount(amount).buildAssetCode(assetCode).build();
            EvalTransaction newAcctEval = getOperationService().newEvalTransaction(user1.getBubiAddress());
            TestTxResult fee = newAcctEval.buildAddOperation(bcOperation).commit();

            Transaction issueTransaction = getOperationService().newTransaction(user1.getBubiAddress());

            issueTransaction
                    .buildAddOperation(bcOperation)
                    .buildAddFee(fee.getRealFee())
                    .buildAddSigner(user1.getPubKey(), user1.getPriKey())
                    .commit();

            Account account = getQueryService().getAccount(user1.getBubiAddress());
            LOGGER.info("user1资产:" + GsonUtil.toJson(account.getAssets()));
            Assert.assertNotNull("发行资产不能为空", account.getAssets());

            
            
            BlockchainKeyPair user2 = createAccountOperation();
            bcOperation = OperationFactory.newPaymentOperation(user2.getBubiAddress(), user1.getBubiAddress(), assetCode, transferAmount);
            fee = getOperationService().newEvalTransaction(user1.getBubiAddress()).buildAddOperation(bcOperation).commit();
            Transaction transferTransaction = getOperationService().newTransaction(user1.getBubiAddress());
            transferTransaction
                    .buildAddOperation(bcOperation)
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

```

> 这里使用的OperationFactory是对操作的简单参数封装，应用调用方可根据实际的使用参数自行封装操作对象，这里列出所有可以进行的操作供参考。这里列出的是操作对象，所有的操作创建方法都应使用对应操作对象的Builder对象，比如创建账户操作需要CreateAccountOperation.Builder对象来创建。
```java
// 创建账户操作
CreateAccountOperation
// 调用合约方法（并不映射底层操作，仅仅是访问方法）
InvokeContractOperation
// 发行资产操作
IssueAssetOperation
// 转移资产操作
PaymentOperation
// 设置/修改metadata操作
SetMetadataOperation
// 设置/修改权重/签名列表操作
SetSignerWeightOperation
// 设置/修改交易门限操作
SetThresholdOperation
```  

   
### 3设置和修改metadata
   

```java
/**
 * 设置和修改metadata
 */
@Test
    public void updateMetadata(){
        try {
            BlockchainKeyPair user = createAccountOperation();

            String key1 = "boot自定义key1";
            String key2 = "boot自定义key2";

            SetMetadata setMetadata = getQueryService().getAccount(user.getBubiAddress(), key1);
            setMetadata.setValue("这是新设置的value1");
            BcOperation bcOperation = OperationFactory.newUpdateSetMetadataOperation(setMetadata);
            EvalTransaction newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            TestTxResult fee = newAcctEval.buildAddOperation(bcOperation).commit();
            Transaction updateMetadataTransaction = getOperationService().newTransaction(user.getBubiAddress());
            
            updateMetadataTransaction
                    .buildAddOperation(bcOperation)
                    .buildAddFee(fee.getRealFee())
                    .buildAddSigner(user.getPubKey(), user.getPriKey())
                    .commit();

            Account account = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("修改metadata结果:" + GsonUtil.toJson(account.getMetadatas()));
            Assert.assertTrue("修改metadata1结果,失败", Arrays.stream(account.getMetadatas())
                    .anyMatch(setMetadata1 -> "这是新设置的value1".equals(setMetadata1.getValue())));


            bcOperation = OperationFactory.newSetMetadataOperation("newMetadataKey2", "newMetadataValue2");
            
            newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            fee = newAcctEval.buildAddOperation(bcOperation).commit();
            Transaction newMetadataTransaction = getOperationService().newTransaction(user.getBubiAddress());
            newMetadataTransaction
                    .buildAddOperation(bcOperation)
                    .buildAddFee(fee.getRealFee())
                    .buildAddSigner(user.getPubKey(), user.getPriKey())
                    .commit();

            Account account2 = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("新建metadata结果:" + GsonUtil.toJson(account2.getMetadatas()));
            Assert.assertTrue("新建metadata结果,失败", Arrays.stream(account2.getMetadatas())
                    .anyMatch(setMetadata1 -> "newMetadataValue2".equals(setMetadata1.getValue())));


            SetMetadata setMetadata2 = getQueryService().getAccount(user.getBubiAddress(), key2);
            setMetadata2.setValue("这是新设置的value222");
            
            bcOperation = OperationFactory.newSetMetadataOperation(setMetadata2.getKey(), setMetadata2.getValue(), setMetadata2.getVersion());
            newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            fee = newAcctEval.buildAddOperation(bcOperation).commit();
            Transaction updateMetadataTransaction2 = getOperationService().newTransaction(user.getBubiAddress());
            updateMetadataTransaction2
                    .buildAddOperation(bcOperation)
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
```

> 这里需要注意，如果是修改metadata值，必须先查询出来然后通过将整个SetMetadata对象传入到修改metadata操作中去，因为在修改metadata是需要做版本号递增控制的，如果自行提供版本也可以，SDK在生成操作时默认的行为是将传入的版本号+1。这种查询再修改可以避免自己做版本号管理。

### 4设置 修改权重

```java
/**
 * 设置/修改权重
 */
 @Test
    public void setSignerWeight(){
        try {
            BlockchainKeyPair user = createAccountOperation();

            BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
            
            BcOperation bcOperation = OperationFactory.newSetSignerWeightOperation(keyPair.getBubiAddress(), 8);
            EvalTransaction newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            TestTxResult fee = newAcctEval.buildAddOperation(bcOperation).commit();
            getOperationService()
                    .newTransaction(user.getBubiAddress())
                    .buildAddFee(fee.getRealFee())
                    .buildAddOperation(bcOperation)
                    .commit(user.getPubKey(), user.getPriKey());

            Account account = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("增加一个签名人权重8:" + GsonUtil.toJson(account.getPriv()));
            Assert.assertTrue("增加一个签名人权重8,失败", account.getPriv().getSigners().stream()
                    .anyMatch(signer -> signer.getAddress().equals(keyPair.getBubiAddress()) && signer.getWeight() == 8));
            
            bcOperation = OperationFactory.newSetSignerWeightOperation(20);
            newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            fee = newAcctEval.buildAddOperation(bcOperation).commit();
            Transaction setSignerWeightTransaction = getOperationService().newTransaction(user.getBubiAddress());
            TransactionCommittedResult setSignerWeightResult = setSignerWeightTransaction
            		.buildAddFee(fee.getRealFee())
                    .buildAddOperation(bcOperation)
                    .commit(user.getPubKey(), user.getPriKey());

            resultProcess(setSignerWeightResult, "修改权重结果状态:");

            Account account2 = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("修改权重到20:" + GsonUtil.toJson(account2.getPriv()));
            Assert.assertEquals("修改权重到20,失败", 20, account2.getPriv().getMasterWeight());


            bcOperation = OperationFactory.newSetSignerWeightOperation(keyPair.getBubiAddress(), 0);
            
            newAcctEval = getOperationService().newEvalTransaction(user.getBubiAddress());
            fee = newAcctEval.buildAddOperation(bcOperation).commit();
            getOperationService()
                    .newTransaction(user.getBubiAddress())
                    .buildAddFee(fee.getRealFee())
                    .buildAddOperation(bcOperation)
                    .commit(user.getPubKey(), user.getPriKey());

            Account account3 = getQueryService().getAccount(user.getBubiAddress());
            LOGGER.info("移除一个签名人:" + GsonUtil.toJson(account3.getPriv()));
            Assert.assertTrue("移除一个签名人,失败", account3.getPriv().getSigners().stream()
                    .noneMatch(signer -> signer.getAddress().equals(keyPair.getBubiAddress())));
        } catch (SdkException e) {
            e.printStackTrace();
        }

    }

```

### 5设置 修改门限

```java
/**
 * 设置/修改门限
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

```

### 6合约调用

```java

/**
 * 合约调用
 */
public void invokeContract(){
    BlockchainKeyPair user = createAccountOperation();
    BlockchainKeyPair user2 = createAccountOperation();

    TransactionCommittedResult result = getOperationService()
            .newTransaction(user.getBubiAddress())
            .buildAddOperation(OperationFactory.newInvokeContractOperation(user2.getBubiAddress(), "inputdata")).buildAddFee(500000)
            .commit(user.getPubKey(), user.getPriKey());

    resultProcess(result, "合约调用。。。");

    TransactionHistory transactionHistory = getQueryService().getTransactionHistoryByHash(result.getHash());
}
```

> 合约定义通过创建账号传入script定义.

### 7业务分支返回形式

这里需要明确指出，如果提交的交易并没有正常处理，那么返回的信息SDK将统一处理成SdkException返回，这是一个可预期的异常，调用方必须对其处理，具体的错误码和错误信息描述可以参考底层3.0文档。这里做个示例:
```java
/**
 * 参数错误，公钥非法
 *
 * @see cn.bubi.access.adaptation.blockchain.exception.BlockchainError.WRONG_ARGUMENT
 */
public void illegalPublicKeyTest(){

    BlockchainKeyPair user = createAccountOperation();

    try {
        Transaction setSignerWeightTransaction = getOperationService().newTransaction(user.getBubiAddress());
        setSignerWeightTransaction
                .buildAddOperation(OperationFactory.newSetSignerWeightOperation(14))
                .buildAddFee(500000)
                .commit("illegal public key", user.getPriKey());
    } catch (SdkException e) {
        LOGGER.info(" SdkException:" + GsonUtil.toJson(e));
        //  对业务提交交易失败时可以检查失败原因做对应的失败操作
    }

}

```

> 大多数失败情况的业务就是回滚数据库和转换调用方异常，主要是确保，交易失败时不要继续执行调用方正常业务

```
## 8支付BU币 ##
发起人支付给他人BU币。
```java

/**
 * 
 */
public void payCoin(){

    try{
    TransactionCommittedResult result = getOperationService()
	            .newTransaction(userPay.getBubiAddress())
	            .buildAddOperation(OperationFactory.newPayCoinOperation(userGet.getBubiAddress(), 2000))
	            .buildAddFee(500000)
	            .commit(userPay.getPubKey(), userPay.getPriKey());
    
    } catch (SdkException e) {
        e.printStackTrace();
    }
}
```
## 9-15 评估费用 ##
评估创建账户，发行资产，转移资产，修改权限，门限，元数据，支付BU币的费用
```java

/**
 *创建账户操作的费用评估 
 */
public class Demo_1_EvalCreateAccountOper {
//  创始者
  public static void main(String[] args) throws SdkException{
  		SDKEngine engine = SDKEngine.getInstance().configSdk();
  		long fee = evalNewActOper(engine);
      
  		System.out.println("新建账户的费用： " + fee);
  		System.exit(-1);
  }


  /**
   * 创建账户操作
   */
  public static long evalNewActOper(SDKEngine engine){
      try {
          EvalTransaction newAcctEval = engine.getOperationService().newEvalTransaction(engine.getSdkProperties().getAddress());

          BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
          System.out.println("" + GsonUtil.toJson(keyPair));
          
          BlockchainKeyPair user1 = SecureKeyGenerator.generateBubiKeyPair();
          BlockchainKeyPair user2 = SecureKeyGenerator.generateBubiKeyPair();
          
          CreateAccountOperation createAccountOper = new CreateAccountOperation.Builder()
        		  // 要生成的账号的地址
                  .buildDestAddress(keyPair.getBubiAddress())
                  // 合约脚本
                  //.buildScript("function main(input) { /*do what ever you want*/ }")
                  .buildAddInitBalance(100000000000L)
                  .buildAddInitInput("")
                  // metadatas 描述元数据
                  .buildAddMetadata("key1", "自定义value1").buildAddMetadata("key2", "自定义value2")
                  // 权限部分
                  .buildPriMasterWeight(15) //默认的要生成账户的权限值
                  .buildPriTxThreshold(15)  //签名账户列表中不指定门限值默认的操作门限值
                  // 签名账户列表中特定指定的的操作权限值 共6种操作 1-创建账户2-发布资产3-发布资产4-设置元数据5-操作关联账户权限6-设置门限
                  .buildAddPriTypeThreshold(OperationTypeV3.CREATE_ACCOUNT, 8)
                  .buildAddPriTypeThreshold(OperationTypeV3.SET_METADATA, 6)
                  .buildAddPriTypeThreshold(OperationTypeV3.ISSUE_ASSET, 4)
                  .buildAddPriSigner(user1.getBubiAddress(), 10)
                  .buildAddPriSigner(user2.getBubiAddress(), 10)
                  .build();

          TestTxResult result = newAcctEval.buildAddOperation(createAccountOper).commit();
          return result.getRealFee();
      } catch (SdkException e) {
          e.printStackTrace();
      }
      
      return -1l;
  }
}


支付BU币的费用评估
public class Demo_7_EvalPayCoinOper {
	private static Logger logger = LoggerFactory.getLogger(Demo_7_EvalPayCoinOper.class);

	public static void main(String[] args) throws Exception {
		SDKEngine engine = SDKEngine.getInstance().configSdk();
		evalPayCoinOper(engine);
		System.exit(-1);
	}

	public static void evalPayCoinOper(SDKEngine engine)throws Exception {
		String user1Addr = Demo_1_CreateAccount.getNewActAddr(engine);
		String user2Addr = Demo_1_CreateAccount.getNewActAddr(engine);
		String user3Addr = Demo_1_CreateAccount.getNewActAddr(engine);
		
		EvalTransaction transaction = engine.getOperationService().newEvalTransaction(user1Addr);
		
		BcOperation payCoin = new PayCoinOperation.Builder().buildTargetAddress(user2Addr).buildAmount(10000).build();
		BcOperation payCoin2 = new PayCoinOperation.Builder().buildTargetAddress(user3Addr).buildAmount(100000).build();
		
		TestTxResult evalRt = transaction.buildAddOperation(payCoin).buildAddOperation(payCoin2).commit();	
		
		logger.info("评估结果："+ JSON.toJSONString(evalRt));
		System.out.println("评估该交易的实际费用：" + evalRt.getRealFee());
	}
}
```
> 建议调用方在调用系统发起交易时使用账户池，而用户自身操作建议由用户自身发起，发起人信息会记录到区块中存储。

![结束][1]
 


  [1]: http://file26.mafengwo.net/M00/B6/C3/wKgB4lJyOiGAOryaAA6u2rm6dKs69.jpeg





