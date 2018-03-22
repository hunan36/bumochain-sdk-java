package cn.bumo.sdk.core.pool.defaults;

import cn.bumo.access.utils.blockchain.BlockchainKeyPair;
import cn.bumo.access.utils.blockchain.SecureKeyGenerator;
import cn.bumo.sdk.core.exception.SdkException;
import cn.bumo.sdk.core.operation.BcOperation;
import cn.bumo.sdk.core.pool.SponsorAccount;
import cn.bumo.sdk.core.pool.SponsorAccountConfig;
import cn.bumo.sdk.core.pool.SponsorAccountFactory;
import cn.bumo.sdk.core.pool.SponsorAccountPool;
import cn.bumo.sdk.core.spi.BcOperationService;
import cn.bumo.sdk.core.transaction.Transaction;
import cn.bumo.sdk.core.transaction.model.Signature;
import cn.bumo.sdk.core.utils.SwallowUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.bumo.access.utils.spring.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 布萌
 * @since 18/03/12 下午3:03.
 * 账户池工厂，主要负责初始化账户池
 */
public class DefaultSponsorAccountFactory implements SponsorAccountFactory{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSponsorAccountFactory.class);

    private static final int DEFAULT_CREATE_LIMIT = 100;
    private static final String RESOURCE_START = "classpath:";
    private static final String JAR_ENV_START = "/config/";
    private static final String RELATIVE_PATH_FLAG = "file:";
    private static final int DEFAULT_POOL_SIZE = 100;
    private static final String DEFAULT_ACCOUNT_MARK = "无初始化标志";
    private static final String FILE_LINE_SEPARATOR = "-";
    private static final String JAR_EVN = ".jar";
    private static final String BLANK = "";
    private static final String DEFAULT_FILE_NAME = "sponsorAccountPool.poolFile";
    private static final String DEFAULT_DEVELOP_EVN_POOL_FILE_PATH = RESOURCE_START + DEFAULT_FILE_NAME;
    private static final String DEFAULT_JAR_EVN_POOL_FILE_PATH = JAR_ENV_START + DEFAULT_FILE_NAME;

    private SponsorAccountConfig sponsorAccountConfig;

    public DefaultSponsorAccountFactory(){
        this.sponsorAccountConfig = new DefaultSponsorAccountConfig();
    }

    public DefaultSponsorAccountFactory(SponsorAccountConfig sponsorAccountConfig){
        this.sponsorAccountConfig = sponsorAccountConfig;
    }

    @Override
    public SponsorAccountPool initPool(BcOperationService operationService, String address, String publicKey, String privateKey, Integer size, String filePath, String sponsorAccountMark){

        int poolSize = size == null || size == 0 ? DEFAULT_POOL_SIZE : size;
        String finalPath = getFinalPath(filePath);
        String accountMark = StringUtils.isEmpty(sponsorAccountMark) ? DEFAULT_ACCOUNT_MARK : sponsorAccountMark;

        List<SponsorAccount> sponsorAccountList = parseFile(operationService, address, publicKey, privateKey, poolSize, finalPath, accountMark);

        return initSponsorAccountPool(sponsorAccountList);
    }

    private String getFinalPath(String filePath){
        if (!checkPathAbsolute(filePath)) {
            throw new RuntimeException("pool file path must be absolute path!");
        }

        String finalPath = filePath;

        if (getProjectPath().contains(JAR_EVN)) {
            if (StringUtils.isEmpty(filePath) || filePath.startsWith(RESOURCE_START)) {
                finalPath = getDefaultJarPath();
            }
        } else {
            if (StringUtils.isEmpty(filePath)) {
                filePath = DEFAULT_DEVELOP_EVN_POOL_FILE_PATH;
            }
            if (filePath.startsWith(RESOURCE_START)) {
                finalPath = getAbstractProjectRootPath() + "/src/main/resources/" + filePath.replace(RESOURCE_START, BLANK);
            }
        }

        LOGGER.debug("finalFilePath:" + finalPath);

        checkPathExist(finalPath);
        return finalPath;
    }

    private void checkPathExist(String finalPath){
        try {
            // todo 在windows下检查失败!
            String parentPath = finalPath.substring(0, finalPath.lastIndexOf(File.separator));
            File dir = new File(parentPath);
            if (!dir.exists()) {
                LOGGER.debug("finalPath not exists now create dir path : " + parentPath);
                dir.mkdirs();
            }
        } catch (Exception e) {
            // swallow
            LOGGER.debug("checkPathExist fail...");
        }
    }

    private boolean checkPathAbsolute(String filePath){
        return StringUtils.isEmpty(filePath) || filePath.startsWith(RESOURCE_START) || filePath.startsWith("/") || windowsAbsolutePath(filePath);
    }

    private boolean windowsAbsolutePath(String filePath){
        return filePath.startsWith("c:") || filePath.startsWith("C:")
                || filePath.startsWith("d:") || filePath.startsWith("D:")
                || filePath.startsWith("e:") || filePath.startsWith("E:")
                || filePath.startsWith("f:") || filePath.startsWith("F:");
    }

    private String getDefaultJarPath(){
        String projectRootPath;

        Pattern r = Pattern.compile("([\\s\\S]*)(/[\\s\\S]*.jar!)");
        Matcher m = r.matcher(getProjectPath());
        if (m.find()) {
            projectRootPath = m.group(1);
            projectRootPath = projectRootPath.replaceFirst(RELATIVE_PATH_FLAG, BLANK);
        } else {
            throw new RuntimeException("getDefaultJarPath path not found!");
        }

        checkDir(projectRootPath + JAR_ENV_START);
        return projectRootPath + DEFAULT_JAR_EVN_POOL_FILE_PATH;
    }

    private String getAbstractProjectRootPath(){
        String projectRootPath;

        Pattern r = Pattern.compile("([\\s\\S]*)/target/");
        Matcher m = r.matcher(getProjectPath());
        if (m.find()) {
            projectRootPath = m.group(1);
        } else {
            throw new RuntimeException("getAbstractProjectRootPath path not found!");
        }

        return projectRootPath;
    }

    private String getProjectPath(){
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        return url == null ? BLANK : url.getPath();
    }

    private SponsorAccountPool initSponsorAccountPool(List<SponsorAccount> sponsorAccountList){
        SponsorAccountPool sponsorAccountPool = new DefaultSponsorAccountPool();
        sponsorAccountPool.addSponsorAccount(sponsorAccountList.toArray(new SponsorAccount[sponsorAccountList.size()]));
        return sponsorAccountPool;
    }


    private List<SponsorAccount> parseFile(BcOperationService operationService, String address, String publicKey, String privateKey, Integer poolSize, String finalPath, String accountMark){
        File file = new File(finalPath);
        List<SponsorAccount> sponsorAccountList;
        if (file.exists()) {
            LOGGER.debug("file exists then parse file :" + finalPath);
            sponsorAccountList = readFile(finalPath);
            if (sponsorAccountList.size() < poolSize) {
                LOGGER.debug("file content less than pool size , then add SponsorAccount :" + finalPath);
                List<SponsorAccount> addSponsorAccountList = generateSponsorAccount(operationService, address, publicKey, privateKey, poolSize - sponsorAccountList.size(), accountMark);
                sponsorAccountList.addAll(addSponsorAccountList);
                writeFile(SwallowUtil.getFileOutputStream(finalPath), sponsorAccountList);
            }
        } else {
            sponsorAccountList = generateSponsorAccount(operationService, address, publicKey, privateKey, poolSize, accountMark);
            writeFile(SwallowUtil.getFileOutputStream(finalPath), sponsorAccountList);
        }
        return sponsorAccountList;
    }


    private List<SponsorAccount> readFile(String finalPath){
        List<SponsorAccount> sponsorAccountList = new ArrayList<>();
        FileInputStream is = SwallowUtil.getFileInputStream(finalPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String str;
            while ((str = reader.readLine()) != null) {
                try {
                    LOGGER.debug("parse line:" + str);
                    String[] line = str.split(FILE_LINE_SEPARATOR);
                    sponsorAccountList.add(new SponsorAccount(line[0], line[1], line[2]));
                } catch (Exception e) {
                    // swallow
                }
            }
        } catch (IOException e) {
            LOGGER.error("readFile found IOException:", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                LOGGER.error("readFile close FileInputStream found IOException:", e);
            }
        }
        return sponsorAccountList;
    }

    private List<SponsorAccount> generateSponsorAccount(BcOperationService operationService, String address, String publicKey, String privateKey, Integer poolSize, String accountMark){
        int totalCount = 0;
        LOGGER.debug("generateSponsorAccount size:" + poolSize);
        List<SponsorAccount> sponsorAccountList = new ArrayList<>();
        while (poolSize > totalCount) {

            try {
                Transaction transaction = operationService.newTransaction(address);
                if (poolSize - totalCount > DEFAULT_CREATE_LIMIT) {
                    limitCreateAccount(sponsorAccountList, transaction, DEFAULT_CREATE_LIMIT, accountMark);
                } else {
                    limitCreateAccount(sponsorAccountList, transaction, poolSize - totalCount, accountMark);
                }

                totalCount = sponsorAccountList.size();
                transaction.buildAddSigner(publicKey, privateKey);
                List<Signature> signatures = sponsorAccountConfig.provideSignature();
                if (signatures != null && !signatures.isEmpty()) {
                    for (Signature signature : signatures) {
                        transaction.buildAddSigner(signature.getPublicKey(), signature.getPrivateKey());
                    }
                }
                transaction.commit();
            } catch (SdkException e) {
                // 出现异常，清空列表，返回
                LOGGER.error("GenerateSponsorAccount Found Exception", e);
                sponsorAccountList.clear();
                break;
            }
        }
        return sponsorAccountList;
    }

    private void limitCreateAccount(List<SponsorAccount> sponsorAccountList, Transaction transaction, int limit, String accountMark) throws SdkException{
        for (int i = 0; i < limit; i++) {
            BlockchainKeyPair keyPair = SecureKeyGenerator.generateBubiKeyPair();
            sponsorAccountList.add(new SponsorAccount(keyPair.getBubiAddress(), keyPair.getPubKey(), keyPair.getPriKey()));
            transaction.buildAddOperation(sponsorAccountConfig.createAccountOperation(keyPair.getBubiAddress(), keyPair.getPubKey(), keyPair.getPriKey(), accountMark));
            List<BcOperation> operations = sponsorAccountConfig.provideBcOperations(keyPair.getBubiAddress());
            if (operations != null && !operations.isEmpty()) {
                for (BcOperation operation : operations) {
                    transaction.buildAddOperation(operation);
                }
            }
        }
    }


    private void writeFile(OutputStream os, List<SponsorAccount> sponsorAccountList){
        if (sponsorAccountList == null || sponsorAccountList.isEmpty()) {
            return;
        }
        LOGGER.debug("writeFile size:" + sponsorAccountList.size());
        String content = generateLine(sponsorAccountList);
        try {
            os.write(content.getBytes("UTF-8"));
        } catch (IOException e) {
            LOGGER.error("writeFile found IOException:", e);
        } finally {
            try {
                os.flush();
                os.close();
            } catch (IOException e) {
                LOGGER.error("writeFile close OutputStream found IOException:", e);
            }
        }
    }

    private String generateLine(List<SponsorAccount> sponsorAccountList){
        StringBuilder sb = new StringBuilder();
        sponsorAccountList.forEach(sponsorAccount ->
                sb.append(sponsorAccount.getAddress()).append(FILE_LINE_SEPARATOR)
                        .append(sponsorAccount.getPublicKey()).append(FILE_LINE_SEPARATOR)
                        .append(sponsorAccount.getPrivateKey()).append("\n")
        );
        return sb.toString().substring(0, sb.toString().length() - 1);
    }


    private void checkDir(String path){
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

}
