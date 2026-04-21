package com.mxbc.seckill.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/subscribe")
    public void subscribe(String message) {
        logger.info("用户订阅: {}", message);
    }

    public void sendStockUpdate(Long productId, int stock) {
        messagingTemplate.convertAndSend("/topic/stock/" + productId, stock);
        logger.info("发送库存更新通知: 商品 {}, 库存 {}", productId, stock);
    }

    public void sendSeckillResult(Long userId, boolean success, String message) {
        messagingTemplate.convertAndSend("/queue/result/" + userId, 
            "{\"success\": " + success + ", \"message\": \"" + message + "\"}");
        logger.info("发送秒杀结果通知: 用户 {}, 成功 {}, 消息 {}", userId, success, message);
    }

    public void sendSystemMessage(String message) {
        messagingTemplate.convertAndSend("/topic/system", message);
        logger.info("发送系统通知: {}", message);
    }
}
