package com.umc.javacamus.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

// tudo que a home precisa pra montar o dashboard
@Getter
@Setter
public class ResumoDashboard {

    private int dias;
    private int total;
    private int abertos;
    private int resolvidos;
    private int taxaResolucao;
    private String tempoMedio = "-";

    // dia (dd/MM) -> quantidade, ja vem com os dias vazios zerados
    private List<String> diasGrafico = new ArrayList<>();
    private List<Integer> abertosPorDia = new ArrayList<>();
    private List<Integer> resolvidosPorDia = new ArrayList<>();

    // tecnico -> quantos resolveu
    private Map<String, Integer> resolvidosPorTecnico = new LinkedHashMap<>();

    private List<Chamado> ultimos = new ArrayList<>();
}
