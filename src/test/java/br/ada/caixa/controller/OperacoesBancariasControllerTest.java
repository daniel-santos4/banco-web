package br.ada.caixa.controller;

import br.ada.caixa.dto.request.DepositoRequestDto;
import br.ada.caixa.dto.request.InvestimentoRequestDto;
import br.ada.caixa.dto.request.SaqueRequestDto;
import br.ada.caixa.dto.request.TransferenciaRequestDto;
import br.ada.caixa.dto.response.SaldoResponseDto;
import br.ada.caixa.entity.Cliente;
import br.ada.caixa.entity.Conta;
import br.ada.caixa.entity.TipoCliente;
import br.ada.caixa.entity.TipoConta;
import br.ada.caixa.enums.StatusCliente;
import br.ada.caixa.exceptions.ValidacaoException;
import br.ada.caixa.repository.ClienteRepository;
import br.ada.caixa.repository.ContaRepository;
import br.ada.caixa.service.conta.ContaService;
import br.ada.caixa.service.operacoesbancarias.deposito.DepositoService;
import br.ada.caixa.service.operacoesbancarias.investimento.InvestimentoService;
import br.ada.caixa.service.operacoesbancarias.saldo.SaldoService;
import br.ada.caixa.service.operacoesbancarias.saque.SaqueService;
import br.ada.caixa.service.operacoesbancarias.transferencia.TransferenciaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
class OperacoesBancariasControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @SpyBean
    private ContaRepository contaRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    DepositoService depositoService;
    @Autowired
    SaqueService saqueService;
    @Autowired
    TransferenciaService transferenciaService;
    @Autowired
    SaldoService saldoService;
    @Autowired
    InvestimentoService investimentoService;
    @Autowired
    ContaService contaService;

    private String url;

    @BeforeEach
    void setUp() {
        // SET URL
        url = "http://localhost:" + port + "/operacoes";

        // CRIAR CLIENTES
        var cliente1 = Cliente.builder()
                .documento("123.456.789-00")
                .nome("Teste 1")
                .dataNascimento(LocalDate.now())
                .status(StatusCliente.ATIVO)
                .tipo(TipoCliente.PF)
                .createdAt(LocalDate.now())
                .build();
        var cliente2 = Cliente.builder()
                .documento("12.345.678/0009-00")
                .nome("Teste 2")
                .dataNascimento(LocalDate.now())
                .status(StatusCliente.ATIVO)
                .tipo(TipoCliente.PJ)
                .createdAt(LocalDate.now())
                .build();

        clienteRepository.saveAllAndFlush(List.of(cliente1, cliente2));

        // CRIAR CONTAS
        var contaCorrente1 = Conta.builder()
                .numero(1L)
                .saldo(BigDecimal.ZERO)
                .tipo(TipoConta.CONTA_CORRENTE)
                .cliente(cliente1)
                .createdAt(LocalDate.now())
                .build();

        var contaCorrente2 = Conta.builder()
                .numero(2L)
                .saldo(BigDecimal.valueOf(100.50))
                .tipo(TipoConta.CONTA_CORRENTE)
                .cliente(cliente2)
                .createdAt(LocalDate.now())
                .build();

        contaRepository.saveAllAndFlush(List.of(contaCorrente1, contaCorrente2));

    }

    @AfterEach
    void tearDown() {
        contaRepository.deleteAllInBatch();
        clienteRepository.deleteAllInBatch();
    }

    @Test
    void depositarTest() {
        // given
        final var valor = BigDecimal.valueOf(100.50);
        final var numeroConta = 1L;
        DepositoRequestDto depositoRequestDto =
                DepositoRequestDto.builder()
                        .numeroConta(numeroConta)
                        .valor(valor)
                        .build();

        // when
        var response = restTemplate.postForEntity(url + "/depositar", depositoRequestDto, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(valor.compareTo(contaRepository.findByNumero(numeroConta).get().getSaldo())).isZero();
        // assertEquals(0, valor.compareTo(contaRepository.findByNumero(numeroConta).get().getSaldo()));
        verify(contaRepository).save(any(Conta.class));
    }

    @Test
    void sacarTest() {
        // given
        final var valor = BigDecimal.valueOf(100);
        final var numeroConta = 2L;
        SaqueRequestDto saqueRequestDto =
                SaqueRequestDto.builder()
                        .numeroConta(numeroConta)
                        .valor(valor)
                        .build();

        // when
        var response = restTemplate.postForEntity(url + "/sacar", saqueRequestDto, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(0, BigDecimal.ZERO.compareTo(contaRepository.findByNumero(numeroConta).get().getSaldo()));
        verify(contaRepository).save(any(Conta.class));
    }

    @Test
    void transferirTest() {
        // given
        final var valor = BigDecimal.valueOf(100);
        final var numeroContaDestino = 1L;
        final var numeroContaOrigem = 2L;
        TransferenciaRequestDto transferenciaRequestDto =
                TransferenciaRequestDto.builder()
                        .numeroContaOrigem(numeroContaOrigem)
                        .numeroContaDestino(numeroContaDestino)
                        .valor(valor)
                        .build();

        // when
        var response = restTemplate.postForEntity(url + "/transferir", transferenciaRequestDto, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(0, valor.compareTo(contaRepository.findByNumero(numeroContaDestino).get().getSaldo()));
        assertEquals(0, BigDecimal.ZERO.compareTo(contaRepository.findByNumero(numeroContaOrigem).get().getSaldo()));
        verify(contaRepository, times(2)).save(any(Conta.class));
    }

    @Test
    void consultarSaldoTest() {
        // given
        final var expected = BigDecimal.valueOf(100.50);
        final var numeroConta = 2L;

        // when
        var response = restTemplate.getForEntity(url + "/saldo/" + numeroConta, SaldoResponseDto.class);
        var actual = response.getBody().getSaldo();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(0, expected.compareTo(actual));
        assertEquals(numeroConta, response.getBody().getNumeroConta());
    }

    @Test
    void investirTest() {
        // given
        var expected = BigDecimal.valueOf(50);
        var investimentoRequestDto = InvestimentoRequestDto.builder()
                .documentoCliente("12.345.678/0009-00")
                .valor(expected)
                .build();

        // when
        var response = restTemplate.postForEntity(url + "/investir", investimentoRequestDto, SaldoResponseDto.class);
        var actual = response.getBody().getSaldo();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(expected, actual);
    }

    @Test
    void abrirContaPoupancaTest() {
        // given
        var cliente = clienteRepository.findByDocumento("123.456.789-00").get();
        var contas = contaRepository.findContasByClienteAndTipo(cliente, TipoConta.CONTA_POUPANCA).size();

        // when
        var response = restTemplate.postForEntity(url + "/abrir-conta-poupanca/" + cliente.getDocumento(), cliente.getDocumento(), SaldoResponseDto.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var poupanca = contaRepository.findContasByClienteAndTipo(cliente, TipoConta.CONTA_POUPANCA).get(contas);
        assertEquals(0, BigDecimal.ZERO.compareTo(poupanca.getSaldo()));
    }
}