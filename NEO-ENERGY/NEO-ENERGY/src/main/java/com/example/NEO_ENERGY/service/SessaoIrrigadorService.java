package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.model.IrrigadorEntity;
import com.example.NEO_ENERGY.objects.model.SessaoIrrigador;
import com.example.NEO_ENERGY.objects.repository.SessaoIrrigadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessaoIrrigadorService {

    private final SessaoIrrigadorRepository repository;

    // Vazão do irrigador: 1.5 litros por minuto ligado.
    private static final BigDecimal LITROS_POR_MINUTO = new BigDecimal("1.5");

    // Cria uma nova sessão para o irrigador, marcando o início agora.
    // O front informa se o clima está apto via climaApto. Se false, recusa abrir a sessão.
    public SessaoIrrigador abrirSessao(IrrigadorEntity irrigador, boolean climaApto) {
        if (!climaApto) {
            throw new IllegalStateException("Clima não está apto para iniciar a irrigação.");
        }
        SessaoIrrigador sessao = new SessaoIrrigador();
        sessao.setIrrigador(irrigador);
        sessao.setTempoLigado(LocalDateTime.now());
        return repository.save(sessao);
    }

    // Fecha a sessão aberta deste irrigador: marca tempoDesligado, calcula duração e água.
    // Devolve a sessão fechada para que o IrrigadorService some no acumulado.
    public SessaoIrrigador fecharSessaoAberta(IrrigadorEntity irrigador) {
        SessaoIrrigador sessao = repository.findByIrrigadorAndTempoDesligadoIsNull(irrigador)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Não existe sessão aberta para este irrigador."));

        LocalDateTime agora = LocalDateTime.now();
        long duracaoSegundos = Duration.between(sessao.getTempoLigado(), agora).getSeconds();

        sessao.setTempoDesligado(agora);
        sessao.setDuracaoSegundos(duracaoSegundos);
        sessao.setAgua(calcularAguaConsumida(duracaoSegundos));

        return repository.save(sessao);
    }

    public Optional<SessaoIrrigador> buscarSessaoAberta(IrrigadorEntity irrigador) {
        return repository.findByIrrigadorAndTempoDesligadoIsNull(irrigador);
    }

    public List<SessaoIrrigador> listarSessoes(IrrigadorEntity irrigador) {
        return repository.findByIrrigador(irrigador);
    }

    // Fórmula isolada: duração em segundos -> litros consumidos.
    // Está privada porque é detalhe de implementação. Se um dia outro lugar precisar,
    // a gente promove para public sem mudar quem já chama.
    private BigDecimal calcularAguaConsumida(long duracaoSegundos) {
        BigDecimal duracaoMinutos = BigDecimal.valueOf(duracaoSegundos)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return duracaoMinutos.multiply(LITROS_POR_MINUTO);
    }
}
