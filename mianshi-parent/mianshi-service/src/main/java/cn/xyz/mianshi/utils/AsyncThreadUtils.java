package cn.xyz.mianshi.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
/** @version:（1.0） 
* @ClassName	AsyncThreadUtils
* @Description: （利用Future异步获取多线程任务结果） 
* @date:2018年11月23日下午3:36:00  
*/ 
public class AsyncThreadUtils {
	  public static void main(String[] args) {
		  	AsyncThreadUtils t = new AsyncThreadUtils();
	        List<Future<String>> futureList = new ArrayList<Future<String>>();
	        t.generate(4, futureList);
//	        t.doOtherThings();
	        t.getResult(futureList);
	        System.out.println(111111);
	    }

	    /**
	     * 生成指定数量的线程，都放入future数组
	     * 
	     * @param threadNum
	     * @param fList
	     */
	    public void generate(int threadNum, List<Future<String>> fList) {
	        ExecutorService service = Executors.newFixedThreadPool(threadNum);
	        for (int i = 0; i < threadNum; i++) {
	            Future<String> f = service.submit(getJob(i));
	            fList.add(f);
	        }
	        service.shutdown();
	    }

	    /**
	     * other things
	     */
	    /*public void doOtherThings() {
	        try {
	            for (int i = 0; i < 3; i++) {
	                System.out.println("do thing no:" + i);
	                Thread.sleep(1000 * (new Random().nextInt(10)));
	            }
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }*/

	    /**
	     * 从future中获取线程结果，打印结果
	     * 
	     * @param fList
	     */
	    public void getResult(List<Future<String>> fList) {
	        ExecutorService service = Executors.newSingleThreadExecutor();
	        service.execute(getCollectJob(fList));
	        service.shutdown();
	    }

	    /**
	     * 生成指定序号的线程对象
	     * 
	     * @param i
	     * @return
	     */
	    public Callable<String> getJob(final int i) {
	        final int time = new Random().nextInt(10);
	        return new Callable<String>() {

				public String call() throws Exception {
					 Thread.sleep(1000 * time);
		             return "thread-" + i;
				}
	        };
	    }

	    
	    
	    
	    
	    /**
	     * 生成结果收集线程对象
	     * 
	     * @param fList
	     * @return
	     */
	    public Runnable getCollectJob(final List<Future<String>> fList) {
	        return new Runnable() {
	            public void run() {
	                for (Future<String> future : fList) {
	                    try {
	                        while (true) {
	                            if (future.isDone() && !future.isCancelled()) {
	                                System.out.println("Future:" + future
	                                        + ",Result:" + future.get());
	                                break;
	                            } else {
	                                Thread.sleep(1000);
	                            }
	                        }
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	        };
	    }
}
