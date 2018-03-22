package cn.bumo.sdk.core.pool;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 账户池对象
 */
public interface SponsorAccountPool{

    /**
     * 向账户池添加可用账户
     *
     * @param sponsorAccounts 可用账户数组，由于账户池并发控制，尽量避免循环调用
     */
    void addSponsorAccount(SponsorAccount... sponsorAccounts);


    /**
     * 获得可用发起账户
     */
    SponsorAccount getRichSponsorAccount();

    /**
     * 通知恢复
     *
     * @param address 待恢复账户地址
     */
    void notifyRecover(String address);


}
