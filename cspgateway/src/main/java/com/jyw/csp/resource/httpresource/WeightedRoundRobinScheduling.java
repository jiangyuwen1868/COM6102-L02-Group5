package com.jyw.csp.resource.httpresource;

import java.math.BigInteger;
import java.util.Vector;

import com.jyw.csp.util.log.LogUtil;

/**
 * 权重轮询调度算法(WeightedRound-RobinScheduling)-Java实现
 * @author deyang
 *
 */
public class WeightedRoundRobinScheduling {

	private static int currentIndex = -1;// 上一次选择的服务器
    private static int currentWeight = 0;// 当前调度的权值
    private int maxWeight = 0; // 最大权重
    private int gcdWeight = 0; //所有服务器权重的最大公约数
    private int serverCount = 0; //服务器数量
    private Vector<HttpCommService> serverList; //服务器集合
    
    public WeightedRoundRobinScheduling(Vector<HttpCommService> serverList) {
    	this.serverList = serverList;
    	this.serverCount = serverList.size();
    	this.maxWeight = getMaxWeightForServers(serverList);
    	this.gcdWeight = getGCDForServers(serverList);
    	if(currentIndex>=this.serverCount) {
    		currentIndex = -1;
    	}
    }
    
    /**
     * 设置服务器列表
     * @param serverList
     */
    public void setServerList(Vector<HttpCommService> serverList) {
    	this.serverList = serverList;
    	this.serverCount = serverList.size();
    	this.maxWeight = getMaxWeightForServers(serverList);
    	this.gcdWeight = getGCDForServers(serverList);
    	if(currentIndex>=this.serverCount) {
    		currentIndex = -1;
    	}
    }

    /**
     * 返回最大公约数
     * @param a
     * @param b
     * @return
     */
    private static int gcd(int a, int b) {
        BigInteger b1 = new BigInteger(String.valueOf(a));
        BigInteger b2 = new BigInteger(String.valueOf(b));
        BigInteger gcd = b1.gcd(b2);
        return gcd.intValue();
    }
    
    /**
     * 返回所有服务器权重的最大公约数
     * @param serverList
     * @return
     */
    private int getGCDForServers(Vector<HttpCommService> serverList) {
        int w = 0;
        for (int i = 0, len = serverList.size(); i < len - 1; i++) {
            if (w == 0) {
                w = gcd(serverList.get(i).getWeight(), serverList.get(i + 1).getWeight());
            } else {
                w = gcd(w, serverList.get(i + 1).getWeight());
            }
        }
        return w;
    }
    
    /**
     * 返回所有服务器中的最大权重
     * @param serverList
     * @return
     */
    public int getMaxWeightForServers(Vector<HttpCommService> serverList) {
        int w = 0;
        for (int i = 0, len = serverList.size(); i < len - 1; i++) {
            if (w == 0) {
                w = Math.max(serverList.get(i).getWeight(), serverList.get(i + 1).getWeight());
            } else {
                w = Math.max(w, serverList.get(i + 1).getWeight());
            }
        }
        return w;
    }
    
    /**
     *  算法流程： 
     *  假设有一组服务器 S = {S0, S1, …, Sn-1}
     *  有相应的权重，变量currentIndex表示上次选择的服务器
     *  权值currentWeight初始化为0，currentIndex初始化为-1 ，当第一次的时候返回 权值取最大的那个服务器，
     *  通过权重的不断递减 寻找 适合的服务器返回，直到轮询结束，权值返回为0 
     */
    public HttpCommService getResouce(int max_req) throws Exception {
    	int cnt = 0;
    	int rLen = serverList.size();
        while (true) {
        	cnt++;
            currentIndex = (currentIndex + 1) % serverCount;
            if (currentIndex == 0) {
                currentWeight = currentWeight - gcdWeight;
                if (currentWeight <= 0) {
                    currentWeight = maxWeight;
                    if (currentWeight == 0)
                        return null;
                }
            }
            if (serverList.get(currentIndex).getWeight() >= currentWeight) {
            	HttpCommService srv = serverList.get(currentIndex);
            	if (srv.isRight) {
            		int curConn = srv.getCurConn();
    				if (curConn < max_req) {
    					srv.getCurConnAndIncrement();
    					LogUtil.debug("Service Url:" + currentIndex + "==="+srv.httpURL);
    					return srv;
    				}
    			}
            }
            
            if (rLen == cnt) {//轮询了所有资源
				break;
			}
        }
        
        throw new HttpResouceException("Http comm service in group all not available!,please check network!");
    }
}
