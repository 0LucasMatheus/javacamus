package com.umc.javacamus.service;

import com.umc.javacamus.dto.Chamado;
import com.umc.javacamus.entity.Usuario;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// conversa com o microservico de chamados, cada chamada vai com o token do usuario logado
@Service
public class ChamadoService {

    private final RestClient restClient;
    private final TokenService tokenService;
    private final UsuarioService usuarioService;

    public ChamadoService(@Value("${app.chamados.url}") String chamadosUrl, TokenService tokenService,
            UsuarioService usuarioService) {
        this.restClient = RestClient.builder().baseUrl(chamadosUrl).build();
        this.tokenService = tokenService;
        this.usuarioService = usuarioService;
    }

    public Chamado abrir(String username, String titulo, String descricao) {
        Usuario usuario = buscarUsuario(username);
        try {
            return restClient.post()
                    .uri("/api/chamados")
                    .header("Authorization", "Bearer " + tokenService.gerar(usuario))
                    .body(Map.of("titulo", titulo, "descricao", descricao))
                    .retrieve()
                    .body(Chamado.class);
        } catch (RestClientException e) {
            throw erro(e);
        }
    }

    // tecnico e admin veem todos, usuario comum so os dele
    public List<Chamado> listar(String username) {
        Usuario usuario = buscarUsuario(username);
        String uri = "/api/chamados/usuario/" + usuario.getId();
        if (podeResolver(usuario)) {
            uri = "/api/chamados";
        }
        try {
            return restClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + tokenService.gerar(usuario))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Chamado>>() {});
        } catch (RestClientException e) {
            throw erro(e);
        }
    }

    public Chamado buscar(String username, String id) {
        Usuario usuario = buscarUsuario(username);
        try {
            return restClient.get()
                    .uri("/api/chamados/{id}", id)
                    .header("Authorization", "Bearer " + tokenService.gerar(usuario))
                    .retrieve()
                    .body(Chamado.class);
        } catch (RestClientException e) {
            throw erro(e);
        }
    }

    public Chamado resolver(String username, String id, String solucao) {
        Usuario usuario = buscarUsuario(username);
        try {
            return restClient.put()
                    .uri("/api/chamados/{id}/resolver", id)
                    .header("Authorization", "Bearer " + tokenService.gerar(usuario))
                    .body(Map.of("solucao", solucao))
                    .retrieve()
                    .body(Chamado.class);
        } catch (RestClientException e) {
            throw erro(e);
        }
    }

    public boolean podeResolver(Usuario usuario) {
        return tokenService.permissoes(usuario.getRole()).contains("chamados:resolver");
    }

    public Usuario buscarUsuario(String username) {
        return usuarioService.buscarPorUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    // transforma o erro do microservico numa mensagem pra mostrar na tela
    private IllegalArgumentException erro(RestClientException e) {
        if (e instanceof HttpClientErrorException http) {
            if (http.getStatusCode() == HttpStatus.NOT_FOUND) {
                return new IllegalArgumentException("Chamado não encontrado.");
            }
            if (http.getStatusCode() == HttpStatus.FORBIDDEN) {
                return new IllegalArgumentException("Você não tem permissão pra isso.");
            }
            if (http.getStatusCode() == HttpStatus.CONFLICT) {
                return new IllegalArgumentException("Esse chamado já foi resolvido.");
            }
            if (http.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return new IllegalArgumentException("Preencha todos os campos.");
            }
        }
        return new IllegalArgumentException("Serviço de chamados indisponível, tente de novo mais tarde.");
    }
}
