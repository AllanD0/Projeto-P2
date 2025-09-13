package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpregadoComissionado extends Empregado {
    private double taxaDeComissao;
    private List<ResultadoDaVenda> resultadosDeVenda = new ArrayList<>();

    public EmpregadoComissionado() {}

    public EmpregadoComissionado(String id, String nome, String endereco, String tipo, double salario, double comissao) {
        super(id, nome, endereco, tipo, salario);
        this.taxaDeComissao = comissao;
    }

    @Override
    public double calcularSalario(LocalDate dataPagamento) {
        LocalDate dataInicial = dataPagamento.minusDays(13);
        double salarioBase = (getSalario() * 12.0 / 52.0) * 2.0;
        double totalVendas = getVendasRealizadas(dataInicial, dataPagamento);
        double valorComissoes = totalVendas * getTaxaDeComissao();
        return salarioBase + valorComissoes;
    }

    @Override
    public double calcularDeducoes(LocalDate dataPagamento) {
        if (!isSindicalizado()) {
            return 0.0;
        }
        LocalDate dataInicial = dataPagamento.minusDays(13);
        double taxaSindicalDoPeriodo = getMembroSindicato().getTaxaSindical() * 14;
        double taxasDeServico = getMembroSindicato().getTaxasDeServicoNoPeriodo(dataInicial, dataPagamento);
        return taxaSindicalDoPeriodo + taxasDeServico;
    }

    public double getVendasRealizadas(LocalDate dataInicial, LocalDate dataFinal) {
        double totalVendas = 0.0;
        if (this.resultadosDeVenda != null) {
            for (ResultadoDaVenda venda : this.resultadosDeVenda) {
                LocalDate dataDaVenda = venda.getDataVenda();

                if (dataDaVenda != null && !dataDaVenda.isBefore(dataInicial) && dataDaVenda.isBefore(dataFinal)) {
                    totalVendas += venda.getValor();
                }
            }
        }
        return totalVendas;
    }

    public void adicionarResultadoDeVenda(ResultadoDaVenda venda) {
        if (this.resultadosDeVenda == null) {
            this.resultadosDeVenda = new ArrayList<>();
        }
        this.resultadosDeVenda.add(venda);
    }

    public double getTaxaDeComissao() {
        return taxaDeComissao;
    }

    public void setTaxaDeComissao(double taxaDeComissao) {
        this.taxaDeComissao = taxaDeComissao;
    }

    public List<ResultadoDaVenda> getResultadosDeVenda() {
        return resultadosDeVenda;
    }

    public void setResultadosDeVenda(List<ResultadoDaVenda> resultadosDeVenda) {
        this.resultadosDeVenda = resultadosDeVenda;
    }
}