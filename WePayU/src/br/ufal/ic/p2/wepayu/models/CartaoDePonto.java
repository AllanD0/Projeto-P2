package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class CartaoDePonto {
    private LocalDate data;
    private double horas;

    // Construtor vazio (obrigatório para XMLEncoder)
    public CartaoDePonto() {}

    public CartaoDePonto(LocalDate data, double horas) {
        this.data = data;
        this.horas = horas;
    }

    // Getters e Setters para todos os atributos
    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public double getHorasTrabalhadas() {
        return this.horas;
    }

    public double getHoras() {
        return horas;
    }

    public void setHoras(double horas) {
        this.horas = horas;
    }
}