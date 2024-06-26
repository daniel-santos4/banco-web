package br.ada.caixa.service.cliente;

import br.ada.caixa.dto.request.RegistrarClientePFRequestDto;
import br.ada.caixa.dto.request.RegistrarClientePJRequestDto;
import br.ada.caixa.dto.response.ClienteResponseDto;
import br.ada.caixa.dto.response.RegistrarClienteResponseDto;
import br.ada.caixa.entity.Cliente;
import br.ada.caixa.entity.TipoCliente;
import br.ada.caixa.repository.ClienteRepository;
import br.ada.caixa.repository.ContaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;

class ClienteServiceTest {
    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final ContaRepository contaRepository = mock(ContaRepository.class);
    private final ModelMapper modelMapper = mock(ModelMapper.class);
    private ClienteService sut = new ClienteService(clienteRepository, contaRepository, modelMapper);

    @ParameterizedTest
    @NullSource
    @EnumSource(value = TipoCliente.class)
    void listarTodosTest(TipoCliente tipoCliente) {
        // given
        Cliente clientePF = new Cliente();
        clientePF.setTipo(TipoCliente.PF);
        Cliente clientePJ = new Cliente();
        clientePJ.setTipo(TipoCliente.PJ);
        ClienteResponseDto clientePFDto = new ClienteResponseDto();
        ClienteResponseDto clientePJDto = new ClienteResponseDto();
        List<ClienteResponseDto> expected = new ArrayList<ClienteResponseDto>();
        given(modelMapper.map(clientePF, ClienteResponseDto.class)).willReturn(clientePFDto);
        given(modelMapper.map(clientePJ, ClienteResponseDto.class)).willReturn(clientePJDto);
        if (tipoCliente == null) {
            given(clienteRepository.findAll())
                    .willReturn(List.of(clientePF, clientePJ));
            expected.add(clientePFDto);
            expected.add(clientePJDto);
        } else if (TipoCliente.PF.equals(tipoCliente)) {
            given(clienteRepository.findAllByTipo(tipoCliente))
                    .willReturn(List.of(clientePF));
            expected.add(clientePFDto);
        } else { // PJ
            given(clienteRepository.findAllByTipo(tipoCliente))
                    .willReturn(List.of(clientePJ));
            expected.add(clientePJDto);
        }

        // when
        List<ClienteResponseDto> actual;
        if (tipoCliente == null) {
            actual = sut.listarTodos();
        } else {
            actual = sut.listarTodos(tipoCliente);
        }

        // then
        assertEquals(expected, actual);
    }

    @Test
    void registrarPF() {
        // given
        String expected = "123.456.789-00";
        Cliente cliente = Cliente.builder()
                .documento(expected)
                .tipo(TipoCliente.PF)
                .build();
        RegistrarClientePFRequestDto entrada = mock(RegistrarClientePFRequestDto.class);
        given(modelMapper.map(entrada, Cliente.class)).willReturn(cliente);
        given(clienteRepository.save(cliente)).willReturn(cliente);

        // when
        RegistrarClienteResponseDto response = sut.registrarPF(entrada);
        String actual = response.getDocumento();

        // then
        assertEquals(expected, actual);
        assertNotNull(response.getSaldoResponseDto().getNumeroConta());
        assertEquals(BigDecimal.ZERO, response.getSaldoResponseDto().getSaldo());
    }

    @Test
    void registrarPJ() {
        // given
        String expected = "12.345.678/0009-00";
        Cliente cliente = Cliente.builder()
                .documento(expected)
                .tipo(TipoCliente.PJ)
                .build();
        RegistrarClientePJRequestDto entrada = mock(RegistrarClientePJRequestDto.class);
        given(modelMapper.map(entrada, Cliente.class)).willReturn(cliente);
        given(clienteRepository.save(cliente)).willReturn(cliente);

        // when
        RegistrarClienteResponseDto response = sut.registrarPJ(entrada);
        String actual = response.getDocumento();

        // then
        assertEquals(expected, actual);
        assertNotNull(response.getSaldoResponseDto().getNumeroConta());
        assertEquals(BigDecimal.ZERO, response.getSaldoResponseDto().getSaldo());
    }
}