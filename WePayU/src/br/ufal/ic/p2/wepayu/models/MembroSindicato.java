package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MembroSindicato {
    private String idMembro;
    private double taxaSindical;
    private List<TaxaServico> taxasDeServico;

    public MembroSindicato() {
        this.taxasDeServico = new ArrayList<>();
    }

    public MembroSindicato(String idMembro, double taxaSindical) {
        this.idMembro = idMembro;
        this.taxaSindical = taxaSindical;
        this.taxasDeServico = new ArrayList<>();
    }

    public double getTaxasDeServicoNoPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        double totalTaxas = 0;
        if (taxasDeServico != null) {
            for (TaxaServico taxa : taxasDeServico) {
                if (taxa.getData() != null && !taxa.getData().isBefore(dataInicial) && taxa.getData().isBefore(dataFinal)) {
                    totalTaxas += taxa.getValor();
                }
            }
        }
        return totalTaxas;
    }

    public void adicionaTaxaServico(TaxaServico taxa) {
        if (this.taxasDeServico == null) {
            this.taxasDeServico = new ArrayList<>();
        }
        this.taxasDeServico.add(taxa);
    }

    public String getIdMembro() { return idMembro; }
    public void setIdMembro(String idMembro) { this.idMembro = idMembro; }
    public double getTaxaSindical() { return taxaSindical; }
    public void setTaxaSindical(double taxaSindical) { this.taxaSindical = taxaSindical; }
    public List<TaxaServico> getTaxasDeServico() { return taxasDeServico; }
    public void setTaxasDeServico(List<TaxaServico> taxasDeServico) { this.taxasDeServico = taxasDeServico; }
}