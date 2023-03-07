package com.tao.trade.infra;

import com.alibaba.fastjson.JSON;
import com.tao.trade.infra.vo.SinaDailyVo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SinaClient {
    private static final String URL_BASE = "https://quotes.sina.cn/cn/api/jsonp_v2.php";
    private OkHttpClient client;
    public SinaClient(){
        client = new OkHttpClient();
    }

    public List<SinaDailyVo> getSymbolDaily(String symbol, int scale, String ma, int dataLen){
        String rUrl = String.format("%s/%s_%d_%d=/CN_MarketDataService.getKLineData", URL_BASE,symbol,
                scale, (System.currentTimeMillis()/1000));
        HttpUrl.Builder builder = HttpUrl.parse(rUrl).newBuilder();
        builder.addQueryParameter("symbol", symbol);
        builder.addQueryParameter("scale", String.valueOf(scale));
        builder.addQueryParameter("ma", ma);
        builder.addQueryParameter("datalen", String.valueOf(dataLen));
        Request req = new Request.Builder().url(builder.build()).build();
        try {
            Response rsp = client.newCall(req).execute();
            String values = rsp.body().string();
            int start = values.indexOf("[");
            int end   = values.lastIndexOf("]");
            if((start > 0) && (end > start)){
                List<SinaDailyVo> list = JSON.parseArray(values.substring(start, (end + 1)), SinaDailyVo.class);
                return list;
            }else{
                log.warn("Error:{}", values);
            }
        }catch (Throwable t){
            log.info("getSymbolDaily exceptions:", t);
        }
        return null;
    }
}
