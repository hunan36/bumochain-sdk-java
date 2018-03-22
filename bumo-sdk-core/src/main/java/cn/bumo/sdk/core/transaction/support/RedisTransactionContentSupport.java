package cn.bumo.sdk.core.transaction.support;

import cn.bumo.sdk.core.seq.redis.SimpleRedisClient;
import cn.bumo.sdk.core.spi.BcOperationService;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.model.TransactionSerializable;
import cn.bumo.sdk.core.utils.SerializeUtil;
import cn.bumo.access.utils.spring.Assert;
import cn.bumo.access.utils.spring.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 */
public class RedisTransactionContentSupport implements TransactionContentSupport{

    private SimpleRedisClient simpleRedisClient;
    private BcOperationService bcOperationService;

    private final int expect = (int) TimeUnit.DAYS.toSeconds(1);

    public RedisTransactionContentSupport(SimpleRedisClient simpleRedisClient, BcOperationService bcOperationService){
        this.simpleRedisClient = simpleRedisClient;
        this.bcOperationService = bcOperationService;
    }

    @Override
    public void put(String hash, Transaction transaction){
        simpleRedisClient.setEx(hash.getBytes(), SerializeUtil.serialize(transaction.forSerializable()), expect);
    }

    @Override
    public Transaction get(String hash){
        Assert.hasText(hash, "");
        if (StringUtils.isEmpty(hash)) {
            throw new IllegalArgumentException("get transaction hash must not be null");
        }

        byte[] redisObject = simpleRedisClient.get(hash.getBytes());
        if (redisObject == null) {
            return null;
        }

        TransactionSerializable transactionSerializable = SerializeUtil.deserialize(redisObject);
        return bcOperationService.continueTransaction(transactionSerializable);
    }

}
