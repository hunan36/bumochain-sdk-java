package cn.bumo.sdk.core.operation;

import cn.bumo.sdk.core.exception.SdkException;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
@FunctionalInterface
public interface BuildConsume{

    void build() throws SdkException;

}
