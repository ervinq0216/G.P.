package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.AiChatDTO;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    // TODO: 请替换为你自己的 DeepSeek API Key
    private static final String API_KEY = "sk-ab306525f0f2414dbdcc04109861703e";
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody AiChatDTO chatDTO) {
        if (chatDTO.getMessage() == null || chatDTO.getMessage().trim().isEmpty()) {
            return Result.error("请输入问题");
        }

        try {
            // 构建请求体 (JSON)
            // 这里使用简单的字符串拼接，实际项目中建议使用 Jackson/Gson
            String jsonBody = String.format(
                    "{" +
                            "  \"model\": \"deepseek-chat\"," +
                            "  \"messages\": [" +
                            "    {\"role\": \"system\", \"content\": \"你是一名专业的医疗助手。请根据用户的症状提供初步建议，并提醒用户这就医。回答要简洁明了。\"}," +
                            "    {\"role\": \"user\", \"content\": \"%s\"}" +
                            "  ]," +
                            "  \"stream\": false" +
                            "}",
                    chatDTO.getMessage().replace("\"", "\\\"").replace("\n", "\\n") // 简单转义
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // 解析返回的 JSON (这里为了演示简单，直接返回原始 JSON 或简单处理)
                // 实际建议解析 choices[0].message.content
                return Result.success(response.body());
            } else {
                return Result.error("AI 服务响应异常: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI 服务调用失败: " + e.getMessage());
        }
    }
}