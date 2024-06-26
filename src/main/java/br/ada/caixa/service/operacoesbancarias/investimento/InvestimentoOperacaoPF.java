package br.ada.caixa.service.operacoesbancarias.investimento;

import br.ada.caixa.entity.Conta;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("investimentoOperacaoPF")
public class InvestimentoOperacaoPF implements InvestimentoOperacao {
    private static final BigDecimal RENDIMENTO_INVESTIMENTO = BigDecimal.valueOf(1.01);

    @Override
    public void executar(Conta contaInvestimento, BigDecimal valor) {
        // O rendimento é aplicado uma vez por mês e não na execução do investimento
        // valor = valor.multiply(RENDIMENTO_INVESTIMENTO);
        contaInvestimento.setSaldo(contaInvestimento.getSaldo().add(valor));
    }

    @Override
    public void render(Conta contaInvestimento) {
        contaInvestimento.setSaldo(contaInvestimento.getSaldo().multiply(RENDIMENTO_INVESTIMENTO));
    }
}
