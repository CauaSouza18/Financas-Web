package com.app.financas.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Ganho {
    private int id;
    private double valor;
    private String plataforma;
    private LocalDate data;
    public double getValor() {
        return valor;
    }
    public String getPlataforma() {
        return plataforma;
    }
    @JsonCreator
    public Ganho() {

    }
    public Ganho(int id, double valor, String plataforma, LocalDate data) {

        this.id = id;
        this.valor = valor;
        this.plataforma = plataforma;
        this.data = data;
    }
    public LocalDate getData() {
        return data;
    }
    public void setValor(double valor) {
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
