package com.umc.javacamus.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

// o chamado do jeito que volta do microservico, aqui nao salva nada no mongo
@Getter
@Setter
public class Chamado {

    private String id;
    private String titulo;
    private String descricao;
    private String status;

    private String solicitanteId;
    private String solicitanteNome;
    private LocalDateTime dataAbertura;

    private String solucao;
    private String tecnicoId;
    private String tecnicoNome;
    private LocalDateTime dataResolucao;

    public boolean isResolvido() {
        return "RESOLVIDO".equals(status);
    }
}
