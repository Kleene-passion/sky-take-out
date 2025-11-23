package com.sky.controller.notify;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.WeChatProperties;
import com.sky.service.OrderService;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * 支付回调相关接口
 */
/**
 * 支付回调相关接口（模拟版）
 */
@RestController
@RequestMapping("/notify")
@Slf4j
public class PayNotifyController {

    @Autowired
    private OrderService orderService;

    /**
     * 模拟支付成功回调
     * 前端/Postman 直接传 JSON：
     * {
     *   "out_trade_no": "20251122001",
     *   "transaction_id": "MOCK_123456"
     * }
     */
    @PostMapping("/paySuccess")
    public void paySuccessNotify(@RequestBody String body,
                                 HttpServletResponse response) throws Exception {

        log.info("模拟支付成功回调：{}", body);

        // 解析 JSON（模拟版，不做任何解密）
        JSONObject jsonObject = JSON.parseObject(body);

        String outTradeNo = jsonObject.getString("out_trade_no");   // 商户平台订单号
        String transactionId = jsonObject.getString("transaction_id"); // 模拟微信订单号

        log.info("商户平台订单号：{}", outTradeNo);
        log.info("模拟微信交易号：{}", transactionId);

        // 核心业务处理：修改订单状态
        orderService.paySuccess(outTradeNo);

        // 返回给“微信”（其实是前端）
        responseToWeixin(response);
    }


    /**
     * 响应微信（模拟）
     */
    private void responseToWeixin(HttpServletResponse response) throws Exception {
        response.setStatus(200);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("code", "SUCCESS");
        map.put("message", "SUCCESS");
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        response.getOutputStream().write(JSON.toJSONString(map).getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }
}

