package cn.bumo.sdk.core.pool;

import cn.bumo.sdk.core.event.handle.AbstractEventHandler;
import cn.bumo.sdk.core.event.message.TransactionExecutedEventMessage;
import cn.bumo.sdk.core.event.source.EventSourceEnum;
import cn.bumo.sdk.core.spi.BcOperationService;
import cn.bumo.access.utils.spring.StringUtils;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 对外提供的账户池操作
 */
public class SponsorAccountPoolManager extends AbstractEventHandler<TransactionExecutedEventMessage>{

    private SponsorAccountFactory sponsorAccountFactory;
    private SponsorAccountPool sponsorAccountPool;

    public SponsorAccountPoolManager(SponsorAccountFactory sponsorAccountFactory){
        super(EventSourceEnum.TRANSACTION_NOTIFY.getEventSource(), TransactionExecutedEventMessage.class);
        this.sponsorAccountFactory = sponsorAccountFactory;
    }

    /**
     * 初始化
     */
    public void initPool(BcOperationService operationService, String address, String publicKey, String privateKey, Integer size, String filePath, String sponsorAccountMark){
        this.sponsorAccountPool = sponsorAccountFactory.initPool(operationService, address, publicKey, privateKey, size, filePath, sponsorAccountMark);
    }

    /**
     * 获取可用发起账户
     */
    public SponsorAccount getRichSponsorAccount(){
        if (sponsorAccountPool == null) {
            throw new IllegalStateException("invoke method getRichSponsorAccount must be method initPool after!");
        }
        return sponsorAccountPool.getRichSponsorAccount();
    }

    /**
     * 通知恢复
     */
    public void notifyRecover(String address){
        if (sponsorAccountPool == null || StringUtils.isEmpty(address)) {
            return;
        }
        sponsorAccountPool.notifyRecover(address);
    }

    @Override
    public void processMessage(TransactionExecutedEventMessage message){
        notifyRecover(message.getSponsorAddress());
    }

}
