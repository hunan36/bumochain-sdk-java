package cn.bumo.access.adaptation.blockchain.bc.response.converter;

import com.alibaba.fastjson.JSONObject;

import cn.bumo.access.adaptation.blockchain.bc.response.TransactionHistory;

public class GetTransactionHistoryResponse extends AbstractResponseConverter{

    @Override
    public Object dealResult(ServiceResponse serviceResponse){
        if (!"0".equals(serviceResponse.getErrorCode())) {
            return null;
        }
        return JSONObject.toJavaObject(serviceResponse.getResult(), TransactionHistory.class);
    }

}
