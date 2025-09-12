package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;

public interface MetodoPagamento extends Serializable {
    String getTipo();
}