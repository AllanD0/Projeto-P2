package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.controller.SistemaFolha;
import java.time.format.DateTimeParseException;
import java.io.IOException;

public class Facade {
    private SistemaFolha sistemaFolha;

    public Facade() {
        this.sistemaFolha = new SistemaFolha();
    }

    public void zerarSistema() {
        this.sistemaFolha.zerarSistema();
    }

    public void encerrarSistema() {
        this.sistemaFolha.encerrarSistema();
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        if (salario == null || salario.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
        try {
            double salarioNumerico = Double.parseDouble(salario.replace(',', '.'));
            return this.sistemaFolha.criarEmpregado(nome, endereco, tipo, salarioNumerico);
        } catch (NumberFormatException e) {
            throw new Exception("Salario deve ser numerico.");
        }
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        if (comissao == null || comissao.isEmpty()) throw new Exception("Comissao nao pode ser nula.");
        try {
            double salarioNumerica = Double.parseDouble(salario.replace(',', '.'));
            double comissaoNumerica = Double.parseDouble(comissao.replace(',', '.'));
            return this.sistemaFolha.criarEmpregado(nome, endereco, tipo, salarioNumerica, comissaoNumerica);
        } catch (NumberFormatException e) {
            throw new Exception("Comissao deve ser numerica.");
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
        this.sistemaFolha.alteraEmpregado(emp, atributo, valor);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String valorExtra) throws Exception {
        this.sistemaFolha.alteraEmpregado(emp, atributo, valor, valorExtra);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
        if (!"sindicalizado".equalsIgnoreCase(atributo)) throw new Exception("Atributo nao existe.");
        if (!"true".equalsIgnoreCase(valor)) throw new Exception("Valor deve ser true.");
        if (idSindicato == null || idSindicato.isEmpty()) throw new Exception("Identificacao do sindicato nao pode ser nula.");
        if (taxaSindical == null || taxaSindical.isEmpty()) throw new Exception("Taxa sindical nao pode ser nula.");
        try {
            double taxa = Double.parseDouble(taxaSindical.replace(',', '.'));
            if (taxa < 0) throw new Exception("Taxa sindical deve ser nao-negativa.");
            this.sistemaFolha.alteraEmpregado(emp, atributo, true, idSindicato, taxa);
        } catch (NumberFormatException e) {
            throw new Exception("Taxa sindical deve ser numerica.");
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws Exception {
        if (!"metodoPagamento".equalsIgnoreCase(atributo)) throw new Exception("Atributo nao existe.");
        if (!"banco".equalsIgnoreCase(valor1)) throw new Exception("Metodo de pagamento invalido.");
        if (banco == null || banco.isEmpty()) throw new Exception("Banco nao pode ser nulo.");
        if (agencia == null || agencia.isEmpty()) throw new Exception("Agencia nao pode ser nulo.");
        if (contaCorrente == null || contaCorrente.isEmpty()) throw new Exception("Conta corrente nao pode ser nulo.");
        this.sistemaFolha.alteraEmpregado(emp, atributo, valor1, banco, agencia, contaCorrente);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws Exception {
        return this.sistemaFolha.getAtributoEmpregado(emp, atributo);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        return this.sistemaFolha.getEmpregadoPorNome(nome, indice);
    }

    public void removerEmpregado(String emp) throws Exception {
        this.sistemaFolha.removerEmpregado(emp);
    }

    public void lancaCartao(String emp, String data, String horas) throws Exception {
        try {
            this.sistemaFolha.lancaCartao(emp, data, horas);
        } catch (DateTimeParseException e) {
            throw new Exception("Data invalida.");
        }
    }

    public void lancaVenda(String emp, String data, String valor) throws Exception {
        try {
            this.sistemaFolha.lancaVenda(emp, data, valor);
        } catch (DateTimeParseException e) {
            throw new Exception("Data invalida.");
        }
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        if (membro == null || membro.isEmpty()) throw new Exception("Identificacao do membro nao pode ser nula.");
        try {
            double valorNumerico = Double.parseDouble(valor.replace(',', '.'));
            this.sistemaFolha.lancaTaxaServico(membro, data, valorNumerico);
        } catch (DateTimeParseException e) {
            throw new Exception("Data invalida.");
        } catch (NumberFormatException e) {
            throw new Exception("Valor deve ser numerico.");
        }
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return this.sistemaFolha.getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return this.sistemaFolha.getHorasExtrasTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return this.sistemaFolha.getVendasRealizadas(emp, dataInicial, dataFinal);
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
        return this.sistemaFolha.getTaxasServico(emp, dataInicial, dataFinal);
    }

    public String totalFolha(String data) throws Exception {
        return this.sistemaFolha.totalFolha(data);
    }

    public void rodaFolha(String data, String saida) throws Exception {
        this.sistemaFolha.rodaFolha(data, saida);
    }

    public void undo() throws Exception {
        this.sistemaFolha.undo();
    }

    public void redo() throws Exception {
        this.sistemaFolha.redo();
    }

    public int getNumeroDeEmpregados() {
        return this.sistemaFolha.getNumeroDeEmpregados();
    }
}