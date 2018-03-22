package cn.bumo.sdk.core.event.bottom;

import cn.bubi.baas.utils.encryption.utils.HexFormat;
import cn.bubi.blockchain.adapter.BlockChainAdapter;
import cn.bumo.blockchain.adapter3.Chain;
import cn.bumo.blockchain.adapter3.Overlay;
import cn.bumo.sdk.core.event.EventBusService;
import cn.bumo.sdk.core.event.message.LedgerSeqEventMessage;
import cn.bumo.sdk.core.event.message.TransactionExecutedEventMessage;
import cn.bumo.sdk.core.event.source.EventSourceEnum;
import cn.bumo.sdk.core.exception.SdkError;
import cn.bumo.sdk.core.exception.SdkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.bumo.access.utils.spring.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 底层mq消息处理器
 */
public class BlockchainMqHandler{

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockchainMqHandler.class);
    private static final Pattern URI_PATTERN = Pattern.compile("(ws://)(.*)(:[\\d+])");

    private BlockChainAdapter mQBlockChainExecute;
    private TxMqHandleProcess mqHandleProcess;
    private EventBusService eventBusService;
    private String eventUri;
    private String host;


    public BlockchainMqHandler(String eventUri, TxMqHandleProcess mqHandleProcess, EventBusService eventBusService) throws SdkException{
        this.eventUri = eventUri;
        this.host = getHostByUri(eventUri);
        this.mqHandleProcess = mqHandleProcess;
        this.eventBusService = eventBusService;
    }

    private static String getHostByUri(String eventUri) throws SdkException{
        try {
            Matcher matcher = URI_PATTERN.matcher(eventUri);
            if (!matcher.find()) {
                throw new SdkException(SdkError.PARSE_URI_ERROR);
            }
            return matcher.group(2);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SdkException(SdkError.PARSE_URI_ERROR);
        }
    }

    public void init(){
        // 初始化链接
        BlockChainAdapter mQBlockChainExecute = new BlockChainAdapter(eventUri);

        // 接收握手消息
        mQBlockChainExecute.AddChainResponseMethod(Overlay.ChainMessageType.CHAIN_HELLO_VALUE, this :: onHelloCallback);
        
        // 接收订阅消息
        mQBlockChainExecute.AddChainResponseMethod(Overlay.ChainMessageType.CHAIN_SUBSCRIBE_TX_VALUE, this :: subscribeTx);

        // 交易
        // mQBlockChainExecute.AddChainMethod(Overlay.ChainMessageType.CHAIN_TX_STATUS_VALUE, this :: txCallback);
        
        //共识后回调
        mQBlockChainExecute.AddChainMethod(Overlay.ChainMessageType.CHAIN_TX_ENV_STORE_VALUE, this :: afterConsensusCallback);

        // 区块seq
        mQBlockChainExecute.AddChainMethod(Overlay.ChainMessageType.CHAIN_LEDGER_HEADER_VALUE, this :: ledgerSeqCallback);

        Thread thread = new Thread(() -> {
            long future = LocalDateTime.now().plusMinutes(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long wait = TimeUnit.MINUTES.toMillis(1);
            boolean helloSuccess = false;
            while (wait > 0) {
                if (!mQBlockChainExecute.IsConnected()) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        //
                        LOGGER.error("", e);
                        Thread.currentThread().interrupt();
                    }
                    wait = future - System.currentTimeMillis();
                } else {
                    Overlay.ChainHello.Builder chain_hello = Overlay.ChainHello.newBuilder();
                    chain_hello.setTimestamp(new Date().getTime());
                    mQBlockChainExecute.Send(Overlay.ChainMessageType.CHAIN_HELLO_VALUE, chain_hello.build().toByteArray());
                    helloSuccess = true;
                    break;
                }
            }
            LOGGER.debug("hello success : " + helloSuccess);
        }, "hello-" + this.host);
        thread.start();


        this.mQBlockChainExecute = mQBlockChainExecute;
    }

    public void destroy(){
        if (mQBlockChainExecute != null) {
            mQBlockChainExecute.Stop();
        }
    }


    /**
     * 接收交易结果
     */
