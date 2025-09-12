package br.ufal.ic.p2.wepayu.models;

public class EmMaos implements MetodoPagamento {
    @Override
    public String getTipo() {
        return "emMaos";
    }
}