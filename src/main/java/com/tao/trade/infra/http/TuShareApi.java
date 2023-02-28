package com.tao.trade.infra.http;

import com.tao.trade.infra.dto.TuShareReqDto;
import com.tao.trade.infra.dto.TuShareRspDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "TuShare", url = "http://api.tushare.pro")
public interface TuShareApi {
    @RequestMapping(method = RequestMethod.POST, value = "/")
    TuShareRspDto get(TuShareReqDto req);
}