//    private void txCallback(byte[] msg, int length){
//        try {
//            Overlay.ChainTxStatus chainTx = Overlay.ChainTxStatus.parseFrom(msg);
//            TransactionExecutedEventMessage message = new TransactionExecutedEventMessage();
//            message.setHash(chainTx.getTxHash());
//            message.setErrorCode(String.valueOf(chainTx.getErrorCode().getNumber()));
//            message.setSponsorAddress(chainTx.getSourceAddress());
//            message.setSequenceNumber(chainTx.getSourceAccountSeq());

//            if (StringUtils.isEmpty(message.getSponsorAddress())) {
//	            LOGGER.error("received empty source address. TransactionExecutedEventMessage : " + message);
//	            return;
//            }
//            if(chainTx.getStatus().getNumber() == Overlay.ChainTxStatus.TxStatus.FAILURE_VALUE) {
//                // 失败
//                message.setSuccess(false);
//                message.setErrorMessage(chainTx.getErrorDesc());
//                LOGGER.debug("交易失败errorcode：" + chainTx.getErrorCode().getNumber() + ",chainTx.getErrorDesc():" + chainTx.getErrorDesc() + ",status=" + chainTx.getStatus() + " ,交易hash:" + chainTx.getTxHash());
//                mqHandleProcess.process(message);
//
//            }
//
//        } catch (Exception e) {
//            LOGGER.error("接受交易结果异常", e);
//        }
//    }
    private void afterConsensusCallback(byte[] msg, int length) {
    	try {
    		Chain.TransactionEnvStore tranEnvStore = Chain.TransactionEnvStore.parseFrom(msg);
    		
    		int errCode = tranEnvStore.getErrorCode();
    		String txHash = HexFormat.byteToHex(tranEnvStore.getHash().toByteArray()).toLowerCase();
    		
    		TransactionExecutedEventMessage message = new TransactionExecutedEventMessage();
            message.setHash(HexFormat.byteToHex(tranEnvStore.getHash().toByteArray()).toLowerCase());
            message.setErrorCode(String.valueOf(tranEnvStore.getErrorCode()));
            message.setSponsorAddress(tranEnvStore.getTransactionEnv().getTransaction().getSourceAddress());
            message.setSequenceNumber(tranEnvStore.getTransactionEnv().getTransaction().getNonce());
    		//共识失败
    		if(errCode == 0) {
    			 System.out.println("交易成功errorcode：" + errCode +  " ,交易hash:" + txHash + ",错误信息："+tranEnvStore.getErrorDesc());
    			message.setSuccess(true);
    		} else {
    			System.out.println("交易成功errorcode：" + errCode +  " ,交易hash:" + txHash+",错误信息："+tranEnvStore.getErrorDesc());
    			message.setSuccess(false);
    			message.setErrorMessage(tranEnvStore.getErrorDesc());
    		}
    		
    		if (StringUtils.isEmpty(message.getSponsorAddress())) {
                LOGGER.error("received empty source address. TransactionExecutedEventMessage : " + message);
                return;
            }
    		
    		if (message.getSuccess() != null)
    			mqHandleProcess.process(message);
    	} catch(Exception e) {
    		
    	}
    }
    /**
     * 接收seq增加
     */
    private void ledgerSeqCallback(byte[] msg, int length){
        try {
            Chain.LedgerHeader ledger_header = Chain.LedgerHeader.parseFrom(msg);
            LOGGER.trace("================" + ledger_header.toString());

            LedgerSeqEventMessage seqEventMessage = new LedgerSeqEventMessage();
            seqEventMessage.setHost(host);
            seqEventMessage.setSeq(ledger_header.getSeq());

            eventBusService.publishEvent(EventSourceEnum.LEDGER_SEQ_INCREASE.getEventSource(), seqEventMessage);

        } catch (Exception e) {
            LOGGER.error("接收seq增加异常", e);
        }
    }
    
    private void subscribeTx(byte[] msg, int length){
    	try {
    		Overlay.ChainResponse chainResponse = Overlay.ChainResponse.parseFrom(msg);
    		LOGGER.trace("subscribeTx errorCode: " + chainResponse.getErrorCode() + ", desc: " + chainResponse.getErrorDesc());
    		
    	 } catch (Exception e) {
             LOGGER.error("接收seq增加异常", e);
         }
    }


    /**
     * 接收握手消息
     */
    private void onHelloCallback(byte[] msg, int length){
        LOGGER.debug("!!!!!receive hello successful , to host:" + host);
    }

}
