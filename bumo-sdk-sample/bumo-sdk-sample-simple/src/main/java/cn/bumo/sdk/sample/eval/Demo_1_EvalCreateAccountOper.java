package cn.bumo.sdk.sample.eval;

import cn.bumo.access.adaptation.blockchain.bc.OperationTypeV3;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.access.utils.blockchain.SecureKeyGenerator;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.impl.CreateAccountOperation;
import cn.bumo.sdk.core.transaction.EvalTransaction;
import cn.bumo.sdk.core.utils.GsonUtil;
/***
 * 评估创建账户
 * @author 布萌
 *
 */
public class Demo_1_EvalCreateAccountOper {
	
//  创始者
  public static void main(String[] args) throws SdkException{
  		SDKEngine engine = SDKEngine.getInstance();
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
