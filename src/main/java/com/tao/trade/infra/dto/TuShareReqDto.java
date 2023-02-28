package com.tao.trade.infra.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TuShareReqDto {
    private String api_name;
    private String token;
    private Map<String,Object> params;
    private List<String> fields;
}
