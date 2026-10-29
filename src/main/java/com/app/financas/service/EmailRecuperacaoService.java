package com.app.financas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailRecuperacaoService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String usuarioSmtp;

    @Value("${spring.mail.password:}")
    private String senhaSmtp;

    @Value("${app.mail.from:}")
    private String remetente;

    public EmailRecuperacaoService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean configurado() {
        return usuarioSmtp != null && !usuarioSmtp.isBlank()
                && senhaSmtp != null && !senhaSmtp.isBlank()
                && remetente != null && !remetente.isBlank();
    }

    public void enviarLink(String email, String link) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(email);
        mensagem.setSubject("Redefinição de senha | Finanças");
        mensagem.setText("Recebemos um pedido para redefinir a senha da sua conta Finanças.\n\n"
                + "Use este link para escolher uma nova senha (válido por 30 minutos):\n"
                + link + "\n\n"
                + "Se você não pediu a redefinição, ignore este e-mail. Sua senha atual continuará funcionando.");
        mailSender.send(mensagem);
    }
}
