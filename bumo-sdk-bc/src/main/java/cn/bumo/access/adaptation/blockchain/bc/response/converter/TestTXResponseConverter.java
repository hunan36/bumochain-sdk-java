package cn.bumo.access.adaptation.blockchain.bc.response.converter;

import java.io.InputStream;

import com.alibaba.fastjson.JSONObject;

import cn.bubi.baas.utils.http.HttpServiceContext;
import cn.bubi.baas.utils.http.ResponseConverter;
import cn.bubi.baas.utils.http.agent.ServiceRequest;
import cn.bubi.baas.utils.http.converters.JsonResponseConverter;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;
/***
 * 评估费用的响应转换器
 * @author 布萌
 *
 */
public class TestTXResponseConverter implements ResponseConverter{

	private JsonResponseConverter jsonResponseConverter = new JsonResponseConverter(ServiceResponse.class);
	@Override
	public Object getResponse(ServiceRequest request, InputStream responseStream, HttpServiceContext serviceContext)
			throws Exception {
		ServiceResponse serviceResponse = (ServiceResponse) jsonResponseConverter.getResponse(request, responseStream, null);
        if (serviceResponse == null || !"0".equals(serviceResponse.getErrorCode())) {
            return null;
        }//TestTransaction
        return JSONObject.toJavaObject(serviceResponse.getResult(), TestTxResult.class);
	}

}
