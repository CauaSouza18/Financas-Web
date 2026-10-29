package com.app.financas.service;

import com.app.financas.dao.RecuperacaoSenhaDAO;
import com.app.financas.dao.UsuarioDAO;
import com.app.financas.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.sql.SQLException;

@Service
public class RecuperacaoSenhaService {
    private static final Logger logger = LoggerFactory.getLogger(RecuperacaoSenhaService.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_TTL_MINUTOS = 30;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RecuperacaoSenhaDAO recuperacaoSenhaDAO = new RecuperacaoSenhaDAO();
    private final EmailRecuperacaoService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public RecuperacaoSenhaService(EmailRecuperacaoService emailService) {
        this.emailService = emailService;
    }

    public void solicitar(String email) throws SQLException {
        if (!emailService.configurado()) {
            throw new IllegalStateException("Configuração SMTP ausente");
        }

        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        if (usuario == null || !recuperacaoSenhaDAO.podeSolicitar(usuario.getId())) {
            return;
        }

        String token = gerarToken();
        String tokenHash = hashToken(token);
        recuperacaoSenhaDAO.salvarToken(usuario.getId(), tokenHash,
                LocalDateTime.now(ZoneOffset.UTC).plusMinutes(TOKEN_TTL_MINUTOS));

        String base = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
        String link = base + "/html/redefinir-senha.html#token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);

        try {
            emailService.enviarLink(email, link);
        } catch (MailException e) {
            try {
                recuperacaoSenhaDAO.expirarToken(tokenHash);
            } catch (SQLException erroBanco) {
                logger.error("Não foi possível invalidar um token após falha no envio de e-mail.", erroBanco);
            }
            logger.error("Falha ao enviar e-mail de recuperação para usuarioId={}", usuario.getId(), e);
        }
    }

    public boolean tokenValido(String token) throws SQLException {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            return false;
        }
        return recuperacaoSenhaDAO.tokenValido(hashToken(token));
    }

    public boolean redefinir(String token, String senhaHash) throws SQLException {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            return false;
        }
        return recuperacaoSenhaDAO.redefinirSenha(hashToken(token), senhaHash);
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não está disponível", e);
        }
    }
}
