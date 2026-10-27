package com.app.financas.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.app.financas.dao.GanhoDAO;
import com.app.financas.model.Ganho;

@RestController
@RequestMapping("/api/ganhos")
public class GanhoController {

    private final GanhoDAO ganhoDAO = new GanhoDAO();

    @GetMapping
    public ArrayList<Ganho> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            HttpSession sessao) {
        int usuarioId = usuarioDaSessao(sessao);
        try {
            if (inicio != null || fim != null) {
                if (inicio == null || fim == null || inicio.isAfter(fim)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Informe inicio e fim válidos no formato YYYY-MM-DD.");
                }
                return ganhoDAO.listarPorPeriodo(inicio, fim, usuarioId);
            }
            return ganhoDAO.listGanhos(usuarioId);
        } catch (SQLException e) {
            throw erroBanco(e);
        }
    }

    @GetMapping("/por-plataforma")
    public Map<String, Double> totaisPorPlataforma(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            HttpSession sessao) {
        if (inicio.isAfter(fim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe inicio e fim válidos no formato YYYY-MM-DD.");
        }
        try {
            return ganhoDAO.totaisPorPlataforma(usuarioDaSessao(sessao), inicio, fim);
        } catch (SQLException e) {
            throw erroBanco(e);
        }
    }

    @GetMapping("/total/{plataforma}")
    public double totalPorPlataforma(@PathVariable String plataforma, HttpSession sessao) {
        try {
            return ganhoDAO.totalPorPlataforma(plataforma, usuarioDaSessao(sessao));
        } catch (SQLException e) {
            throw erroBanco(e);
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void criar(@RequestBody Ganho ganho, HttpSession sessao) {
        try {
            ganhoDAO.salvar(ganho, usuarioDaSessao(sessao));
        } catch (SQLException e) {
            throw erroBanco(e);
        }
    }

    @PutMapping("/{id}")
    public void atualizar(@PathVariable int id, @RequestBody Ganho ganho, HttpSession sessao) {
        ganho.setId(id);
        try {
            if (!ganhoDAO.atualizar(ganho, usuarioDaSessao(sessao))) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ganho não encontrado.");
            }
        } catch (SQLException e) {
            throw erroBanco(e);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable int id, HttpSession sessao) {
        try {
            if (!ganhoDAO.deletar(id, usuarioDaSessao(sessao))) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ganho não encontrado.");
            }
        } catch (SQLException e) {
            throw erroBanco(e);
        }
    }

    private ResponseStatusException erroBanco(SQLException e) {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao acessar os ganhos no banco de dados.", e);
    }

    private int usuarioDaSessao(HttpSession sessao) {
        Object atributo = sessao.getAttribute("usuarioId");
        if (atributo instanceof Integer usuarioId) {
            return usuarioId;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida.");
    }
}
