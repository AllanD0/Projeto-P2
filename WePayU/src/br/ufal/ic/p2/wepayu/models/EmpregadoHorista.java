package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

import java.util.ArrayList;
import java.util.List;

public class EmpregadoHorista extends Empregado {

  List<CartaoDePonto> cartaoDePontos = new ArrayList<>();
    public EmpregadoHorista(String nome, String endereco, String tipo, int salario) throws EmpregadoNaoExisteException {
        super(nome, endereco, tipo, salario);

    }
    public void adicionarCartaoDePonto(CartaoDePonto cartao) {
        this.cartaoDePontos.add(cartao);
    }
    public double calcularSalario() {
        double totalHorasTrabalhadas = 0;
        double salarioTotal = 0;

        for (CartaoDePonto cartao : cartaoDePontos) {
            totalHorasTrabalhadas += cartao.getHorasTrabalhadas();
        }

        for (CartaoDePonto cartao : cartaoDePontos) {
            double horas = cartao.getHorasTrabalhadas();
            if (horas > 8) {
                salarioTotal += (8 * this.getSalario()) + ((horas - 8) * this.getSalario() * 1.5);
            }
            else  {
                salarioTotal += (horas * this.getSalario());
            }
        }
        return salarioTotal;
    }


}
