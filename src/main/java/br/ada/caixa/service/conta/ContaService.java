package br.ada.caixa.service.conta;

import br.ada.caixa.entity.Conta;
import br.ada.caixa.entity.TipoCliente;
import br.ada.caixa.entity.TipoConta;
import br.ada.caixa.exceptions.ValidacaoException;
import br.ada.caixa.repository.ClienteRepository;
import br.ada.caixa.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepository;
    private final ClienteRepository clienteRepository;

    public Conta abrirContaPoupanca(String cpf) {
        return clienteRepository.findByDocumento(cpf)
                .map(cliente -> {
                    if (TipoCliente.PJ.equals(cliente.getTipo())) {
                        throw new ValidacaoException("Cliente PJ nao pode ter conta poupanca");
                    }
                    var contaPoupanca = new Conta();
                    contaPoupanca.setTipo(TipoConta.CONTA_POUPANCA);
                    contaPoupanca.setCliente(cliente);
                    contaPoupanca.setSaldo(BigDecimal.ZERO);
                    contaPoupanca.setNumero(gerarNumero(contaRepository));
                    return contaRepository.save(contaPoupanca);
                })
                .orElseThrow(() -> new ValidacaoException("Cliente nao encontrado com o CPF informado!"));
    }

    public static Long gerarNumero(ContaRepository repository) {
        Random random = new Random();
        Long numero;
        do {
            numero = random.nextLong();
        } while (repository.findByNumero(numero).isPresent());

        return numero;
    }

}
