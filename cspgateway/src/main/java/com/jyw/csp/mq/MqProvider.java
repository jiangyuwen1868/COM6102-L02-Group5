//package com.jyw.csp.mq;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.annotation.PostConstruct;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import com.alibaba.fastjson.JSON;
//import com.jyw.csp.mq.msg.AlarmMsgData;
//import com.jyw.csp.mq.msg.MqMessageType;
//import com.jyw.csp.mq.msg.TxMsgData;
//import com.jyw.csp.util.Base64;
//import com.jyw.csp.util.JsonUtils;
//import com.jyw.csp.util.Utils;
//
//@Component
//public class MqProvider {
//	
//	private final static Logger logger = LoggerFactory.getLogger(MqProvider.class);
//	
//	private static MqProvider provider;
//	
//	@Autowired
//    private RabbitTemplate rabbitTemplate;  //使用RabbitTemplate,这提供了接收/发送等等方法
//	
//	@PostConstruct
//	public void onInit() {
//		provider = this;
//		provider.rabbitTemplate = this.rabbitTemplate;
//	}
//
//	/**
//	 * 发送交易通知 消息
//	 * @param txMsgData
//	 * @return
//	 */
//	public static boolean sendTxMessage(TxMsgData txMsgData) {
//		try {
//	        String msgId = Utils.GUID();
//	        String msgData = "";
//	        if(msgData!=null) {
//	        	String messageData = JSON.toJSONString(txMsgData);
//	        	msgData = Base64.encodeString(messageData.getBytes());
//	        }
//	        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//	        Map<String, Object> msgMap = new HashMap<>();
//	        msgMap.put("msgId", msgId);
//	        msgMap.put("msgType", MqMessageType.TYPE01.getType());
//	        msgMap.put("msgData", msgData);
//	        msgMap.put("createTime", createTime);
//	        String sendMsg = JSON.toJSONString(msgMap);
//	        logger.debug("--------------sendTxMessage>>\n" + JsonUtils.formatJson(sendMsg));
//	        provider.rabbitTemplate.convertAndSend(TopicRabbitConfig.topicExchange, 
//	        		TopicRabbitConfig.cspGatewayTopic, 
//	        		sendMsg);
//		} catch(Exception e) {
//			logger.error("sendTxMessage", e);
//			return false;
//		}
//		return true;
//    }
//	
//	/**
//	 * 发送告警通知 消息
//	 * @param alarmMsgData
//	 * @return
//	 */
//	public static boolean sendAlarmMessage(AlarmMsgData alarmMsgData) {
//		try {
//			String msgId = Utils.GUID();
//	        String msgData = "";
//	        if(msgData!=null) {
//	        	String messageData = JSON.toJSONString(alarmMsgData);
//	        	msgData = Base64.encodeString(messageData.getBytes());
//	        }
//	        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//	        Map<String, Object> msgMap = new HashMap<>();
//	        msgMap.put("msgId", msgId);
//	        msgMap.put("msgType", MqMessageType.TYPE02.getType());
//	        msgMap.put("msgData", msgData);
//	        msgMap.put("createTime", createTime);
//	        String sendMsg = JSON.toJSONString(msgMap);
//	        logger.debug("--------------sendAlarmMessage>>\n" + JsonUtils.formatJson(sendMsg));
//	        provider.rabbitTemplate.convertAndSend(TopicRabbitConfig.topicExchange, 
//	        		TopicRabbitConfig.cspGatewayTopic, 
//	        		sendMsg);
//		} catch(Exception e) {
//			logger.error("sendAlarmMessage", e);
//			return false;
//		}
//		return true;
//	}
//}


