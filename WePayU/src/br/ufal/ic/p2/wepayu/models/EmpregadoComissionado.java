package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.temporal.ChronoUnit;

public class EmpregadoComissionado extends Empregado {
    private double taxaDeComissao;
    private List<ResultadoDaVenda> resultadosDeVenda;

    public EmpregadoComissionado() {
        this.resultadosDeVenda = new ArrayList<>();
    }

    public EmpregadoComissionado(String id, String nome, String endereco, String tipo, double salario, double comissao) throws EmpregadoNaoExisteException {
        super(id, nome, endereco, tipo, salario);
        this.taxaDeComissao = comissao;
        this.resultadosDeVenda = new ArrayList<>();
    }

    @Override
    public double calcularSalario(LocalDate data) {
        LocalDate dataInicial = this.getDataUltimoPagamento() == null ? data.minusDays(14) : this.getDataUltimoPagamento();
        LocalDate dataFinal = data;
        double salarioBase = this.getSalario() * 12 / 52;
        double comissoes = this.getVendasRealizadas(dataInicial, dataFinal);
        return salarioBase + (comissoes * this.getTaxaDeComissao());
    }

    @Override
    public double calcularDeducoes(LocalDate data) {
        double deducoes = 0.0;
        if (this.isSindicalizado()) {
            LocalDate dataInicial = this.getDataUltimoPagamento() == null ? data.minusDays(14) : this.getDataUltimoPagamento();
            LocalDate dataFinal = data;
            long dias = ChronoUnit.DAYS.between(dataInicial, dataFinal) + 1;

            deducoes += this.getMembroSindicato().getTaxaSindical() * dias;
            deducoes += this.getMembroSindicato().getTaxasDeServicoNoPeriodo(dataInicial, dataFinal);
        }
        return deducoes;
    }

    public double getVendasRealizadas(LocalDate dataInicial, LocalDate dataFinal) {
        double totalVendas = 0;
        if (resultadosDeVenda != null) {
            for (ResultadoDaVenda venda : resultadosDeVenda) {
                if (venda.getDataVenda() != null && !venda.getDataVenda().isBefore(dataInicial) && !venda.getDataVenda().isAfter(dataFinal)) {
                    totalVendas += venda.getValorVenda();
                }
            }
        }
        return totalVendas;
    }

    public List<ResultadoDaVenda> getResultadosDeVenda() { return resultadosDeVenda; }
    public void setResultadosDeVenda(List<ResultadoDaVenda> resultadosDeVenda) { this.resultadosDeVenda = resultadosDeVenda; }
    public double getTaxaDeComissao() { return taxaDeComissao; }
    public void setTaxaDeComissao(double taxaDeComissao) { this.taxaDeComissao = taxaDeComissao; }
    public void adicionarResultadoDeVenda(ResultadoDaVenda venda) {
        if (this.resultadosDeVenda == null) this.resultadosDeVenda = new ArrayList<>();
        this.resultadosDeVenda.add(venda);
    }
}