package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class ResultadoDaVenda {
    private LocalDate dataVenda;
    private double valor;

    public ResultadoDaVenda() {}

    public ResultadoDaVenda(LocalDate data, double valor) {
        this.dataVenda = data;
        this.valor = valor;
    }

    public LocalDate getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDate dataVenda) {
        this.dataVenda = dataVenda;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}