package cn.bumo.sdk.core.event.source;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 简单的事件定义
 */
public interface EventSource{

    /**
     * 事件代码
     */
    String getCode();

    /**
     * 事件名
     */
    String getName();

}
