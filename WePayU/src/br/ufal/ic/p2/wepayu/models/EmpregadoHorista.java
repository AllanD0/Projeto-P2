package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.temporal.ChronoUnit;

public class EmpregadoHorista extends Empregado {
    private List<CartaoDePonto> cartaoDePontos;

    public EmpregadoHorista() {
        this.cartaoDePontos = new ArrayList<>();
    }

    public EmpregadoHorista(String id, String nome, String endereco, String tipo, double salario) throws EmpregadoNaoExisteException {
        super(id, nome, endereco, tipo, salario);
        this.cartaoDePontos = new ArrayList<>();
    }

    public List<CartaoDePonto> getCartaoDePontos() {
        return cartaoDePontos;
    }

    public void setCartaoDePontos(List<CartaoDePonto> cartaoDePontos) {
        this.cartaoDePontos = cartaoDePontos;
    }

    public void adicionarCartaoDePonto(CartaoDePonto cartao) {
        if (this.cartaoDePontos == null) this.cartaoDePontos = new ArrayList<>();
        this.cartaoDePontos.add(cartao);
    }

    public double getHorasNormaisTrabalhadas(LocalDate dataInicial, LocalDate dataFinal) {
        double horasNormais = 0;
        if (cartaoDePontos != null) {
            for (CartaoDePonto c : cartaoDePontos) {
                // A condição de data deve ser inclusiva na data inicial e exclusiva na data final
                if (!c.getData().isBefore(dataInicial) && c.getData().isBefore(dataFinal)) {
                    horasNormais += Math.min(c.getHorasTrabalhadas(), 8);
                }
            }
        }
        return horasNormais;
    }

    public double getHorasExtrasTrabalhadas(LocalDate dataInicial, LocalDate dataFinal) {
        double horasExtras = 0;
        if (cartaoDePontos != null) {
            for (CartaoDePonto c : cartaoDePontos) {
                // A condição de data deve ser inclusiva na data inicial e exclusiva na data final
                if (!c.getData().isBefore(dataInicial) && c.getData().isBefore(dataFinal)) {
                    horasExtras += Math.max(0, c.getHorasTrabalhadas() - 8);
                }
            }
        }
        return horasExtras;
    }

    @Override
    public double calcularSalario(LocalDate data) {
        LocalDate dataInicial = this.getDataUltimoPagamento() == null ? data.minusDays(6) : this.getDataUltimoPagamento();
        LocalDate dataFinal = data;

        double horasNormais = getHorasNormaisTrabalhadas(dataInicial, dataFinal);
        double horasExtras = getHorasExtrasTrabalhadas(dataInicial, dataFinal);

        return (horasNormais * this.getSalario()) + (horasExtras * this.getSalario() * 1.5);
    }

    @Override
    public double calcularDeducoes(LocalDate data) {
        double deducoes = 0.0;
        if (this.isSindicalizado()) {
            LocalDate dataInicial = this.getDataUltimoPagamento() == null ? data.minusDays(6) : this.getDataUltimoPagamento();
            LocalDate dataFinal = data;
            long dias = dataFinal.toEpochDay() - dataInicial.toEpochDay() + 1;

            deducoes += this.getMembroSindicato().getTaxaSindical() * dias;
            deducoes += this.getMembroSindicato().getTaxasDeServicoNoPeriodo(dataInicial, dataFinal);
        }
        return deducoes;
    }
}