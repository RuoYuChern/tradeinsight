package com.tao.trade.api;


import com.tao.trade.api.facade.DailyHqDto;
import com.tao.trade.api.facade.StockRangDailyDto;
import com.tao.trade.api.facade.TaoResponse;
import com.tao.trade.domain.TaoData;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.utils.DateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping(path="/api/hq")
@Slf4j
public class StockHqApi {
    @Autowired
    private TaoData taoData;

    @GetMapping({"/get-symbol"})
    public TaoResponse<StockRangDailyDto> getSymbol(@RequestParam(name = "stock") String stock,
                                                    @RequestParam(name = "startDate") String startDate,
                                                    @RequestParam(name = "endDate") String endDate){
        try{
            Date first = DateHelper.strToDate("yyyyMMdd", startDate);
            Date end = DateHelper.strToDate("yyyyMMdd", endDate);
            List<CnStockDaily> dailyList = taoData.getSymbolBetween(stock, first, end);
            if(!CollectionUtils.isEmpty(dailyList)){
                List<String> holidays = taoData.getHoliday(startDate, endDate);
                StockRangDailyDto dto = new StockRangDailyDto();
                List<DailyHqDto> hqDtoList = new LinkedList<>();
                for(CnStockDaily daily:dailyList){
                    DailyHqDto hqDto = new DailyHqDto();
                    hqDto.setValue(daily.getClosePrice());
                    hqDto.setDate(DateHelper.dateToStr("yyyy-MM-dd", daily.getTradeDate()));
                    hqDtoList.add(hqDto);
                }
                dto.setDtoList(hqDtoList);
                dto.setHolidays(holidays);
                dto.setTsCode(taoData.getTsCode(stock));
                return new TaoResponse<>(HttpStatus.OK.value(), "OK", dto);
            }else {
                return new TaoResponse<>(HttpStatus.NOT_FOUND.value(), "Not find", null);
            }
        }catch (Throwable t){
            return new TaoResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Exceptions", null);
        }
    }
}
