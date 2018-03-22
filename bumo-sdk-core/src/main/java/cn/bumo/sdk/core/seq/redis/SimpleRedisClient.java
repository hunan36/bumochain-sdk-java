package cn.bumo.sdk.core.seq.redis;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public interface SimpleRedisClient{


    void setEx(byte[] key, byte[] value, int seconds);

    byte[] get(byte[] key);

}
