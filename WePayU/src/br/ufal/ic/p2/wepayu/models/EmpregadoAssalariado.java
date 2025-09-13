package br.ufal.ic.p2.wepayu.models;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class EmpregadoAssalariado extends Empregado {

    public EmpregadoAssalariado() {}

    public EmpregadoAssalariado(String id, String nome, String endereco, String tipo, double salario) {
        super(id, nome, endereco, tipo, salario);
    }

    @Override
    public double calcularSalario(LocalDate dataPagamento) {
        String agenda = getAgendaPagamento();
        if (agenda.startsWith("semanal")) {
            double salarioSemanal = getSalario() * 12.0 / 52.0;
            String[] partes = agenda.split(" ");
            if (partes.length == 3) {
                return salarioSemanal * Integer.parseInt(partes[1]);
            }
            return salarioSemanal;
        }
        return getSalario();
    }

    @Override
    public double calcularDeducoes(LocalDate dataPagamento) {
        if (!isSindicalizado()) return 0.0;

        LocalDate primeiroDiaDoMes = dataPagamento.withDayOfMonth(1);
        double taxaSindicalDoPeriodo = getMembroSindicato().getTaxaSindical() * dataPagamento.lengthOfMonth();
        double taxasDeServico = getMembroSindicato().getTaxasDeServicoNoPeriodo(primeiroDiaDoMes, dataPagamento);
        return taxaSindicalDoPeriodo + taxasDeServico;
    }
}