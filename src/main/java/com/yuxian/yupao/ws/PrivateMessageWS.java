package com.yuxian.yupao.ws;

import com.yuxian.yupao.config.MessageEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
@ServerEndpoint(value = "/privateMessage/{userId}", encoders = MessageEncoder.class)  // 接口路径 ws://localhost:port/webSocket/userId;
public class PrivateMessageWS {


	//与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;
	private Long userId;
	//concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
	//虽然@Component默认是单例模式的，但springboot还是会为每个websocket连接初始化一个bean，所以可以用一个静态set保存起来。
	//注：底下WebSocket是当前类名
	private final static CopyOnWriteArraySet<PrivateMessageWS> privateMessageWS = new CopyOnWriteArraySet<>();
	// 用来存在线连接用户信息
	private final static ConcurrentHashMap<Long, Session> sessionPool = new ConcurrentHashMap<>();

	/**
	 * 链接成功调用的方法
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("userId") Long userId) {
		try {
			//判断该用户是否已建立连接
			if (!sessionPool.containsKey(userId)) {
				this.session = session;
				this.userId = userId;
				privateMessageWS.add(this);
				sessionPool.put(userId, session);
				log.info("【websocket消息】有新的连接，总数为:" + privateMessageWS.size());
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 链接关闭调用的方法
	 */
	@OnClose
	public void onClose() {
		try {
			privateMessageWS.remove(this);
			sessionPool.remove(this.userId);
			log.info("【websocket消息】连接断开，总数为:" + privateMessageWS.size());
		} catch (Exception e) {
		}
	}

	/**
	 * 收到客户端消息后调用的方法
	 *
	 * @param message
	 */
	@OnMessage
	public void onMessage(String message) {
		log.info("【成功接收客户消息：{}】", message);
	}

	/**
	 * 发送错误时的处理
	 *
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {

		log.error("用户错误,原因:" + error.getMessage());
		error.printStackTrace();
	}


	// 此为广播消息
	public static void sendAllMessage(String message) {
		log.info("【websocket消息】广播消息:" + message);
		for (PrivateMessageWS privateMessageWS : PrivateMessageWS.privateMessageWS) {
			try {
				if (privateMessageWS.session.isOpen()) {
					privateMessageWS.session.getBasicRemote().sendText(message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 此为单点消息
	public static void sendOneMessage(Long userId, Object message) {
		Session session = sessionPool.get(userId);
		if (session != null && session.isOpen()) {
			try {
				log.info("【websocket消息】 单点消息:" + message);
				session.getBasicRemote().sendObject(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 此为单点消息(多人)
	public static void sendMoreMessage(String[] userIds, String message) {
		for (String userId : userIds) {
			Session session = sessionPool.get(userId);
			if (session != null && session.isOpen()) {
				try {
					log.info("【websocket消息】 单点消息:" + message);
					session.getBasicRemote().sendText(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

}