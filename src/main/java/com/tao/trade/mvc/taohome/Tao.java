package com.tao.trade.mvc.taohome;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class Tao {
    private final static String prefix = "tao-home";
    @GetMapping({"/","","index"})
    public String home(Model model){
        model.addAttribute("title", "TradeInsight");
        model.addAttribute("company","Tao");
        return String.format("%s/home", prefix);
    }

    @GetMapping({"/rebound"})
    public String rebound(Model model){
        model.addAttribute("title", "TradeInsight");
        model.addAttribute("company","Tao");
        return String.format("%s/rebound", prefix);
    }

    @GetMapping({"/recommend"})
    public String recommend(Model model){
        model.addAttribute("title", "TradeInsight");
        model.addAttribute("company","Tao");
        return String.format("%s/recommend", prefix);
    }

    @GetMapping({"/market-sentiment"})
    public String marketSentiment(Model model){
        model.addAttribute("title", "TradeInsight");
        model.addAttribute("company","Tao");
        return String.format("%s/market-sentiment", prefix);
    }

    @GetMapping({"/crypto-market"})
    public String cryptoMarket(Model model){
        model.addAttribute("title", "TradeInsight");
        model.addAttribute("company","Tao");
        return String.format("%s/crypto-market", prefix);
    }
}
