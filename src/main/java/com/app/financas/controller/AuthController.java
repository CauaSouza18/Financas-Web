package com.app.financas.controller;

import com.app.financas.dao.UsuarioDAO;
import com.app.financas.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import java.sql.SQLException;

///
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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

        if (dados.senha() == null || dados.senha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a senha");
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
