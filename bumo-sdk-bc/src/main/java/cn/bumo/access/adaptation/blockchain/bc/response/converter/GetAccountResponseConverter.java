package cn.bumo.access.adaptation.blockchain.bc.response.converter;

import cn.bubi.baas.utils.http.util.SerializeUtils;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;

/**
 * 解析rpc返回结果中的result
 *
 * @author 布萌
 */
public class GetAccountResponseConverter extends AbstractResponseConverter{

    @Override
    public Object dealResult(ServiceResponse serviceResponse){
        //		return JSONObject.toJavaObject(serviceResponse.getResult(), Account.class);
        return SerializeUtils.deserializeAs(serviceResponse.getResult(), Account.class);
    }


}
