package cn.bumo.access.adaptation.blockchain.bc;

import cn.bubi.baas.utils.http.HttpAction;
import cn.bubi.baas.utils.http.HttpMethod;
import cn.bubi.baas.utils.http.HttpService;
import cn.bubi.baas.utils.http.RequestBody;
import cn.bubi.baas.utils.http.RequestParam;
import cn.bumo.access.adaptation.blockchain.bc.request.SubTransactionRequest;
import cn.bumo.access.adaptation.blockchain.bc.request.test.TestTXReq;
import cn.bumo.access.adaptation.blockchain.bc.response.Account;
import cn.bumo.access.adaptation.blockchain.bc.response.Hello;
import cn.bumo.access.adaptation.blockchain.bc.response.TransactionHistory;
import cn.bumo.access.adaptation.blockchain.bc.response.converter.GetAccountMetadataResponseConverter;
import cn.bumo.access.adaptation.blockchain.bc.response.converter.GetAccountResponseConverter;
import cn.bumo.access.adaptation.blockchain.bc.response.converter.GetLedgerResponseConverter;
import cn.bumo.access.adaptation.blockchain.bc.response.converter.GetServiceResponse;
import cn.bumo.access.adaptation.blockchain.bc.response.converter.GetTransactionHistoryResponse;
import cn.bumo.access.adaptation.blockchain.bc.response.converter.HelloResponseConverter;
import cn.bumo.access.adaptation.blockchain.bc.response.converter.ServiceResponse;
import cn.bumo.access.adaptation.blockchain.bc.response.converter.SubmitTranactionResponseConverter;
import cn.bumo.access.adaptation.blockchain.bc.response.converter.TestTXResponseConverter;
import cn.bumo.access.adaptation.blockchain.bc.response.ledger.Ledger;
import cn.bumo.access.adaptation.blockchain.bc.response.operation.SetMetadata;
import cn.bumo.access.adaptation.blockchain.bc.response.test.TestTxResult;

@HttpService
public interface RpcService{

    /**
     * 获取账号信息
     */
    @HttpAction(path = "/getAccount", method = HttpMethod.GET, responseConverter = GetAccountResponseConverter.class)
    Account getAccount(@RequestParam(name = "address") String address);

    /**
     * 获取底层基本信息
     */
    @HttpAction(path = "/hello", method = HttpMethod.GET, responseConverter = HelloResponseConverter.class)
    Hello hello();

    /**
     * 获取账号信息
     */
    @HttpAction(path = "/getAccount", method = HttpMethod.GET, responseConverter = GetAccountMetadataResponseConverter.class)
    SetMetadata getAccountMetadata(@RequestParam(name = "address") String address, @RequestParam(name = "key") String key);

    /**
     * 获取账号信息
     */
    @HttpAction(path = "/getLedger", method = HttpMethod.GET, responseConverter = GetLedgerResponseConverter.class)
    Ledger getLedger();

    /**
     * 获取账号信息
     */
    @HttpAction(path = "/getLedger", method = HttpMethod.GET, responseConverter = GetLedgerResponseConverter.class)
    Ledger getLedgerBySeq(@RequestParam(name = "seq") long seq);

    /**
     * 提交
     */
    @HttpAction(path = "/submitTransaction", method = HttpMethod.POST, responseConverter = SubmitTranactionResponseConverter.class)
    String submitTransaction(@RequestBody SubTransactionRequest request);
    
    /**
     * 
     * @param addrs
     * @return
     */
    @HttpAction(path = "/testTransaction", method = HttpMethod.POST, responseConverter = TestTXResponseConverter.class)
    TestTxResult testTransaction(@RequestBody TestTXReq request);

    /**
     * 根据地址获取交易历史信息
     */
    @HttpAction(path = "/getTransactionHistory", method = HttpMethod.GET, responseConverter = GetTransactionHistoryResponse.class)
    TransactionHistory getTransactionHistoryByAddress(@RequestParam(name = "address") String address);

    /**
     * 根据地址获取交易历史信息
     */
    @HttpAction(path = "/getTransactionHistory", method = HttpMethod.GET, responseConverter = GetTransactionHistoryResponse.class)
    TransactionHistory getTransactionHistoryBySeq(@RequestParam(name = "ledger_seq", required = false) Long seq, @RequestParam(name = "start") int start, @RequestParam(name = "limit") int limit);

    /**
     * 根据hash获取交易信息
     */
    @HttpAction(path = "/getTransactionHistory", method = HttpMethod.GET, responseConverter = GetTransactionHistoryResponse.class)
    TransactionHistory getTransactionHistoryByHash(@RequestParam(name = "hash") String hash);

    /**
     * 根据hash获取交易信息
     */
    @HttpAction(path = "/getTransactionHistory", method = HttpMethod.GET, responseConverter = GetServiceResponse.class)
    ServiceResponse getTransactionResultByHash(@RequestParam(name = "hash") String hash);
    

}

