package com.umc.javacamus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// manda email usando o smtp configurado no application.properties, vem do spring boot starter mail
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoRecuperacao(String destinatario, String codigo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Javacamus - Código de recuperação de senha");
        mensagem.setText(
                "Seu código de recuperação de senha é: \n\n " + codigo + "\n\n expira em 10 minutos."
        );
        mailSender.send(mensagem);
    }
}
