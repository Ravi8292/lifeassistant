/*package com.ravi.lifeassistant.service;

import com.ravi.lifeassistant.dto.AiTaskResponse;
import com.ravi.lifeassistant.dto.openai.OpenAiMessage;
import com.ravi.lifeassistant.dto.openai.OpenAiRequest;
import com.ravi.lifeassistant.dto.openai.OpenAiResponse;
import com.ravi.lifeassistant.enums.TaskStatus;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AiService {

    private final WebClient webClient;

    @Value("${openai.model}")
    private String model;

    public AiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public AiTaskResponse parseTextToTask(String userText) {

        String prompt = """
        Convert the following text into JSON with fields:
        title, description, dueDate (yyyy-MM-dd), status.
        Use status as PENDING or DONE only.

        Text: %s
        """.formatted(userText);

        OpenAiRequest request = new OpenAiRequest(
                model,
                List.of(new OpenAiMessage("user", prompt))
        );

        OpenAiResponse response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiResponse.class)
                .block();

        String aiJson = response.getChoices().get(0).getMessage().getContent();

        AiTaskResponse result = new AiTaskResponse();

        result.setTitle(extract(aiJson, "title"));
        result.setDescription(extract(aiJson, "description"));

        // ✅ dueDate parsing
        String dueDateStr = extract(aiJson, "dueDate");
        LocalDate dueDate = (dueDateStr == null || dueDateStr.isEmpty())
                ? LocalDate.now().plusDays(1)
                : LocalDate.parse(dueDateStr);

        result.setDueDate(dueDate);

        // ✅ status parsing
        String statusStr = extract(aiJson, "status");
        TaskStatus status = (statusStr == null)
                ? TaskStatus.PENDING
                : TaskStatus.valueOf(statusStr);

        result.setStatus(status);

        return result;
    }

    private String extract(String json, String field) {
        String pattern = "\"" + field + "\"\\s*:\\s*\"(.*?)\"";
        var matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }
}*/
package com.ravi.lifeassistant.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.lifeassistant.dto.AiTaskResponse;
import com.ravi.lifeassistant.dto.openai.OpenAiMessage;
import com.ravi.lifeassistant.dto.openai.OpenAiRequest;
import com.ravi.lifeassistant.dto.openai.OpenAiResponse;
import com.ravi.lifeassistant.enums.TaskStatus;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


/*
public class AiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiService() {
        this.webClient = WebClient.create("http://localhost:11434");
    }

    public AiTaskResponse parseTextToTask(String userText) {

        String prompt = """
        Extract task details and return ONLY JSON in this format:
        {
          "title": "...",
          "description": "...",
          "dueDate": "YYYY-MM-DD",
          "status": "PENDING"
        }

        Text:
        %s
        """.formatted(userText);

        String response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(Map.of(
                        "model", "llama2",
                        "prompt", prompt,
                        "stream", false
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            // Ollama response contains extra fields → extract "response"
            String jsonText = objectMapper
                    .readTree(response)
                    .get("response")
                    .asText();

            return objectMapper.readValue(jsonText, AiTaskResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
}*/
@Service
public class AiService {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;
    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public AiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AiTaskResponse parseTextToTask(String userText) {
        // 1. ANCHOR THE DATE: Tell the AI today's date so it calculates "tomorrow" correctly
        // DEBUG - remove after fixing
        System.out.println(">>> API URL: " + apiUrl);
        System.out.println(">>> API KEY: " + (apiKey != null ? apiKey.substring(0, 10) + "..." : "NULL"));
        System.out.println(">>> MODEL: " + model);
        
        // ... rest of your code

        String today = java.time.LocalDate.now().toString(); 
        String systemPrompt = """
        	    Today is %s. 
        	    Convert the user's text into a JSON task.
        	    
        	    RULES:
        	    1. Do NOT put a trailing comma after the last field.
        	    2. Ensure the JSON is valid and minified.
        	    
        	    3. The "description" field is MANDATORY. If the user doesn't provide details, create a helpful description yourself.
        	    4. Return ONLY raw JSON.
        	    
        	    Format:
        	    {
        	      "title": "string",
        	      "description": "string",
        	      "dueDate": "YYYY-MM-DD"
        	    }
        	    """.formatted(today);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userText)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.setBearerAuth("ollama");
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                        apiUrl + "/chat/completions",
                        request,
                        String.class
                );

        try {
            ObjectMapper mapper = new ObjectMapper();
            // Need to register JavaTimeModule to handle LocalDate parsing correctly
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
         // This line tells Jackson NOT to crash if it sees a trailing comma
            mapper.enable(com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature());
            
            
            JsonNode root = mapper.readTree(response.getBody());
            String rawContent = root.get("choices").get(0).get("message").get("content").asText();

            // 2. CLEAN THE CONTENT: Extract only the part between { and }
            // This prevents the "Unrecognized token 'Here'" error
            int firstBrace = rawContent.indexOf("{");
            int lastBrace = rawContent.lastIndexOf("}");
            
            if (firstBrace == -1 || lastBrace == -1) {
                throw new RuntimeException("AI response did not contain a valid JSON object: " + rawContent);
            }
            
            String jsonOnly = rawContent.substring(firstBrace, lastBrace + 1);
            jsonOnly = jsonOnly.replaceAll(",(?=\\s*[\\}\\]])", "");
            JsonNode taskJson = mapper.readTree(jsonOnly);

            AiTaskResponse task = new AiTaskResponse();
            task.setTitle(taskJson.get("title").asText());
            if (taskJson.has("description") && !taskJson.get("description").isNull()) {
                task.setDescription(taskJson.get("description").asText());
            } else {
                // Default description so the UI doesn't look empty
                task.setDescription("Task generated from: " + userText);
            }
            String rawDate = taskJson.get("dueDate").asText();
            if (rawDate.contains("T")) {
                // If it's a full timestamp, take only the first 10 characters (the date)
                task.setDueDate(LocalDate.parse(rawDate.substring(0, 10)));
            } else {
                // If it's already a clean date, parse it normally
                task.setDueDate(LocalDate.parse(rawDate));
            }            task.setStatus(TaskStatus.PENDING);

            return task;

        } catch (Exception e) {
            // This will capture the specific reason if it still fails
            throw new RuntimeException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }
}