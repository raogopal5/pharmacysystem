package org.nh.pharmacy.aop.producer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.nh.pharmacy.config.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nitesh on 7/6/17.
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StockServiceAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier(Channels.ITEM_STORE_STOCK_OUTPUT)
    private MessageChannel itemStoreStockChannel;

    @Autowired
    @Qualifier(Channels.STOCK_MOVE_OUTPUT)
    private MessageChannel stockMoveChannel;

    @Autowired
    @Qualifier(Channels.STOCK_OUTPUT)
    private MessageChannel stockOutChannel;

    @Autowired
    @Qualifier(Channels.STOCK_SOURCE_OUTPUT)
    private MessageChannel stockSourceChannel;

    @Autowired
    @Qualifier(Channels.MOVE_TO_TRANSIT_STOCK_OUTPUT)
    private MessageChannel stockTransitChannel;
    @Autowired
    @Qualifier(Channels.ITEM_STORE_STOCK_VIEW_OUTPUT)
    private MessageChannel itemStoreStockViewChannel;

    @Autowired
    @Qualifier(Channels.STORE_AUTO_CONSUMPTION_OUTPUT)
    private MessageChannel storeAutoConsumptionOutputChannel;



    public static ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            // To initialize the ThreadLocal object with initial value
            return new LinkedHashMap<String, Object>();
        }
    };

    /**
     * Pointcut that matches all services.
     */
    @Pointcut("@annotation(org.nh.pharmacy.annotation.PublishStockTransaction)")
    public void publishStockTransaction() {
    }


    /**
     * Pointcut that matches all services.
     */
    @Around("publishStockTransaction()")
    public Object aroundProducer(ProceedingJoinPoint joinPoint) throws Throwable {
        StockServiceAspect.threadLocal.get();
        try {
            Object data = joinPoint.proceed();
            if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                afterReturning();
            }
            return data;
        } finally {
            if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                StockServiceAspect.threadLocal.remove();
            }
        }
    }

        public void afterReturning() {
            Map<String, Object> map = StockServiceAspect.threadLocal.get();
            if (map.containsKey(Channels.ITEM_STORE_STOCK_OUTPUT)) {
                itemStoreStockChannel.send(MessageBuilder.withPayload(map.get(Channels.ITEM_STORE_STOCK_OUTPUT)).build());
            }
            if (map.containsKey(Channels.STOCK_OUTPUT)) {
                stockOutChannel.send(MessageBuilder.withPayload(map.get(Channels.STOCK_OUTPUT)).build());
            }
            if (map.containsKey(Channels.STOCK_SOURCE_OUTPUT)) {
                stockSourceChannel.send(MessageBuilder.withPayload(map.get(Channels.STOCK_SOURCE_OUTPUT)).build());
            }
            if (map.containsKey(Channels.ITEM_STORE_STOCK_VIEW_OUTPUT)) {
                itemStoreStockViewChannel.send(MessageBuilder.withPayload(map.get(Channels.ITEM_STORE_STOCK_VIEW_OUTPUT)).build());
            }

            if (map.containsKey(Channels.STORE_AUTO_CONSUMPTION_OUTPUT)) {
                storeAutoConsumptionOutputChannel.send(MessageBuilder.withPayload(map.get(Channels.STORE_AUTO_CONSUMPTION_OUTPUT)).build());
            }

            if (map.containsKey(Channels.MOVE_TO_TRANSIT_STOCK_OUTPUT)) {
                stockTransitChannel.send(MessageBuilder.withPayload(map.get(Channels.MOVE_TO_TRANSIT_STOCK_OUTPUT)).build());
            }
        }
}
