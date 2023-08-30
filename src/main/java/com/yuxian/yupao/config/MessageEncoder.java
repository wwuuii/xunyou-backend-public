package com.yuxian.yupao.config;

import com.google.gson.Gson;
import com.yuxian.yupao.model.dto.privateMessage.Message;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class MessageEncoder implements Encoder.Text<Message> {
    @Override
    public String encode(Message message) throws EncodeException {
        // 将 Message 对象转换为 JSON 格式的字符串
        // 注意：此处假设你使用 JSON 格式进行消息传输，你可以根据实际情况选择适合的编码方式
        Gson gson = new Gson();
        return gson.toJson(message);
    }
    @Override
    public void init(EndpointConfig endpointConfig) {
        // 可选的初始化方法
    }

    @Override
    public void destroy() {
        // 可选的销毁方法
    }
}