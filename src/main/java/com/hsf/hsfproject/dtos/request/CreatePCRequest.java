package com.hsf.hsfproject.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CreatePCRequest {
    private String name;

    private String description;

    @JsonProperty("total_price")
    private Double totalPrice;

    @JsonProperty("computer_items")
     private List<String> computerItem;

    @JsonProperty("images")
     private List<String> images;
}
