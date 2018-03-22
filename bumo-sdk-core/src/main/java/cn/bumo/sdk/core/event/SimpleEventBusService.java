package cn.bumo.sdk.core.event;

import cn.bumo.access.utils.concurrent.NamedThreadFactory;
import cn.bumo.sdk.core.event.handle.EventHandler;
import cn.bumo.sdk.core.event.message.EventMessage;
import cn.bumo.sdk.core.event.source.EventSource;
import cn.bumo.sdk.core.exec.ExecutorsFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 简单的事件通知总线
 */
public class SimpleEventBusService implements EventBusService{

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEventBusService.class);

    private static final Map<String, List<EventHandler>> EVENT_HANDLE_MAP = new ConcurrentHashMap<>();

    @Override
    public void clear(){
        EVENT_HANDLE_MAP.clear();
    }

    @Override
    public synchronized void addEventHandler(EventHandler eventHandle){
        List<EventHandler> eventHandlers = EVENT_HANDLE_MAP.computeIfAbsent(eventHandle.eventSource().getCode(), k -> new ArrayList<>());
        eventHandlers.add(eventHandle);
    }

    @Override
    public void publishEvent(EventSource eventSource, EventMessage eventMessage){
        String eventCode = eventSource.getCode();
        List<EventHandler> eventHandlers = EVENT_HANDLE_MAP.get(eventCode);
        if (eventHandlers == null || eventHandlers.isEmpty()) {
            LOGGER.debug("not found event handle , event code:" + eventCode);
            return;
        }

        eventHandlers.forEach(eventHandler -> ExecutorsFactory.getExecutorService().execute(() -> eventHandler.onEvent(eventMessage)));
    }

}
