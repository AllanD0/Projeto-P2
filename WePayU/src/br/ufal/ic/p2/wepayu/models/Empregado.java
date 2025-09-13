package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public abstract class Empregado {
    private String id;
    private String nome;
    private String endereco;
    private String tipo;
    private double salario;
    private MembroSindicato membroSindicato;
    private MetodoPagamento metodoPagamento;
    private LocalDate dataUltimoPagamento;
    private String agendaPagamento;

    public Empregado() {}

    public Empregado(String id, String nome, String endereco, String tipo, double salario) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
        this.membroSindicato = null;
        this.metodoPagamento = new EmMaos();
        this.dataUltimoPagamento = null;


        if (tipo.equalsIgnoreCase("horista")) {
            this.agendaPagamento = "semanal 5";
        } else if (tipo.equalsIgnoreCase("assalariado")) {
            this.agendaPagamento = "mensal $";
        } else if (tipo.equalsIgnoreCase("comissionado")) {
            this.agendaPagamento = "semanal 2 5";
        }
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getTipo() { return tipo; }
    public double getSalario() { return salario; }
    public MembroSindicato getMembroSindicato() { return membroSindicato; }
    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public LocalDate getDataUltimoPagamento() { return dataUltimoPagamento; }
    public String getAgendaPagamento() { return agendaPagamento; }

    public boolean isSindicalizado() {
        return this.membroSindicato != null;
    }

    public void setId(String id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSalario(double salario) { this.salario = salario; }
    public void setMembroSindicato(MembroSindicato membroSindicato) { this.membroSindicato = membroSindicato; }
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public void setDataUltimoPagamento(LocalDate dataUltimoPagamento) { this.dataUltimoPagamento = dataUltimoPagamento; }
    public void setAgendaPagamento(String agendaPagamento) { this.agendaPagamento = agendaPagamento; }

    public abstract double calcularSalario(LocalDate data);

    public abstract double calcularDeducoes(LocalDate data);
}