package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpregadoHorista extends Empregado {
    private List<CartaoDePonto> cartaoDePontos = new ArrayList<>();

    public EmpregadoHorista() {}

    public EmpregadoHorista(String id, String nome, String endereco, String tipo, double salario) {
        super(id, nome, endereco, tipo, salario);
    }

    public double getHorasNormaisTrabalhadas(LocalDate dataInicial, LocalDate dataFinal) {
        double horasNormais = 0;
        for (CartaoDePonto c : cartaoDePontos) {

            if (!c.getData().isBefore(dataInicial) && c.getData().isBefore(dataFinal)) {
                horasNormais += Math.min(c.getHoras(), 8);
            }
        }
        return horasNormais;
    }

    public double getHorasExtrasTrabalhadas(LocalDate dataInicial, LocalDate dataFinal) {
        double horasExtras = 0;
        for (CartaoDePonto c : cartaoDePontos) {

            if (!c.getData().isBefore(dataInicial) && c.getData().isBefore(dataFinal)) {
                horasExtras += Math.max(0, c.getHoras() - 8);
            }
        }
        return horasExtras;
    }

    @Override
    public double calcularSalario(LocalDate dataPagamento) {
        LocalDate dataInicial = dataPagamento.minusDays(6);
        double salarioBruto = (getHorasNormaisTrabalhadas(dataInicial, dataPagamento) * getSalario()) +
                (getHorasExtrasTrabalhadas(dataInicial, dataPagamento) * getSalario() * 1.5);
        return salarioBruto;
    }

    @Override
    public double calcularDeducoes(LocalDate dataPagamento) {
        if (!isSindicalizado()) return 0.0;
        LocalDate dataInicial = dataPagamento.minusDays(6);
        double taxaSindicalDoPeriodo = getMembroSindicato().getTaxaSindical() * 7;
        double taxasDeServico = getMembroSindicato().getTaxasDeServicoNoPeriodo(dataInicial, dataPagamento);
        return taxaSindicalDoPeriodo + taxasDeServico;
    }

    public void adicionarCartaoDePonto(CartaoDePonto cartao) { this.cartaoDePontos.add(cartao); }
    public List<CartaoDePonto> getCartaoDePontos() { return cartaoDePontos; }
    public void setCartaoDePontos(List<CartaoDePonto> cartaoDePontos) { this.cartaoDePontos = cartaoDePontos; }
}