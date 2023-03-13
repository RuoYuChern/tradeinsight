package com.tao.trade.api;

import com.tao.trade.api.facade.TaoResponse;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping(path="/api/ai")
@Slf4j
public class AiTalk {
    private volatile OpenAiService openAiService;

    @GetMapping("/talk")
    public TaoResponse<String> talk(@RequestParam(name = "world") String world){
        if(openAiService == null){
            return new TaoResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Open Ai is not ok", null);
        }
        List<ChatMessage> messages = new LinkedList<>();
        messages.add( new ChatMessage("user", world));
        try {
            ChatCompletionRequest req = ChatCompletionRequest.builder().messages(messages)
                    .model("gpt-3.5-turbo-0301")
                    .n(10)
                    .user("Tao")
                    .maxTokens(20).build();
            List<ChatCompletionChoice> choices = openAiService.createChatCompletion(req).getChoices();
            StringBuilder sb = new StringBuilder();
            for (ChatCompletionChoice c : choices) {
                sb.append(c.getMessage().getContent());
            }
            return new TaoResponse(HttpStatus.OK.value(), "OK", sb.toString());
        }catch (Throwable t){
            log.warn("talk:{}", t.getMessage());
            return new TaoResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), t.getMessage(), null);
        }
    }

    @PostMapping("/set")
    public TaoResponse<String> set(@RequestParam(name = "token") String token){
        openAiService = new OpenAiService(token);
        return new TaoResponse(HttpStatus.OK.value(), "OK", null);
    }
}
