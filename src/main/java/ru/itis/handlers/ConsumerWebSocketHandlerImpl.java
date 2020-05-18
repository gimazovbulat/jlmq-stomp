package ru.itis.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import itis.ConsumerCallback;
import itis.ConsumerWebSocketHandler;
import itis.MessageDto;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import ru.itis.services.interfaces.RequestDispatcher;

@Data
@Component
public class ConsumerWebSocketHandlerImpl extends StompSessionHandlerAdapter implements ConsumerWebSocketHandler<MessageDto> {
    private ConsumerCallback<MessageDto> callback;
    private StompSession session;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RequestDispatcher requestDispatcher;

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("handling new message " + headers + " " + payload);
        MessageDto message = null;
        try {
            message = objectMapper.readValue(payload.toString(), MessageDto.class);
            MessageDto mesToSend = requestDispatcher.dispatch(callback, message);
            if (mesToSend != null) {
                String string = objectMapper.writeValueAsString(mesToSend);
                session.send("/queue/update/" + mesToSend.getHeaders().get("queueName"), new TextMessage(string));
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("connected. session: " + session + ". headers: " + connectedHeaders);
        this.session = session;
    }
}