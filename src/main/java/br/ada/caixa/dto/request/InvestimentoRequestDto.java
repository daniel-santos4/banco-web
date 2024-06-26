package br.ada.caixa.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class InvestimentoRequestDto {

    private String documentoCliente;
    private BigDecimal valor;

}
