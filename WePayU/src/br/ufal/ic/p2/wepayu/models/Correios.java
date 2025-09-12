package br.ufal.ic.p2.wepayu.models;

public class Correios implements MetodoPagamento {
    public Correios() {}
    @Override
    public String getTipo() {
        return "correios";
    }
}