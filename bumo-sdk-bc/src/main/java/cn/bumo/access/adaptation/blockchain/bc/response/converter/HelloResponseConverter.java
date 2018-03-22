package cn.bumo.access.adaptation.blockchain.bc.response.converter;

import cn.bumo.access.adaptation.blockchain.bc.response.Hello;
import cn.bubi.baas.utils.http.HttpServiceContext;
import cn.bubi.baas.utils.http.ResponseConverter;
import cn.bubi.baas.utils.http.agent.ServiceRequest;
import cn.bubi.baas.utils.http.converters.StringResponseConverter;
import cn.bubi.baas.utils.http.util.SerializeUtils;

import java.io.InputStream;

/**
 * @author 布萌
 * @since 18/3/20 下午5:47.
 */
public class HelloResponseConverter implements ResponseConverter{

    @Override
    public Object getResponse(ServiceRequest serviceRequest, InputStream inputStream, HttpServiceContext httpServiceContext) throws Exception{
        String jsonResponse = (String) StringResponseConverter.INSTANCE.getResponse(serviceRequest, inputStream, null);
        if (jsonResponse == null) {
            return null;
        } else {
            jsonResponse = jsonResponse.trim();
            return SerializeUtils.deserializeAs(jsonResponse, Hello.class);
        }
    }

}
