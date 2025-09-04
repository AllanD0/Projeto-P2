package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class ResultadoDaVenda {
    private LocalDate dataVenda;
    public Double valorVenda;

    public ResultadoDaVenda(LocalDate dataVenda, Double valorVenda) {
        this.dataVenda = dataVenda;
        this.valorVenda = valorVenda;
    }
    public LocalDate getDataVenda() {
        return dataVenda;
    }
    public double getvalorVenda() {
        return valorVenda;
    }

}
