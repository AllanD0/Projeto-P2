package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class ResultadoDaVenda {
    private LocalDate dataVenda;
    private Double valorVenda;

    public ResultadoDaVenda() {}

    public ResultadoDaVenda(LocalDate dataVenda, Double valorVenda) {
        this.dataVenda = dataVenda;
        this.valorVenda = valorVenda;
    }

    public LocalDate getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDate dataVenda) {
        this.dataVenda = dataVenda;
    }

    public Double getValorVenda() {
        return valorVenda;
    }

    public void setValorVenda(Double valorVenda) {
        this.valorVenda = valorVenda;
    }
}