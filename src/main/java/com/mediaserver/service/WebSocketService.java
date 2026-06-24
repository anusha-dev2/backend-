package com.mediaserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyPaymentStatus(String userId, String status, Object data) {
        messagingTemplate.convertAndSend("/topic/payment/" + userId, new PaymentNotification(status, data));
    }

    public void notifySubscriptionUpdate(String userId, String action, Object data) {
        messagingTemplate.convertAndSend("/topic/subscription/" + userId, new SubscriptionNotification(action, data));
    }

    public void broadcastPlanUpdate(String action, Object data) {
        messagingTemplate.convertAndSend("/topic/plans", new PlanNotification(action, data));
    }

    public void sendMessageToDevice(String deviceId, Object payload) {
        messagingTemplate.convertAndSend("/topic/device/" + deviceId, payload);
    }

    // Notification classes
    public static class PaymentNotification {
        private String status;
        private Object data;

        public PaymentNotification(String status, Object data) {
            this.status = status;
            this.data = data;
        }

        public String getStatus() { return status; }
        public Object getData() { return data; }
    }

    public static class SubscriptionNotification {
        private String action;
        private Object data;

        public SubscriptionNotification(String action, Object data) {
            this.action = action;
            this.data = data;
        }

        public String getAction() { return action; }
        public Object getData() { return data; }
    }

    public static class PlanNotification {
        private String action;
        private Object data;

        public PlanNotification(String action, Object data) {
            this.action = action;
            this.data = data;
        }

        public String getAction() { return action; }
        public Object getData() { return data; }
    }
}
