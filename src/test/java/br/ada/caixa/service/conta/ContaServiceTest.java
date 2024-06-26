package br.ada.caixa.service.conta;

import br.ada.caixa.entity.Cliente;
import br.ada.caixa.entity.Conta;
import br.ada.caixa.entity.TipoCliente;
import br.ada.caixa.exceptions.ValidacaoException;
import br.ada.caixa.repository.ClienteRepository;
import br.ada.caixa.repository.ContaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class ContaServiceTest {
    private ContaRepository contaRepository = mock(ContaRepository.class);
    private ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private ContaService sut = new ContaService(contaRepository, clienteRepository);

    @Test
    void abrirContaPoupancaTest() {
        // given
        final String cpf = "123.456.789-00";
        final Cliente cliente = mock(Cliente.class);
        cliente.setTipo(TipoCliente.PF);
        final Optional<Cliente> opCliente = Optional.of(cliente);
        doReturn(opCliente).when(clienteRepository).findByDocumento(cpf);
        final Conta expected = mock(Conta.class);
        given(contaRepository.save(any())).willReturn(expected);

        // when
        Conta actual = sut.abrirContaPoupanca(cpf);

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Dado um cliente PJ quando abrirContaPoupanca então é lançada ValidacaoException")
    void abrirContaPoupancaPJTest() {
        // given
        final String cnpj = "12.345.678/0009-00";
        final Cliente cliente = mock(Cliente.class);
        cliente.setTipo(TipoCliente.PJ);
        final Optional<Cliente> opCliente = Optional.of(cliente);
        doReturn(opCliente).when(clienteRepository).findByDocumento(cnpj);

        // when
        // then
        assertThrows(ValidacaoException.class, () -> sut.abrirContaPoupanca(cnpj));
    }
}