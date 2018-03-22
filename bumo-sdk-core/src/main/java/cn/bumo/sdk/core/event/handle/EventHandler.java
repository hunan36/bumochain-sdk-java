package cn.bumo.sdk.core.event.handle;

import cn.bumo.sdk.core.event.message.EventMessage;
import cn.bumo.sdk.core.event.source.EventSource;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 事件处理器
 */
public interface EventHandler{

    /**
     * 事件源
     */
    EventSource eventSource();

    /**
     * 事件处理器
     */
    void onEvent(EventMessage message);

}
