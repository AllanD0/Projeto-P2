package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import java.util.ArrayList;
import java.util.List;

public class EmpregadoComissionado extends EmpregadoAssalariado {
    private int taxaDeComissao;
    private List<ResultadoDaVenda> resultadosDeVenda;

    public EmpregadoComissionado(String nome, String endereco, String tipo, int salario, int comissao) throws EmpregadoNaoExisteException {
        super(nome, endereco, tipo, salario);
        this.taxaDeComissao = comissao;
        this.resultadosDeVenda = new ArrayList<>();
    }

    public void adicionarResultadoDeVenda(ResultadoDaVenda venda) {
        this.resultadosDeVenda.add(venda);
    }
}