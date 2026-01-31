package org.tribenet.tribenet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String keyId;
}
