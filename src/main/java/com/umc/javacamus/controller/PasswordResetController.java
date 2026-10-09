package com.umc.javacamus.controller;

import com.umc.javacamus.service.PasswordResetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// fluxo de recuperacao de senha, usa a sessao pra lembrar qual email ta recuperando entre as telas
@Controller
public class PasswordResetController {

    private static final String SESSION_EMAIL = "resetEmail";

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/recuperar-senha")
    public String formSolicitar() {
        return "recuperar-senha";
    }

    // gera o codigo, guarda o email na sessao e manda pra tela de verificar
    @PostMapping("/recuperar-senha")
    public String solicitar(@RequestParam String email, HttpSession session,
            RedirectAttributes redirectAttributes) {
        passwordResetService.solicitarCodigo(email);
        session.setAttribute(SESSION_EMAIL, email);
        redirectAttributes.addFlashAttribute("info",
                "código de verificação enviado.");
        return "redirect:/recuperar-senha/verificar";
    }

    // se o usuario cair aqui direto sem ter passado pela tela anterior, manda ele pra la
    @GetMapping("/recuperar-senha/verificar")
    public String formVerificar(HttpSession session, Model model) {
        String email = (String) session.getAttribute(SESSION_EMAIL);
        if (email == null) {
            return "redirect:/recuperar-senha";
        }
        return "verificar-codigo";
    }

    @PostMapping("/recuperar-senha/verificar")
    public String verificar(@RequestParam String codigo, @RequestParam String novaSenha,
            @RequestParam String confirmarSenha, HttpSession session,
            RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute(SESSION_EMAIL);
        if (email == null) {
            return "redirect:/recuperar-senha";
        }

        if (!novaSenha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute("erro", "As senhas não coincidem.");
            return "redirect:/recuperar-senha/verificar";
        }

        try {
            passwordResetService.redefinirSenha(email, codigo, novaSenha);
            session.removeAttribute(SESSION_EMAIL);
            redirectAttributes.addFlashAttribute("sucesso", "Senha redefinida com sucesso! Faça login com a nova senha.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/recuperar-senha/verificar";
        }
    }
}
