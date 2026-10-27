package com.app.financas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Ganho {
    private int id;
    private BigDecimal valor;
    private String plataforma;
    private LocalDate data;
    public BigDecimal getValor() {
        return valor;
    }
    public String getPlataforma() {
        return plataforma;
    }
    @JsonCreator
    public Ganho() {

    }
    public Ganho(int id, BigDecimal valor, String plataforma, LocalDate data) {

        this.id = id;
        this.valor = valor;
        this.plataforma = plataforma;
        this.data = data;
    }
    public LocalDate getData() {
        return data;
    }
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }
    public void setData(LocalDate data) {
        this.data = data;
    }
    public int getId() {    
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
