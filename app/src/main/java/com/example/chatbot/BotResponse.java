package com.example.chatbot;

public class BotResponse {
    private String recipent;
    private String text;

    public BotResponse() {
    }

    public BotResponse(String recipent, String text) {
        this.recipent = recipent;
        this.text = text;
    }

    public String getRecipent() {
        return recipent;
    }

    public void setRecipent(String recipent) {
        this.recipent = recipent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
