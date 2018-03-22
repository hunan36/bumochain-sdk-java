package cn.bumo.sdk.sample;

import cn.bumo.access.adaptation.blockchain.bc.response.operation.SetMetadata;
import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.sdk.core.config.SDKEngine;
import cn.bumo.sdk.core.operation.OperationFactory;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.utils.GsonUtil;

public class Demo_4_OperMetaData {

	public static void main(String[] args) throws Exception {
		SDKEngine engine = SDKEngine.getInstance().configSdk();
		operMetaData(engine);
		System.exit(-1);
	}

	
	public static void operMetaData(SDKEngine engine) throws Exception  {
		
		NewActResult nar = Demo_1_CreateAccount.newActOper(engine);
		
		if(nar!=null && nar.getTxHash()!=null && !nar.getTxHash().equals("")) {
			BlockchainKeyPair user = nar.getKeyPairs().get(0);
			
			Transaction newMetadataTransaction = engine.getOperationService().newTransaction(user.getBubiAddress());
	        newMetadataTransaction
	                .buildAddOperation(OperationFactory.newSetMetadataOperation("key1", "value1"))
	                .buildAddFee(500000)
	                .buildAddSigner(user.getPubKey(), user.getPriKey())
	                .commit();
	        
	        System.out.println("=========================设置元数据：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user.getBubiAddress())));
	        
	        SetMetadata setMetadata = engine.getQueryService().getAccount(user.getBubiAddress(), "key1");
            setMetadata.setValue("这是新设置的value1");
	        Transaction updateMetadataTransaction = engine.getOperationService().newTransaction(user.getBubiAddress());
            updateMetadataTransaction
                    .buildAddOperation(OperationFactory.newUpdateSetMetadataOperation(setMetadata))
                    .buildAddFee(500000)
                    .buildAddSigner(user.getPubKey(), user.getPriKey())
                    .commit();
	        
            System.out.println("=========================修改元数据后：\n"+GsonUtil.toJson(engine.getQueryService().getAccount(user.getBubiAddress())));
		}
	}
}
