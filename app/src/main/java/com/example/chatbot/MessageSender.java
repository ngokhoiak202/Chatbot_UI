package com.example.chatbot;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MessageSender {
    @POST("webhooks/rest/webhook")
    Call<ArrayList<BotResponse>> messageSender(@Body Message userMessage);
}
