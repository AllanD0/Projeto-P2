package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

public class CartaoDePonto {
    private LocalDate data;
    private LocalTime entrada;
    private LocalTime saida;

    public CartaoDePonto(LocalDate data, LocalTime entrada, LocalTime saida) {
        this.data = data;
        this.entrada = entrada;
        this.saida = saida;
    }

    public LocalDate getData() {
        return data;
    }

    public int getHorasTrabalhadas() {
        return (int) Duration.between(entrada, saida).toHours();
    }
}
