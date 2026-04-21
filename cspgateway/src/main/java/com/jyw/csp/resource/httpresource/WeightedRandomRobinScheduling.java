package com.jyw.csp.resource.httpresource;

import java.util.Vector;

import com.jyw.csp.util.log.LogUtil;

/**
 * 权重随机调度算法(WeightedRandom-RobinScheduling)-Java实现
 * @author deyang
 *
 */
public class WeightedRandomRobinScheduling {

	 private int serverCount = 0; //服务器数量
	 private Vector<HttpCommService> serverList; //服务器集合
	 
	 public WeightedRandomRobinScheduling(Vector<HttpCommService> serverList) {
		 this.serverList = serverList;
		 this.serverCount = serverList.size();
	 }
	 
	 /**
     * 设置服务器列表
     * @param serverList
     */
	 public void setServerList(Vector<HttpCommService> serverList) {
    	this.serverList = serverList;
    	this.serverCount = serverList.size();
	 }
    
	 /**
	  * 获取所有服务器的权重值相加的总权重值
	  * @param serverList
	  * @return
	  */
	 private int getSumWeightForServers() {
		int sw = 0;
        for (int i = 0; i < serverCount; i++) {
            sw = sw + serverList.get(i).getWeight();
        }
        return sw;
	 }
	 
	 /**
	  * 获取权重随机数
	  * @param seed
	  * @return
	  */
	 private int getRandom(int seed) {
		 return (int)Math.round(Math.random() * seed);
	 }
	 
	 /**
	  * 算法流程：
	  * 将所有服务器的权重值相加 sumWeight
	  * 以相加结果为随机数的种子，生成1~sumWeight之间的随机数random
	  * 遍历服务器列表（访问顺序可以随意），将当前节点的权重值加上前面访问的各节点权重值得curWeight,
	  * 判断curWeight >= random,如果条件成立则返回当前节点服务器,如果不是则继续累加下一节点。
	  * 直到符合上面的条件,由于random <= sum 因此一定存在curWeight >= random
	  * @return
	  */
	 public HttpCommService getResouce(int max_req)  throws Exception {
		 int sumWeight = getSumWeightForServers();
		 int random = getRandom(sumWeight);
		 
		 int curWeight = 0;
		 for(int i=0;i<serverCount;i++) {
			 HttpCommService srv = serverList.get(i);
			 curWeight += srv.getWeight();
			 if(curWeight >= random) {
            	if (srv.isRight) {
            		int curConn = srv.getCurConn();
    				if (curConn < max_req) {
    					srv.getCurConnAndIncrement();
    					LogUtil.debug("Service Url:" + i + "==="+srv.httpURL);
    					return srv;
    				}
    			}
			 }
		 }
		 
		 throw new HttpResouceException("Http comm service in group all not available!,please check network!");
	 }
}
