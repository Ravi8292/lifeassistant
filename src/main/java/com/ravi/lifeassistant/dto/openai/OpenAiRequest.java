package com.ravi.lifeassistant.dto.openai;

import java.util.List;

public class OpenAiRequest {

    private String model;
    private List<OpenAiMessage> messages;
    private double temperature;

    public OpenAiRequest(String model, List<OpenAiMessage> messages) {
        this.model = model;
        this.messages = messages;
        this.temperature = 0.2;
    }

    public String getModel() {
        return model;
    }

    public List<OpenAiMessage> getMessages() {
        return messages;
    }

    public double getTemperature() {
        return temperature;
    }
}
