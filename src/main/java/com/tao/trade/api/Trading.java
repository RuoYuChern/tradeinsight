package com.tao.trade.api;

import com.tao.trade.api.facade.QuaintTradeDto;
import com.tao.trade.api.facade.TaoResponse;
import com.tao.trade.domain.TaoData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/api/trading")
@Slf4j
public class Trading {
    @Autowired
    private TaoData taoData;
    @PostMapping("/quaint-buy")
    public TaoResponse<String> addTrading(@RequestBody QuaintTradeDto request){
        if(!StringUtils.hasLength(request.getStock()) || !StringUtils.hasLength(request.getStrategy())){
            return new TaoResponse(HttpStatus.BAD_REQUEST.value(), "Failed", "Args are null");
        }
        try {
            taoData.addQuaint(request);
            return new TaoResponse(HttpStatus.OK.value(), "OK", "Success");
        }catch (Throwable t){
            return new TaoResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed", t.getMessage());
        }
    }
}
