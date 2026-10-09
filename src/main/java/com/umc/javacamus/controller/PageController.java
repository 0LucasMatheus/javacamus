package com.umc.javacamus.controller;

import com.umc.javacamus.entity.Role;
import com.umc.javacamus.entity.Usuario;
import com.umc.javacamus.service.UsuarioService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// controller das paginas do sistema, so monta tela, quem faz a regra de negocio e o UsuarioService
@Controller
public class PageController {

    private final UsuarioService usuarioService;

    public PageController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "acesso-negado";
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    // so admin e moderator conseguem entrar aqui, o SecurityConfig que barra o resto
    @GetMapping("/moderator/painel")
    public String painelModerator() {
        return "moderator/painel";
    }

    // area liberada pra qualquer usuario logado
    @GetMapping("/usuario/painel")
    public String painelUsuario() {
        return "usuario/painel";
    }

    @GetMapping("/admin/usuarios")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios/lista";
    }

    // usuario novo e vazio, so pra preencher o form
    @GetMapping("/admin/usuarios/novo")
    public String novoUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Role.values());
        return "admin/usuarios/form";
    }

    @GetMapping("/admin/usuarios/editar/{id}")
    public String editarUsuarioForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.buscarPorId(id).orElse(null);

        if (usuario == null) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
            return "redirect:/admin/usuarios";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Role.values());
        return "admin/usuarios/form";
    }

    // se der erro de validacao volta pro form com a mensagem, se der certo volta pra lista
    @PostMapping("/admin/usuarios/salvar")
    public String salvarUsuario(@ModelAttribute Usuario usuario,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.criarUsuario(usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário criado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/admin/usuarios/novo";
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/editar/{id}")
    public String atualizarUsuario(@PathVariable String id,
            @ModelAttribute Usuario usuario,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.atualizarUsuario(id, usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/admin/usuarios/editar/" + id;
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/deletar/{id}")
    public String deletarUsuario(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        usuarioService.deletarUsuario(id);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário removido com sucesso!");
        return "redirect:/admin/usuarios";
    }
}
