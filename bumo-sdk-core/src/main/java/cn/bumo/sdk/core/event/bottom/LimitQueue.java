package cn.bumo.sdk.core.event.bottom;

import java.util.LinkedList;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class LimitQueue<E>{

    private int limit; // 队列长度

    private LinkedList<E> queue = new LinkedList<>();

    public LimitQueue(int limit){
        this.limit = limit;
    }

    public void offer(E e){
        if (queue.size() >= limit) {
            queue.poll();
        }
        queue.offer(e);
    }

    public boolean exist(E e){
        return queue.contains(e);
    }

}
