package com.umc.javacamus.controller;

import com.umc.javacamus.dto.Chamado;
import com.umc.javacamus.entity.Usuario;
import com.umc.javacamus.service.ChamadoService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// telas dos chamados, quem guarda e valida tudo e o microservico
@Controller
public class ChamadoController {

    private final ChamadoService chamadoService;

    public ChamadoController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @GetMapping("/chamados")
    public String listar(@RequestParam(required = false) String status, Principal principal, Model model) {
        Usuario usuario = chamadoService.buscarUsuario(principal.getName());
        List<Chamado> chamados = new ArrayList<>();
        try {
            chamados = chamadoService.listar(principal.getName());
        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
        }

        // filtro de status feito aqui msm
        List<Chamado> filtrados = new ArrayList<>();
        for (Chamado chamado : chamados) {
            if (status == null || status.isBlank() || status.equals(chamado.getStatus())) {
                filtrados.add(chamado);
            }
        }
        // mais novo primeiro
        filtrados.sort(Comparator.comparing(Chamado::getDataAbertura).reversed());

        model.addAttribute("chamados", filtrados);
        model.addAttribute("status", status);
        model.addAttribute("podeResolver", chamadoService.podeResolver(usuario));
        return "chamados/lista";
    }

    @GetMapping("/chamados/novo")
    public String novo() {
        return "chamados/form";
    }

    @PostMapping("/chamados")
    public String abrir(@RequestParam String titulo, @RequestParam String descricao, Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            Chamado chamado = chamadoService.abrir(principal.getName(), titulo, descricao);
            redirectAttributes.addFlashAttribute("sucesso", "Chamado aberto com sucesso!");
            return "redirect:/chamados/" + chamado.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            redirectAttributes.addFlashAttribute("titulo", titulo);
            redirectAttributes.addFlashAttribute("descricao", descricao);
            return "redirect:/chamados/novo";
        }
    }

    @GetMapping("/chamados/{id}")
    public String detalhe(@PathVariable String id, Principal principal, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = chamadoService.buscarUsuario(principal.getName());
            model.addAttribute("chamado", chamadoService.buscar(principal.getName(), id));
            model.addAttribute("podeResolver", chamadoService.podeResolver(usuario));
            return "chamados/detalhe";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/chamados";
        }
    }

    @PostMapping("/chamados/{id}/resolver")
    public String resolver(@PathVariable String id, @RequestParam String solucao, Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            chamadoService.resolver(principal.getName(), id, solucao);
            redirectAttributes.addFlashAttribute("sucesso", "Chamado resolvido!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/chamados/" + id;
    }
}
