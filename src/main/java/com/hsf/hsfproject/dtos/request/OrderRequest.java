package com.hsf.hsfproject.dtos.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String userId;
    private String cartId;


}
