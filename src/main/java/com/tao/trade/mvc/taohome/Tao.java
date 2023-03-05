package com.tao.trade.mvc.taohome;

import com.tao.trade.domain.TaoData;
import com.tao.trade.facade.DashBoardDto;
import com.tao.trade.utils.Help;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class Tao {
    private final static String prefix = "tao-home";

    @Autowired
    private TaoData taoData;
    @GetMapping({"/","","index"})
    public String home(Model model){
        model.addAttribute("title", "TradeInsight");
        model.addAttribute("company","Tao");
        DashBoardDto dashBoard = taoData.getDashBoard();
        model.addAttribute("dashBoard", dashBoard);
        return String.format("%s/home", prefix);
    }

    @GetMapping({"/detail"})
    public String homeDetail(@RequestParam(name="page", required = false) Integer num, Model model){
        model.addAttribute("title", "TradeInsight");
        model.addAttribute("company","Tao");
        DashBoardDto dashBoard = taoData.getDashBoard();
        if(num == null){
            num = Integer.valueOf("1");
        }
        int offset = (num - 1) * 6;
        Help.inverted(dashBoard.getGdpList(), dashBoard, DashBoardDto::setGdpList);
        Help.inverted(dashBoard.getCpiDtoList(), dashBoard, DashBoardDto::setCpiDtoList);
        Help.inverted(dashBoard.getPpiDtoList(), dashBoard, DashBoardDto::setPpiDtoList);
        Help.inverted(dashBoard.getMoneyList(), dashBoard, DashBoardDto::setMoneyList);

        Help.inverted(offset, 6, dashBoard.getShMARKET(), dashBoard, DashBoardDto::setShMARKET);
        Help.inverted(offset, 6, dashBoard.getShSTAR(), dashBoard, DashBoardDto::setShSTAR);
        Help.inverted(offset, 6, dashBoard.getSzMarket(), dashBoard, DashBoardDto::setSzMarket);
        Help.inverted(offset, 6, dashBoard.getSzMain(), dashBoard, DashBoardDto::setSzMain);
        Help.inverted(offset, 6, dashBoard.getSzGEM(), dashBoard, DashBoardDto::setSzGEM);

        model.addAttribute("dashBoard", dashBoard);
        if(num > 1) {
            model.addAttribute("prePage", String.format("/detail?page=%d", num - 1));
        }else {
            model.addAttribute("prePage", String.format("/detail?page=%d", 1));
        }

        if(num < 5) {
            model.addAttribute("nextPage", String.format("/detail?page=%d", num + 1));
        }else {
            model.addAttribute("nextPage", String.format("/detail?page=%d", 5));
        }

        return String.format("%s/detail", prefix);
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
