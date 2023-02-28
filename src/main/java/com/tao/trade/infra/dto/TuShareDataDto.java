package com.tao.trade.infra.dto;

import lombok.Data;

import java.util.List;

@Data
public class TuShareDataDto {
    private List<String> fields;
    private List<List<Object>> items;
    private boolean has_more;
}
