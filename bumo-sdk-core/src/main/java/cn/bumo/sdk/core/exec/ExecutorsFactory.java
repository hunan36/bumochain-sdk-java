package cn.bumo.sdk.core.exec;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import cn.bumo.access.utils.PropertiesUtils;
import cn.bumo.access.utils.concurrent.NamedThreadFactory;


/***
 * 
 * @author 布萌
 *
 */
public final class ExecutorsFactory {
//	private static final String threadPoolConfClassPath = "classpath:thread-pool.properties";
//	private static final String defaultCharset = "UTF-8";
	
	final static PoolAttrs poolAttrs = new PoolAttrs();
	
//	static {
//		try {
//			PropertiesUtils.load(poolAttrs, threadPoolConfClassPath, defaultCharset);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	final static ExecutorService executorService = new ThreadPoolExecutor(
			poolAttrs.getCorePoolSize(), 
			poolAttrs.getMaximumPoolSize(), 
			poolAttrs.getKeepAliveTime(), TimeUnit.SECONDS,
	        new LinkedTransferQueue<>(), //   SynchronousQueue
	        new NamedThreadFactory(poolAttrs.getThreadFactoryName(), poolAttrs.isIndexThread(), poolAttrs.isDeamon()));
	 
	final static ScheduledThreadPoolExecutor scheduCheck = new ScheduledThreadPoolExecutor(poolAttrs.getSchdCorePoolSize());
	
	public static  final ExecutorService getExecutorService() {
		return	executorService;
	}
	
	public static final ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
		return scheduCheck;
	}
}
