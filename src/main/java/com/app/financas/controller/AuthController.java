package com.app.financas.controller;

import com.app.financas.dao.UsuarioDAO;
import com.app.financas.model.Usuario;
import com.app.financas.service.RecuperacaoSenhaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

///
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final RecuperacaoSenhaService recuperacaoSenhaService;

    public AuthController(RecuperacaoSenhaService recuperacaoSenhaService) {
        this.recuperacaoSenhaService = recuperacaoSenhaService;
    }

    public record CadastroRequest(String nome, String email, String senha) {
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public void cadastrar(@RequestBody CadastroRequest dados) {
        if (dados.nome() == null || dados.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome");
        }

        if (dados.email() == null || dados.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o email");
        }

        if (!dados.email().contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um email válido");
        }

        if (dados.senha() == null || dados.senha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a senha");
        }

        if (dados.senha().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter pelo menos 6 caracteres");
        }
        try {
            String email = dados.email().trim().toLowerCase();
            if (usuarioDAO.buscarPorEmail(email) != null) throw new ResponseStatusException(HttpStatus.CONFLICT);
            String hash = encoder.encode(dados.senha());
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setSenhaHash(hash);
            usuario.setNome(dados.nome);
            usuarioDAO.salvar(usuario);

        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao acessar o banco de dados");
        }
    }

    public record LoginRequest(String email, String senha) {
    }

    public record RecuperarSenhaRequest(String email) {
    }

    public record TokenRequest(String token) {
    }

    public record RedefinirSenhaRequest(String token, String senha, String confirmarSenha) {
    }

    @PostMapping("/recuperar-senha")
    public Map<String, String> solicitarRecuperacao(@RequestBody RecuperarSenhaRequest dados) {
        if (dados == null || dados.email() == null || dados.email().isBlank() || !dados.email().contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um e-mail válido");
        }

        try {
            recuperacaoSenhaService.solicitar(dados.email().trim().toLowerCase(Locale.ROOT));
            return Map.of("mensagem", "Se o e-mail estiver cadastrado, enviaremos um link de recuperação.");
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "O envio de e-mail ainda não está configurado.");
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível solicitar a recuperação agora.");
        }
    }

    @PostMapping("/redefinir-senha/validar")
    public Map<String, Boolean> validarToken(@RequestBody TokenRequest dados) {
        if (dados == null || dados.token() == null) {
            return Map.of("valido", false);
        }
        try {
            return Map.of("valido", recuperacaoSenhaService.tokenValido(dados.token()));
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível validar o link agora.");
        }
    }

    @PostMapping("/redefinir-senha")
    public Map<String, String> redefinirSenha(@RequestBody RedefinirSenhaRequest dados) {
        if (dados == null || dados.token() == null || dados.token().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O link de recuperação é inválido ou expirou");
        }
        if (dados.senha() == null || dados.senha().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A senha deve ter pelo menos 6 caracteres");
        }
        if (!dados.senha().equals(dados.confirmarSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As senhas não coincidem");
        }

        try {
            if (!recuperacaoSenhaService.redefinir(dados.token(), encoder.encode(dados.senha()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O link de recuperação é inválido ou expirou. Solicite outro.");
            }
            return Map.of("mensagem", "Senha alterada. Você já pode entrar com a nova senha.");
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível alterar a senha agora.");
        }
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest dados, HttpSession sessao) {
        if (dados.email() == null || dados.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o email");
        }

        if (dados.senha() == null || dados.senha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a senha");
        }

        try {
            Usuario usuario = usuarioDAO.buscarPorEmail(dados.email().trim().toLowerCase());
            if (usuario == null || !encoder.matches(dados.senha(), usuario.getSenhaHash())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
            }
            sessao.setAttribute("usuarioId", usuario.getId());
            return Map.of("nome", usuario.getNome());

        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao acessar o banco de dados");
        }
    }

    @PostMapping("/logout")
    public void logout(HttpSession sessao) {
        sessao.invalidate();
    }
}
