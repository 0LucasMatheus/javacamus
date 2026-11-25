package com.umc.javacamus.service;

import com.umc.javacamus.dto.Chamado;
import com.umc.javacamus.dto.ResumoDashboard;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

// monta os numeros do dashboard em cima da lista que vem do microservico, soma tudo num for
@Service
public class DashboardService {

    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM");

    private final ChamadoService chamadoService;

    public DashboardService(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    public ResumoDashboard resumo(String username, int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(dias - 1);

        // tecnico e admin recebem todos, usuario so os dele
        List<Chamado> doPeriodo = new ArrayList<>();
        for (Chamado chamado : chamadoService.listar(username)) {
            if (!chamado.getDataAbertura().toLocalDate().isBefore(inicio)) {
                doPeriodo.add(chamado);
            }
        }

        ResumoDashboard resumo = new ResumoDashboard();
        resumo.setDias(dias);
        resumo.setTotal(doPeriodo.size());

        long minutosResolucao = 0;
        for (Chamado chamado : doPeriodo) {
            if (chamado.isResolvido()) {
                resumo.setResolvidos(resumo.getResolvidos() + 1);
                minutosResolucao += Duration.between(chamado.getDataAbertura(), chamado.getDataResolucao()).toMinutes();
                resumo.getResolvidosPorTecnico().merge(chamado.getTecnicoNome(), 1, Integer::sum);
            } else {
                resumo.setAbertos(resumo.getAbertos() + 1);
            }
        }

        if (resumo.getTotal() > 0) {
            resumo.setTaxaResolucao(resumo.getResolvidos() * 100 / resumo.getTotal());
        }
        if (resumo.getResolvidos() > 0) {
            resumo.setTempoMedio(formatarTempo(minutosResolucao / resumo.getResolvidos()));
        }

        // um ponto por dia, os dias sem chamado ficam zerados pro grafico n pular
        for (LocalDate dia = inicio; !dia.isAfter(hoje); dia = dia.plusDays(1)) {
            int abertosNoDia = 0;
            int resolvidosNoDia = 0;
            for (Chamado chamado : doPeriodo) {
                if (chamado.getDataAbertura().toLocalDate().equals(dia)) {
                    abertosNoDia++;
                }
                if (chamado.isResolvido() && chamado.getDataResolucao().toLocalDate().equals(dia)) {
                    resolvidosNoDia++;
                }
            }
            resumo.getDiasGrafico().add(dia.format(FORMATO_DIA));
            resumo.getAbertosPorDia().add(abertosNoDia);
            resumo.getResolvidosPorDia().add(resolvidosNoDia);
        }

        // os 5 mais recentes pra tabela
        doPeriodo.sort(Comparator.comparing(Chamado::getDataAbertura).reversed());
        for (int i = 0; i < doPeriodo.size() && i < 5; i++) {
            resumo.getUltimos().add(doPeriodo.get(i));
        }

        return resumo;
    }

    // minutos pra algo tipo 2h 15min ou 1d 3h
    public String formatarTempo(long minutos) {
        if (minutos < 60) {
            return minutos + "min";
        }
        long horas = minutos / 60;
        if (horas < 24) {
            return horas + "h " + (minutos % 60) + "min";
        }
        return (horas / 24) + "d " + (horas % 24) + "h";
    }
}
