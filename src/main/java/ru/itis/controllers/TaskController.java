package ru.itis.controllers;

import itis.ConsumerWebSocketHandler;
import itis.JlmqConnector;
import itis.JlmqProducer;
import itis.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.protoocol.UglyDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static itis.MessageDto.Status.NEW;

@RestController
public class TaskController {
    @Autowired
    ConsumerWebSocketHandler<MessageDto> webSocketHandler;

    @PostMapping("/do_things")
    public ResponseEntity connect(@RequestBody UglyDto uglyDto) {
        try {
            JlmqConnector jlmqConnector = JlmqConnector
                    .connector(webSocketHandler)
                    .port(uglyDto.getPort())
                    .connect();

            String queueName = uglyDto.getQueueName();

            jlmqConnector.consumer()
                    .subscribe(queueName)
                    .onReceive(message -> {
                        message.accepted();
                        System.out.println(message.getBody());
                        message.completed();
                    })
                    .create();


            JlmqProducer jlmqProducer = jlmqConnector.producer()
                    .toQueue(uglyDto.getQueueName())
                    .create();

            MessageDto messageDto = MessageDto
                    .builder()
                    .status(NEW)
                    .body(uglyDto.getText())
                    .headers(new HashMap<>())
                    .build();

            jlmqProducer.send(messageDto);
        } catch (ExecutionException | InterruptedException | IOException e) {
            throw new IllegalStateException(e);
        }
        return ResponseEntity.ok().build();
    }

}
