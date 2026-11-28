package com.bohakka.renthub.config;

import com.bohakka.renthub.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Chat push (D-57): STOMP over a plain WebSocket at {@code /ws}. The handshake is an ordinary request behind Spring
 * Security, so only a logged-in session can open it, and only from the SPA's own origin ({@code app.base-url}).
 * Messages are sent through REST; over the socket a client may only subscribe to its own {@code /user/queue/**}.
 * The in-memory simple broker fits the single-instance deployment.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final AppProperties app;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").setAllowedOrigins(app.baseUrl());
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/queue");
		registry.setUserDestinationPrefix("/user");
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				if (accessor == null || accessor.getCommand() == null) {
					return message;
				}
				StompCommand command = accessor.getCommand();
				if (command == StompCommand.CONNECT && accessor.getUser() == null) {
					throw new MessageDeliveryException("Not authenticated");
				}
				if (command == StompCommand.SEND) {
					throw new MessageDeliveryException("Sending over the socket is not supported");
				}
				if (command == StompCommand.SUBSCRIBE) {
					String destination = accessor.getDestination();
					if (destination == null || !destination.startsWith("/user/queue/")) {
						throw new MessageDeliveryException("Subscription not allowed");
					}
				}
				return message;
			}
		});
	}
}
