package ru.itis.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import itis.ConsumerCallback;
import itis.MessageDto;
import org.springframework.stereotype.Component;
import ru.itis.services.interfaces.RequestDispatcher;

import java.util.HashMap;
import java.util.Map;


@Component
public class RequestDispatcherImpl implements RequestDispatcher {

    @Override
    public MessageDto dispatch(ConsumerCallback<MessageDto> callback, MessageDto message) throws JsonProcessingException {
        Map<String, String> headers = message.getHeaders();
        String command = headers.get("command");
        System.out.println(message);
        MessageDto res = null;
        switch (command) {
            case "receive": {
                MessageDto messageDto = MessageDto
                        .builder()
                        .status(MessageDto.Status.ACCEPTED)
                        .messageId(headers.get("messageId"))
                        .body(message.getBody())
                        .build();

                callback.onReceive(messageDto);

                Map<String, String> resHeaders = new HashMap<>();
                resHeaders.put("messageId", headers.get("messageId"));
                resHeaders.put("queueName", headers.get("queueName"));

                res = MessageDto
                        .builder()
                        .headers(resHeaders)
                        .body(messageDto.getStatus().getTitle())
                        .build();

                break;
            }
            case "error": {
                System.out.println(message);
                System.out.println(message.getBody());
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
        return res;
    }
}
