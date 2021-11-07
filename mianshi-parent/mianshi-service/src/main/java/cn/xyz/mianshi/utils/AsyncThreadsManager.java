package cn.xyz.mianshi.utils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import cn.xyz.mianshi.model.PressureParam;
import cn.xyz.mianshi.model.PressureTestVO;

public interface AsyncThreadsManager {
	 /**
     * 生成指定数量的线程，都放入future数组
     * 
     * @param threadNum
     * @param fList
     */
    public void generate(int threadNum, List<Future<String>> fList);
    
    /**
     * other things
     */
    public List<PressureTestVO> doOtherThings(PressureParam param,List<XMPPTCPConnection> connList);
    
    /**
     * 从future中获取线程结果，打印结果
     * 
     * @param fList
     */
    public void getResult(List<Future<String>> fList);
    
    /**
     * 生成指定序号的线程对象
     * 
     * @param i
     * @return
     */
    public Callable<String> getJob(final int i);
    
    /**
     * 生成结果收集线程对象
     * 
     * @param fList
     * @return
     */
    public Runnable getCollectJob(final List<Future<String>> fList);
    
    
}
