package br.ada.caixa.service.operacoesbancarias.investimento;

import br.ada.caixa.entity.Cliente;
import br.ada.caixa.entity.Conta;
import br.ada.caixa.entity.TipoCliente;
import br.ada.caixa.entity.TipoConta;
import br.ada.caixa.exceptions.ValidacaoException;
import br.ada.caixa.repository.ClienteRepository;
import br.ada.caixa.repository.ContaRepository;
import br.ada.caixa.service.conta.ContaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class InvestimentoService {

    @Qualifier("investimentoOperacaoPF")
    private final InvestimentoOperacao investimentoOperacaoPF;
    @Qualifier("investimentoOperacaoPJ")
    private final InvestimentoOperacao investimentoOperacaoPJ;

    private final ContaRepository contaRepository;
    private final ClienteRepository clienteRepository;

    public Conta investir(String documentoCliente, BigDecimal valor) {
        var clienteOptional = clienteRepository.findByDocumento(documentoCliente);
        var cliente = clienteOptional.orElseThrow(() -> new ValidacaoException("Cliente nao existe"));
        var contaInvestimento = getSingleContaInvestimento(cliente);
        getOperacaoTipoCliente(cliente).executar(contaInvestimento, valor);
        return contaRepository.save(contaInvestimento);
    }

    // TODO: Mover pra outra classe pra respeitar o princípio da responsabilidade única
    public void render() {
        for (Conta conta : contaRepository.findContasByTipo(TipoConta.CONTA_INVESTIMENTO)) {
            getOperacaoTipoCliente(conta.getCliente()).render(conta);
        }
    }

    private Conta getSingleContaInvestimento(final Cliente cliente) {
        var contas =
                contaRepository
                        .findContasByClienteAndTipo(cliente, TipoConta.CONTA_INVESTIMENTO);

        if (contas.size() > 1) {
            throw new ValidacaoException("Cliente possui mais de uma conta investimento");
        }
        Conta contaInvestimento;
        if (contas.isEmpty()) {
            contaInvestimento = new Conta();
            contaInvestimento.setTipo(TipoConta.CONTA_INVESTIMENTO);
            contaInvestimento.setCliente(cliente);
            contaInvestimento.setSaldo(BigDecimal.ZERO);
            contaInvestimento.setNumero(ContaService.gerarNumero(contaRepository));
        } else {
            contaInvestimento = contas.get(0);
        }
        return contaInvestimento;
    }

    private InvestimentoOperacao getOperacaoTipoCliente(Cliente cliente) {
        if (cliente.getTipo().equals(TipoCliente.PF)) {
            return investimentoOperacaoPF;
        } else if (cliente.getTipo().equals(TipoCliente.PJ)) {
            return investimentoOperacaoPJ;
        } else {
            throw new ValidacaoException("Operacao investimento nao encontrada!");
        }
    }

}
