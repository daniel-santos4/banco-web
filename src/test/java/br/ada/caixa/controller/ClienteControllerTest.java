package br.ada.caixa.controller;

import br.ada.caixa.dto.request.RegistrarClientePFRequestDto;
import br.ada.caixa.dto.request.RegistrarClientePJRequestDto;
import br.ada.caixa.dto.response.ClienteResponseDto;
import br.ada.caixa.dto.response.RegistrarClienteResponseDto;
import br.ada.caixa.entity.Cliente;
import br.ada.caixa.entity.TipoCliente;
import br.ada.caixa.enums.StatusCliente;
import br.ada.caixa.repository.ClienteRepository;
import br.ada.caixa.repository.ContaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClienteControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ContaRepository contaRepository;
    private String url;


    @BeforeEach
    void setup() {

        url = "http://localhost:" + port + "/clientes";

        var cliente1 = Cliente.builder()
                .documento("123456889")
                .nome("Teste 1")
                .dataNascimento(LocalDate.now())
                .status(StatusCliente.ATIVO)
                .tipo(TipoCliente.PF)
                .createdAt(LocalDate.now())
                .build();
        var cliente2 = Cliente.builder()
                .documento("123456789")
                .nome("Teste 2")
                .dataNascimento(LocalDate.now())
                .status(StatusCliente.ATIVO)
                .tipo(TipoCliente.PF)
                .createdAt(LocalDate.now())
                .build();
        var cliente3 = Cliente.builder()
                .documento("123456779")
                .nome("Teste 3")
                .dataNascimento(LocalDate.now())
                .status(StatusCliente.ATIVO)
                .tipo(TipoCliente.PF)
                .createdAt(LocalDate.now())
                .build();
        clienteRepository.saveAll(List.of(cliente1, cliente2, cliente3));
    }

    @AfterEach
    void tearDown() {
        contaRepository.deleteAllInBatch();
        clienteRepository.deleteAllInBatch();
    }

    @Test
    void listarTodosTest() {
        // given
        long expected = clienteRepository.count();

        // when
        var response =
                restTemplate.getForEntity(url, ClienteResponseDto[].class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody().length);
    }

    @Test
    void listarTodosPJTest() {
        // given
        long expected = 0;

        // when
        var response =
                restTemplate.getForEntity(url + "?tipoCliente=PJ", ClienteResponseDto[].class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().length);
    }

    @Test
    void registrarPFTest() {
        // given
        final var dataNascimento = LocalDate.now().minusYears(18);
        final var cpf = "123.456.789-00";
        final var nome = "Teste Post PF";
        final RegistrarClientePFRequestDto clienteDto =
                RegistrarClientePFRequestDto.builder()
                        .cpf(cpf)
                        .nome(nome)
                        .dataNascimento(dataNascimento)
                        .build();

        // when
        var response =
                restTemplate.postForEntity(url + "/pf",
                                           clienteDto,
                                           RegistrarClienteResponseDto.class);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        final var dtoResponse = response.getBody();
        assertEquals(cpf, dtoResponse.getDocumento());

        final var entity = clienteRepository.findByDocumento(cpf);
        assertEquals(nome, entity.get().getNome());

        assertNotNull(dtoResponse.getSaldoResponseDto());
        assertEquals(BigDecimal.ZERO, dtoResponse.getSaldoResponseDto().getSaldo());
        assertNotNull(dtoResponse.getSaldoResponseDto().getNumeroConta());
    }

    @Test
    void registrarPJTest() {
        // given
        final var cnpj = "12.345.678/0001-90";
        final var nomeFantasia = "TesTI";
        final var razaoSocial = "Teste Post PJ";
        final RegistrarClientePJRequestDto clienteDto =
                RegistrarClientePJRequestDto.builder()
                        .cnpj(cnpj)
                        .nomeFantasia(nomeFantasia)
                        .razaoSocial(razaoSocial)
                        .build();

        // when
        var response =
                restTemplate.postForEntity(url + "/pj",
                                           clienteDto,
                                           RegistrarClienteResponseDto.class);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        final var dtoResponse = response.getBody();
        assertEquals(cnpj, dtoResponse.getDocumento());

        final var entity = clienteRepository.findByDocumento(cnpj);
        assertEquals(nomeFantasia, entity.get().getNome());
    }










}