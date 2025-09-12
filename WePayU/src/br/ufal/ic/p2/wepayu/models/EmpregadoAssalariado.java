package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class EmpregadoAssalariado extends Empregado {

    public EmpregadoAssalariado() {}

    public EmpregadoAssalariado(String id, String nome, String endereco, String tipo, double salario) throws EmpregadoNaoExisteException {
        super(id, nome, endereco, tipo, salario);
    }

    @Override
    public double calcularSalario(LocalDate data) {
        String agenda = this.getAgendaPagamento();
        if (agenda.equals("semanal 5")) {
            return this.getSalario() * 12 / 52;
        } else if (agenda.equals("semanal 2 5")) {
            return this.getSalario() * 12 / 26;
        } else {
            return this.getSalario();
        }
    }

    @Override
    public double calcularDeducoes(LocalDate data) {
        double deducoes = 0.0;
        if (this.isSindicalizado()) {
            LocalDate dataInicial;
            if (this.getAgendaPagamento().equals("semanal 5")) {
                dataInicial = data.minusDays(6);
            } else if (this.getAgendaPagamento().equals("semanal 2 5")) {
                dataInicial = data.minusDays(13);
            } else { // Agenda mensal
                dataInicial = data.withDayOfMonth(1);
            }
            long dias = ChronoUnit.DAYS.between(dataInicial, data) + 1;

            deducoes += this.getMembroSindicato().getTaxaSindical() * dias;
            deducoes += this.getMembroSindicato().getTaxasDeServicoNoPeriodo(dataInicial, data);
        }
        return deducoes;
    }
}