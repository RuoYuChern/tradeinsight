package com.tao.trade.infra;

import com.alibaba.fastjson.JSON;
import com.tao.trade.infra.vo.SinaDailyVo;
import com.tao.trade.infra.vo.SinaRealVo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class SinaClient {
    private static final String URL_BASE = "https://quotes.sina.cn/cn/api/jsonp_v2.php";
    private static final String REAL_URL = "http://hq.sinajs.cn";
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

    public List<SinaRealVo> getReal(List<String> stocks){
        StringBuilder sb = new StringBuilder();
        for(String st:stocks){
            if(sb.length() > 0){
                sb.append(",");
            }
            sb.append(to(st));
        }
        String stockList = sb.toString();
        HttpUrl.Builder builder = HttpUrl.parse(String.format("%s/list=%s", REAL_URL, stockList)).newBuilder();
        Request req = new Request.Builder().addHeader("Referer", "http://finance.sina.com.cn").url(builder.build()).build();
        try {
            Response rsp = client.newCall(req).execute();
            String values = rsp.body().string().replace("\n", "");
            if(!StringUtils.hasLength(values)){
                return null;
            }
            System.out.println("values:" + values);
            String [] stockPrices = values.split(";");
            List<SinaRealVo> voList = new ArrayList<>(stocks.size());
            for(String p:stockPrices){
                if(StringUtils.hasLength(p)) {
                    SinaRealVo vo = from(p);
                    String tsCode = getTsCode(p);
                    if(StringUtils.hasLength(tsCode)) {
                        vo.setTsCode(tsCode);
                        voList.add(vo);
                    }
                }
            }
            return voList;
        }catch (Throwable t){
            t.printStackTrace();
            log.info("getReal exceptions:", t);
        }
        return null;
    }

    private static String getTsCode(String value){
        int pos = value.indexOf("hq_str_");
        int end = value.indexOf("=", pos + 1);
        if(pos < 0 || end < 0){
            throw null;
        }
        String ts = value.substring(pos + "hq_str_".length(), end);
        if(ts.startsWith("sz")){
            return String.format("%s.SZ", ts.substring(2));
        }
        if(ts.startsWith("sh")){
            return String.format("%s.SH", ts.substring(2));
        }
        if(ts.startsWith("bj")) {
            return String.format("%s.BJ", ts.substring(2));
        }
        return ts;
    }

    private static SinaRealVo from(String value){
        int pos = value.indexOf("\"");
        int end = value.lastIndexOf("\"");
        if(pos < 0 || end < 0){
            throw new RuntimeException(String.format("[%s] is error", value));
        }
        String [] values = value.substring(pos + 1, end).split(",");
        SinaRealVo realVo = new SinaRealVo();
        realVo.setName(values[0]);
        realVo.setOpen(new BigDecimal(values[1]));
        realVo.setPreClose(new BigDecimal(values[2]));
        realVo.setCurePrice(new BigDecimal(values[3]));
        realVo.setHigh(new BigDecimal(values[4]));
        realVo.setLow(new BigDecimal(values[5]));
        realVo.setDate(values[30].replace("-",""));
        realVo.setTime(values[31]);
        return realVo;
    }

    private static String to(String st){
        String substring = st.substring(0, st.length() - 3);
        if(st.endsWith(".SZ")){
            return String.format("sz%s", substring);
        }
        if(st.endsWith(".SH")){

            return String.format("sh%s", substring);
        }
        if(st.endsWith(".BJ")){
            return String.format("bj%s", substring);
        }
        return st;
    }

//    public static void main(String []args){
//        List<String> list = new ArrayList<>(3);
//        list.add("000506.SZ");
//        list.add("600339.SH");
//        list.add("832735.BJ");
//
//        SinaClient sinaClient = new SinaClient();
//        List<SinaRealVo> realVos = sinaClient.getReal(list);
//        for(SinaRealVo vo:realVos){
//            System.out.println(JSON.toJSONString(vo));
//        }
//    }
}
