package cn.bumo.access.adaptation.blockchain.bc.response.converter;

import cn.bubi.baas.utils.http.util.SerializeUtils;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class GetAccountMetadataResponseConverter extends AbstractResponseConverter{

    @Override
    public Object dealResult(ServiceResponse serviceResponse){
        Account account = SerializeUtils.deserializeAs(serviceResponse.getResult(), Account.class);
        if (account == null || account.getMetadatas() == null || account.getMetadatas().length < 1) {
            return null;
        }
        return account.getMetadatas()[0];
    }

}
