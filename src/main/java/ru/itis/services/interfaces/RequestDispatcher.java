package ru.itis.services.interfaces;


import com.fasterxml.jackson.core.JsonProcessingException;
import itis.ConsumerCallback;
import itis.MessageDto;

public interface RequestDispatcher {
    MessageDto dispatch(ConsumerCallback<MessageDto> callback, MessageDto message) throws JsonProcessingException;
}
