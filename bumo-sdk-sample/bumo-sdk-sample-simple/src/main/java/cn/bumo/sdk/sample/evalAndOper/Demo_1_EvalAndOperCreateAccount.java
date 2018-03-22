package cn.bumo.sdk.sample.evalAndOper;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.access.utils.blockchain.SecureKeyGenerator;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.model.TransactionCommittedResult;
import cn.bumo.sdk.core.utils.GsonUtil;
import cn.bumo.sdk.sample.NewActResult;

public class Demo_1_EvalAndOperCreateAccount {

	public static void main(String[] args)  throws SdkException {
		// TODO Auto-generated method stub
		SDKEngine engine = SDKEngine.getInstance().configSdk();
		evalAndNewActOper(engine);
		System.exit(-1);
	}

	
	/**
	   * 创建账户操作
	   */
	  public static NewActResult evalAndNewActOper(SDKEngine engine){
	      try {
	          //创建账户交易
	          Transaction transaction = engine.getOperationService().newTransaction(engine.getSdkProperties().getAddress());


	          BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
	          System.out.println("key pair" + GsonUtil.toJson(keyPair));

	          BlockchainKeyPair user1 = SecureKeyGenerator.generateBubiKeyPair();

	          BlockchainKeyPair user2 = SecureKeyGenerator.generateBubiKeyPair();



	          CreateAccountOperation createAccountOper = new CreateAccountOperation.Builder()
	                  // 要生成的账号的地址
	                  .buildDestAddress(keyPair.getBubiAddress())
	                  // 合约脚本
	                  //.buildScript("function main(input) { /*do what ever you want*/ }")
	                  .buildAddInitBalance(100000000L)
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
	          
	          
	          EvalTransaction newAcctEval = engine.getOperationService().newEvalTransaction(engine.getSdkProperties().getAddress());
	          TestTxResult testTx = newAcctEval.buildAddOperation(createAccountOper).commit();

	          long fee = testTx.getRealFee();


	          TransactionCommittedResult result = transaction.buildAddOperation(createAccountOper)
	                  .buildTxMetadata("交易metadata")
	                  .buildAddSigner(engine.getSdkProperties().getPublicKey(), engine.getSdkProperties().getPrivateKey())
	                  .buildAddFee(fee)
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
