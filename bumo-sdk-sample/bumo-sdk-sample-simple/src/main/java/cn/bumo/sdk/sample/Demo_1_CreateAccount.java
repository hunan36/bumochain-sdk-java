package cn.bumo.sdk.sample;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.access.utils.blockchain.SecureKeyGenerator;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.model.TransactionCommittedResult;
import cn.bumo.sdk.core.utils.GsonUtil;

public class Demo_1_CreateAccount {
//  创始者
  public static void main(String[] args) throws SdkException{
  		SDKEngine engine = SDKEngine.getInstance().configSdk();
  		NewActResult caRst = newActOper(engine);
      
  		System.out.println("刚操作的交易hash = " + caRst.getTxHash());
  		/**
  		 * 做完交易后只能查下面这个接口
  		 */
  		System.out.println("结果：" + engine.getRpcService().getTransactionResultByHash(caRst.getTxHash()));
  }
  /***
   * 获取新生账户的账户地址哈希值
   * @param engine
   * @return
   */
  public static String getNewActAddr(SDKEngine engine) {
	  NewActResult nar = newActOper(engine);
	  String txHash = nar.getTxHash();
		if(txHash != null && !txHash.equals("") ) {
			BlockchainKeyPair kp = nar.getKeyPairs().get(0);
			
			if(kp != null && kp.getBubiAddress() != null  && !kp.getBubiAddress().equals(""))
				return kp.getBubiAddress();
			
		}
	  return null;
  }

  /**
   * 创建账户操作
   */
  public static NewActResult newActOper(SDKEngine engine){
      try {
          Transaction transaction = engine.getOperationService().newTransaction(engine.getSdkProperties().getAddress());

          BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
          System.out.println("" + GsonUtil.toJson(keyPair));
          
          BlockchainKeyPair user1 = SecureKeyGenerator.generateBubiKeyPair();
          
          BlockchainKeyPair user2 = SecureKeyGenerator.generateBubiKeyPair();
          
          
          
          CreateAccountOperation createAccountOper = new CreateAccountOperation.Builder()
        		  // 要生成的账号的地址
                  .buildDestAddress(keyPair.getBubiAddress())
                  // 合约脚本
                  //.buildScript("function main(input) { /*do what ever you want*/ }")
                  .buildAddInitBalance(10000000000000L)
                  //.buildAddInitInput("")//干嘛?智能合约的入参 var input的值?就算智能合约也可为空 默认为空。"" or null?
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

          TransactionCommittedResult result = transaction.buildAddOperation(createAccountOper)
                  .buildTxMetadata("交易metadata")
                  .buildAddSigner(engine.getSdkProperties().getPublicKey(), engine.getSdkProperties().getPrivateKey())
                  .buildAddFee(500000)
                  .commit();

          System.out.println("\n------------------------------------------------");
          System.out.println(GsonUtil.toJson(result));
          return NewActResult.newCreateAccountResult().addKeyPair(keyPair).addKeyPair(user1).addKeyPair(user2).setTxHash(result.getHash());
      } catch (SdkException e) {
          e.printStackTrace();
      }
      
      return null;
  }


}
