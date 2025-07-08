package com.hsf.hsfproject.dtos.request;

import lombok.Data;

@Data
public class VnPayRequest {
    private long amount;
    private String language;
}
