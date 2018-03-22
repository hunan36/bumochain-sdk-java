package cn.bumo.sdk.core.event;

import cn.bumo.sdk.core.event.handle.EventHandler;
import cn.bumo.sdk.core.event.message.EventMessage;
import cn.bumo.sdk.core.event.source.EventSource;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public interface EventBusService{

    void clear();

    void addEventHandler(EventHandler eventHandle);

    void publishEvent(EventSource eventSource, EventMessage eventMessage);

}
