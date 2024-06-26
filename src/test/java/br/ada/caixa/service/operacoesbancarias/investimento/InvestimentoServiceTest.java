package br.ada.caixa.service.operacoesbancarias.investimento;

import br.ada.caixa.entity.Cliente;
import br.ada.caixa.entity.Conta;
import br.ada.caixa.entity.TipoCliente;
import br.ada.caixa.entity.TipoConta;
import br.ada.caixa.repository.ClienteRepository;
import br.ada.caixa.repository.ContaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvestimentoServiceTest {

    @Mock
    private InvestimentoOperacao investimentoOperacaoPF;
    @Mock
    private InvestimentoOperacao investimentoOperacaoPJ;
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private InvestimentoService service;

    @Test
    @DisplayName("dado um cliente PF com uma conta investimento unica, " +
            "quando investido um valor, " +
            "entao a conta é atualizada no banco de dados")
    void investirContaPFEncontradaTest() {
        // given
        final Conta expected = mock(Conta.class);
        final Cliente cliente = mock(Cliente.class);

        final Random random = new Random();
        final String documentoCliente = "some document";
        final BigDecimal valor =
                BigDecimal
                        .valueOf(random.nextDouble())
                        .setScale(2, RoundingMode.HALF_UP);


        given(clienteRepository.findByDocumento(documentoCliente))
                .willReturn(Optional.of(cliente));

        given(contaRepository
                      .findContasByClienteAndTipo(cliente, TipoConta.CONTA_INVESTIMENTO))
                .willReturn(List.of(expected));
        given(contaRepository.save(expected))
                .willReturn(expected);

        given(cliente.getTipo())
                .willReturn(TipoCliente.PF);

        // when
        Conta actual = service.investir(documentoCliente, valor);

        // then
        assertEquals(expected, actual);
        verify(investimentoOperacaoPF, times(1)).executar(actual, valor);
        verify(contaRepository, times(1)).save(actual);
    }

    @Test
    @DisplayName("dado um cliente PF sem conta de investimento será criada uma nova conta")
    void investirContaPFNaoEncontradaTest() {
        // given
        // final Conta expected = mock(Conta.class);
        final Cliente cliente = mock(Cliente.class);

        final Random random = new Random();
        final String documentoCliente = "some document";
        final BigDecimal valor =

                BigDecimal
                        .valueOf(random.nextDouble())
                        .setScale(2, RoundingMode.HALF_UP);

        given(clienteRepository.findByDocumento(documentoCliente))
                .willReturn(Optional.of(cliente));
        given(contaRepository
                .findContasByClienteAndTipo(cliente, TipoConta.CONTA_INVESTIMENTO))
                .willReturn(new ArrayList<>());

        var expected = new Conta();
        expected.setTipo(TipoConta.CONTA_INVESTIMENTO);
        expected.setCliente(cliente);
        expected.setSaldo(BigDecimal.ZERO);

        given(contaRepository.save(expected))
                .willReturn(expected);

        given(cliente.getTipo())
                .willReturn(TipoCliente.PF);

        // when
        Conta actual = service.investir(documentoCliente, valor);

        // then
        assertEquals(expected, actual);
        verify(investimentoOperacaoPF, times(1)).executar(actual, valor);
        verify(contaRepository, times(1)).save(actual);
    }

    /*
    1 - Variacao qdo o cliente nao é encontrado
    2 - Testar qdo o cliente tem + de 1 conta investimento ?
    4 - E se o cliente for PJ?
     */
}