package com.app.financas.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import com.app.financas.dao.DespesaDAO;
import com.app.financas.model.Despesa;
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

@RestController
@RequestMapping("/api/despesas")
public class DespesaController {

    private final DespesaDAO despesaDAO = new DespesaDAO();

    @GetMapping
    public ArrayList<Despesa> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            HttpSession sessao) {
        if (inicio.isAfter(fim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe inicio e fim válidos no formato YYYY-MM-DD.");
        }
        try {
            return despesaDAO.listarPorPeriodo(inicio, fim, usuarioDaSessao(sessao));
        } catch (SQLException e) {
            throw erroBanco(e);
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void criar(@RequestBody Despesa despesa, HttpSession sessao) {
        validar(despesa);
        try {
            despesaDAO.salvar(despesa, usuarioDaSessao(sessao));
        } catch (SQLException e) {
            throw erroBanco(e);
        }
    }

    @PutMapping("/{id}")
    public void atualizar(@PathVariable int id, @RequestBody Despesa despesa, HttpSession sessao) {
        validar(despesa);
        despesa.setId(id);
        try {
            if (!despesaDAO.atualizar(despesa, usuarioDaSessao(sessao))) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não encontrada.");
            }
        } catch (SQLException e) {
            throw erroBanco(e);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable int id, HttpSession sessao) {
        try {
            if (!despesaDAO.deletar(id, usuarioDaSessao(sessao))) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não encontrada.");
            }
        } catch (SQLException e) {
            throw erroBanco(e);
        }
    }

    private void validar(Despesa despesa) {
        if (despesa.getValor() == null || despesa.getValor().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um valor maior que zero.");
        }
        if (despesa.getValor().scale() > 2 || despesa.getValor().precision() - despesa.getValor().scale() > 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O valor deve ter até 8 dígitos inteiros e 2 casas decimais.");
        }
        if (despesa.getCategoria() == null || despesa.getCategoria().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a categoria.");
        }
        if (despesa.getCategoria().trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A categoria deve ter até 100 caracteres.");
        }
        if (despesa.getDescricao() != null && despesa.getDescricao().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A descrição deve ter até 255 caracteres.");
        }
        if (despesa.getData() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a data.");
        }
        despesa.setCategoria(despesa.getCategoria().trim());
        despesa.setDescricao(despesa.getDescricao() == null ? "" : despesa.getDescricao().trim());
    }

    private int usuarioDaSessao(HttpSession sessao) {
        Object atributo = sessao.getAttribute("usuarioId");
        if (atributo instanceof Integer usuarioId) {
            return usuarioId;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida.");
    }

    private ResponseStatusException erroBanco(SQLException e) {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao acessar as despesas no banco de dados.", e);
    }
}
