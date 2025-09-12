package br.ufal.ic.p2.wepayu.controller;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.models.*;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.WeekFields;
import java.util.*;

public class SistemaFolha {
    private Map<String, Empregado> empregados;
    private int proximoId;
    private final String ARQUIVO_ESTADO = "estado.xml";
    private LocalDate dataSistema;
    private Stack<Map<String, Empregado>> undoStack;
    private Stack<Map<String, Empregado>> redoStack;

    public SistemaFolha() {
        this.empregados = new HashMap<>();
        this.proximoId = 1;
        this.dataSistema = null;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        carregarEstado();
    }

    @SuppressWarnings("unchecked")
    private void carregarEstado() {
        try (XMLDecoder decoder = new XMLDecoder(new FileInputStream(ARQUIVO_ESTADO))) {
            this.empregados = (Map<String, Empregado>) decoder.readObject();
            this.proximoId = (int) decoder.readObject();
        } catch (Exception e) {
            this.empregados = new HashMap<>();
            this.proximoId = 1;
        }
    }

    public void encerrarSistema() {
        try (XMLEncoder encoder = new XMLEncoder(new FileOutputStream(ARQUIVO_ESTADO))) {
            encoder.setPersistenceDelegate(LocalDate.class,
                    new PersistenceDelegate() {
                        @Override
                        protected Expression instantiate(Object oldInstance, Encoder out) {
                            LocalDate d = (LocalDate) oldInstance;
                            return new Expression(d, LocalDate.class, "parse", new Object[]{d.toString()});
                        }
                    });
            encoder.writeObject(this.empregados);
            encoder.writeObject(this.proximoId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void zerarSistema() {
        this.empregados = new HashMap<>();
        this.proximoId = 1;
        this.undoStack.clear();
        this.redoStack.clear();
        encerrarSistema();
    }

    private void salvarEstado() {
        Map<String, Empregado> estadoCopia = new HashMap<>();
        for (Map.Entry<String, Empregado> entry : empregados.entrySet()) {
            try {
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                XMLEncoder encoder = new XMLEncoder(out);
                encoder.writeObject(entry.getValue());
                encoder.close();

                java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(out.toByteArray());
                XMLDecoder decoder = new XMLDecoder(in);
                Empregado empregadoCopia = (Empregado) decoder.readObject();
                decoder.close();
                estadoCopia.put(entry.getKey(), empregadoCopia);
            } catch (Exception e) {
                estadoCopia.put(entry.getKey(), entry.getValue());
            }
        }
        undoStack.push(estadoCopia);
        redoStack.clear();
    }

    private String formatarHoras(double horas) {
        if (horas == (long) horas) {
            return String.format("%d", (long) horas);
        } else {
            return String.format("%.1f", horas).replace('.', ',');
        }
    }

    private String formatarValor(double valor) {
        return String.format("%.2f", valor).replace('.', ',');
    }

    public String criarEmpregado(String nome, String endereco, String tipo, double salario) throws Exception {
        salvarEstado();
        if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
        if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
        if (salario < 0) throw new Exception("Salario deve ser nao-negativo.");
        if (!tipo.equalsIgnoreCase("horista") && !tipo.equalsIgnoreCase("assalariado")) {
            if (tipo.equalsIgnoreCase("comissionado")) {
                throw new Exception("Tipo nao aplicavel.");
            }
            throw new Exception("Tipo invalido.");
        }
        String id = String.valueOf(proximoId++);
        Empregado empregado = null;
        if (tipo.equalsIgnoreCase("horista")) {
            empregado = new EmpregadoHorista(id, nome, endereco, "horista", salario);
            empregado.setAgendaPagamento("semanal 5");
        } else {
            empregado = new EmpregadoAssalariado(id, nome, endereco, "assalariado", salario);
            empregado.setAgendaPagamento("mensal $");
        }
        empregados.put(id, empregado);
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, double salario, double comissao) throws Exception {
        salvarEstado();
        if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
        if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
        if (salario < 0) throw new Exception("Salario deve ser nao-negativo.");
        if (comissao < 0) throw new Exception("Comissao deve ser nao-negativa.");
        if (!tipo.equalsIgnoreCase("comissionado")) {
            throw new Exception("Tipo nao aplicavel.");
        }
        String id = String.valueOf(proximoId++);
        Empregado empregado = new EmpregadoComissionado(id, nome, endereco, "comissionado", salario, comissao);
        empregado.setAgendaPagamento("semanal 2 5");
        empregados.put(id, empregado);
        return id;
    }

    public void alteraEmpregado(String id, String atributo, String valor) throws Exception {
        salvarEstado();
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = getEmpregado(id);
        if (e == null) throw new EmpregadoNaoExisteException();
        switch (atributo.toLowerCase()) {
            case "nome":
                if (valor == null || valor.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
                e.setNome(valor);
                break;
            case "endereco":
                if (valor == null || valor.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
                e.setEndereco(valor);
                break;
            case "salario":
                if (valor == null || valor.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
                try {
                    double novoSalario = Double.parseDouble(valor.replace(',', '.'));
                    if (novoSalario < 0) throw new Exception("Salario deve ser nao-negativo.");
                    e.setSalario(novoSalario);
                } catch (NumberFormatException ex) {
                    throw new Exception("Salario deve ser numerico.");
                }
                break;
            case "comissao":
                if (valor == null || valor.isEmpty()) throw new Exception("Comissao nao pode ser nula.");
                if (!(e instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
                try {
                    double novaComissao = Double.parseDouble(valor.replace(',', '.'));
                    if (novaComissao < 0) throw new Exception("Comissao deve ser nao-negativa.");
                    ((EmpregadoComissionado) e).setTaxaDeComissao(novaComissao);
                } catch (NumberFormatException ex) {
                    throw new Exception("Comissao deve ser numerica.");
                }
                break;
            case "metodopagamento":
                if (valor == null || valor.isEmpty()) throw new Exception("Metodo de pagamento invalido.");
                if (valor.equalsIgnoreCase("emmaos") || valor.equalsIgnoreCase("emMaos") || valor.equalsIgnoreCase("em_maos") || valor.equalsIgnoreCase("em mãos") ) {
                    e.setMetodoPagamento(new EmMaos());
                } else if (valor.equalsIgnoreCase("correios")) {
                    e.setMetodoPagamento(new Correios());
                } else if (valor.equalsIgnoreCase("banco")) {
                    throw new Exception("Use a sobrecarga com banco/agencia/conta.");
                } else {
                    throw new Exception("Metodo de pagamento invalido.");
                }
                break;
            case "tipo":
                if ("horista".equalsIgnoreCase(valor) || "assalariado".equalsIgnoreCase(valor)) {
                    if (e instanceof EmpregadoHorista && "horista".equalsIgnoreCase(valor)) return;
                    if (e instanceof EmpregadoAssalariado && "assalariado".equalsIgnoreCase(valor)) return;
                    Empregado novoEmpregado = null;
                    if ("horista".equalsIgnoreCase(valor)) {
                        novoEmpregado = new EmpregadoHorista(e.getId(), e.getNome(), e.getEndereco(), "horista", e.getSalario());
                    } else if ("assalariado".equalsIgnoreCase(valor)) {
                        novoEmpregado = new EmpregadoAssalariado(e.getId(), e.getNome(), e.getEndereco(), "assalariado", e.getSalario());
                    }
                    if (e.isSindicalizado()) {
                        novoEmpregado.setMembroSindicato(e.getMembroSindicato());
                    }
                    if (e.getMetodoPagamento() != null) {
                        novoEmpregado.setMetodoPagamento(e.getMetodoPagamento());
                    }
                    empregados.put(e.getId(), novoEmpregado);
                } else {
                    throw new Exception("Tipo invalido.");
                }
                break;
            case "sindicalizado":
                if ("false".equalsIgnoreCase(valor)) {
                    e.setMembroSindicato(null);
                } else {
                    throw new Exception("Valor deve ser false.");
                }
                break;
            case "agendapagamento":
                if (!valor.equals("semanal 5") && !valor.equals("mensal $") && !valor.equals("semanal 2 5")) {
                    throw new Exception("Agenda de pagamento nao esta disponivel");
                }
                e.setAgendaPagamento(valor);
                break;
            default:
                throw new Exception("Atributo nao existe.");
        }
    }

    public void alteraEmpregado(String id, String atributo, String valor, String valorExtra) throws Exception {
        salvarEstado();
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = getEmpregado(id);
        if (e == null) throw new EmpregadoNaoExisteException();
        if ("tipo".equalsIgnoreCase(atributo)) {
            Empregado novoEmpregado = null;
            if ("horista".equalsIgnoreCase(valor)) {
                try {
                    double novoSalario = Double.parseDouble(valorExtra.replace(',', '.'));
                    if (novoSalario < 0) throw new Exception("Salario deve ser nao-negativo.");
                    novoEmpregado = new EmpregadoHorista(e.getId(), e.getNome(), e.getEndereco(), "horista", novoSalario);
                } catch (NumberFormatException ex) {
                    throw new Exception("Salario deve ser numerico.");
                }
            } else if ("assalariado".equalsIgnoreCase(valor)) {
                try {
                    double novoSalario = Double.parseDouble(valorExtra.replace(',', '.'));
                    if (novoSalario < 0) throw new Exception("Salario deve ser nao-negativo.");
                    novoEmpregado = new EmpregadoAssalariado(e.getId(), e.getNome(), e.getEndereco(), "assalariado", novoSalario);
                } catch (NumberFormatException ex) {
                    throw new Exception("Salario deve ser numerico.");
                }
            } else if ("comissionado".equalsIgnoreCase(valor)) {
                try {
                    double novaComissao = Double.parseDouble(valorExtra.replace(',', '.'));
                    if (novaComissao < 0) throw new Exception("Comissao deve ser nao-negativa.");
                    novoEmpregado = new EmpregadoComissionado(e.getId(), e.getNome(), e.getEndereco(), "comissionado", e.getSalario(), novaComissao);
                } catch (NumberFormatException ex) {
                    throw new Exception("Comissao deve ser numerica.");
                }
            } else {
                throw new Exception("Tipo invalido.");
            }
            if (e.isSindicalizado()) {
                novoEmpregado.setMembroSindicato(e.getMembroSindicato());
            }
            if (e.getMetodoPagamento() != null) {
                novoEmpregado.setMetodoPagamento(e.getMetodoPagamento());
            }
            empregados.put(e.getId(), novoEmpregado);
        } else {
            throw new Exception("Atributo nao existe.");
        }
    }

    public void alteraEmpregado(String id, String atributo, boolean ehSindicalizado, String idSindicato, double taxaSindical) throws Exception {
        salvarEstado();
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = getEmpregado(id);
        if (e == null) throw new EmpregadoNaoExisteException();
        if (ehSindicalizado) {
            for (Empregado outro : empregados.values()) {
                if (!outro.getId().equals(id) && outro.isSindicalizado() && outro.getMembroSindicato().getIdMembro().equals(idSindicato)) {
                    throw new Exception("Ha outro empregado com esta identificacao de sindicato");
                }
            }
            e.setMembroSindicato(new MembroSindicato(idSindicato, taxaSindical));
        } else {
            e.setMembroSindicato(null);
        }
    }

    public void alteraEmpregado(String id, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws Exception {
        salvarEstado();
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = getEmpregado(id);
        if (e == null) throw new EmpregadoNaoExisteException();
        if ("metodopagamento".equalsIgnoreCase(atributo) && "banco".equalsIgnoreCase(valor1)) {
            MetodoPagamento metodo = new Banco(banco, agencia, contaCorrente);
            e.setMetodoPagamento(metodo);
        } else {
            throw new Exception("Atributo nao existe.");
        }
    }

    public void lancaTaxaServico(String idMembro, String data, double valor) throws Exception {
        salvarEstado();
        if (idMembro == null || idMembro.isEmpty()) throw new Exception("Identificacao do membro nao pode ser nula.");
        if (valor <= 0) throw new Exception("Valor deve ser positivo.");
        LocalDate dataDaTaxa = parseDataComValidacao(data, "Data invalida.");
        for (Empregado emp : empregados.values()) {
            if (emp.isSindicalizado() && emp.getMembroSindicato().getIdMembro().equals(idMembro)) {
                TaxaServico novaTaxa = new TaxaServico(dataDaTaxa, valor);
                emp.getMembroSindicato().adicionaTaxaServico(novaTaxa);
                return;
            }
        }
        throw new Exception("Membro nao existe.");
    }
    public void undo() throws Exception {
        if (undoStack.isEmpty()) {
            throw new Exception("Nao ha comando a desfazer.");
        }
        Map<String, Empregado> estadoAnterior = undoStack.pop();
        redoStack.push(new HashMap<>(empregados)); // Salva uma cópia do estado atual
        empregados = estadoAnterior;
    }

    public void redo() throws Exception {
        if (redoStack.isEmpty()) {
            throw new Exception("Nao ha comando a refazer.");
        }
        Map<String, Empregado> estadoRefeito = redoStack.pop();
        undoStack.push(new HashMap<>(empregados)); // Salva uma cópia do estado atual
        empregados = estadoRefeito;
    }
    public String getTaxasServico(String id, String dataInicialStr, String dataFinalStr) throws Exception {
        if (id == null || id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado empregado = getEmpregado(id);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        if (!empregado.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");
        LocalDate dInicial = parseDataComValidacao(dataInicialStr, "Data inicial invalida.");
        LocalDate dFinal = parseDataComValidacao(dataFinalStr, "Data final invalida.");
        if (dInicial.isAfter(dFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        double totalTaxas = empregado.getMembroSindicato().getTaxasDeServicoNoPeriodo(dInicial, dFinal);
        return formatarValor(totalTaxas);
    }

    public void lancaCartao(String idEmpregado, String data, String horas) throws Exception {
        salvarEstado();
        if (idEmpregado == null || idEmpregado.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado empregado = getEmpregado(idEmpregado);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        if (!(empregado instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        double horasTrabalhadas;
        try {
            horasTrabalhadas = Double.parseDouble(horas.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new Exception("Horas devem ser numericas.");
        }
        if (horasTrabalhadas <= 0) throw new Exception("Horas devem ser positivas.");
        LocalDate dataDoCartao = parseDataComValidacao(data, "Data invalida.");
        CartaoDePonto novoCartao = new CartaoDePonto(dataDoCartao, horasTrabalhadas);
        ((EmpregadoHorista) empregado).adicionarCartaoDePonto(novoCartao);
    }

    public void lancaVenda(String idEmpregado, String data, String valor) throws Exception {
        salvarEstado();
        if (idEmpregado == null || idEmpregado.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado empregado = getEmpregado(idEmpregado);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        if (!(empregado instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
        double valorDaVenda;
        try {
            valorDaVenda = Double.parseDouble(valor.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new Exception("Valor deve ser numerico.");
        }
        if (valorDaVenda <= 0) throw new Exception("Valor deve ser positivo.");
        LocalDate dataDaVenda = parseDataComValidacao(data, "Data invalida.");
        ResultadoDaVenda novaVenda = new ResultadoDaVenda(dataDaVenda, valorDaVenda);
        ((EmpregadoComissionado) empregado).adicionarResultadoDeVenda(novaVenda);
    }

    public String getHorasNormaisTrabalhadas(String id, String dataInicialStr, String dataFinalStr) throws Exception {
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado empregado = getEmpregado(id);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        if (!(empregado instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        LocalDate dInicial = parseDataComValidacao(dataInicialStr, "Data inicial invalida.");
        LocalDate dFinal = parseDataComValidacao(dataFinalStr, "Data final invalida.");
        if (dInicial.isAfter(dFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        double horas = ((EmpregadoHorista) empregado).getHorasNormaisTrabalhadas(dInicial, dFinal);
        return formatarHoras(horas);
    }

    public String getHorasExtrasTrabalhadas(String id, String dataInicialStr, String dataFinalStr) throws Exception {
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado empregado = getEmpregado(id);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        if (!(empregado instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        LocalDate dInicial = parseDataComValidacao(dataInicialStr, "Data inicial invalida.");
        LocalDate dFinal = parseDataComValidacao(dataFinalStr, "Data final invalida.");
        if (dInicial.isAfter(dFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        double horas = ((EmpregadoHorista) empregado).getHorasExtrasTrabalhadas(dInicial, dFinal);
        return formatarHoras(horas);
    }

    public String getVendasRealizadas(String id, String dataInicialStr, String dataFinalStr) throws Exception {
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado empregado = getEmpregado(id);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        if (!(empregado instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
        LocalDate dInicial = parseDataComValidacao(dataInicialStr, "Data inicial invalida.");
        LocalDate dFinal = parseDataComValidacao(dataFinalStr, "Data final invalida.");
        if (dInicial.isAfter(dFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        double totalVendas = ((EmpregadoComissionado) empregado).getVendasRealizadas(dInicial, dFinal);
        return formatarValor(totalVendas);
    }

    public String getAtributoEmpregado(String id, String atributo) throws Exception {
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado empregado = getEmpregado(id);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        switch (atributo.toLowerCase()) {
            case "nome":
                return empregado.getNome();
            case "endereco":
                return empregado.getEndereco();
            case "tipo":
                return empregado.getTipo();
            case "salario":
                return formatarValor(empregado.getSalario());
            case "sindicalizado":
                return String.valueOf(empregado.isSindicalizado());
            case "comissao":
                if (empregado instanceof EmpregadoComissionado) {
                    double taxa = ((EmpregadoComissionado) empregado).getTaxaDeComissao();
                    return formatarValor(taxa);
                }
                throw new Exception("Empregado nao eh comissionado.");
            case "metodopagamento":
                if (empregado.getMetodoPagamento() == null) {
                    return "nao-especificado";
                }
                return empregado.getMetodoPagamento().getTipo();
            case "banco":
                if (empregado.getMetodoPagamento() instanceof Banco) {
                    return ((Banco) empregado.getMetodoPagamento()).getBanco();
                }
                throw new Exception("Empregado nao recebe em banco.");
            case "agencia":
                if (empregado.getMetodoPagamento() instanceof Banco) {
                    return ((Banco) empregado.getMetodoPagamento()).getAgencia();
                }
                throw new Exception("Empregado nao recebe em banco.");
            case "contacorrente":
                if (empregado.getMetodoPagamento() instanceof Banco) {
                    return ((Banco) empregado.getMetodoPagamento()).getContaCorrente();
                }
                throw new Exception("Empregado nao recebe em banco.");
            case "idsindicato":
                if (empregado.isSindicalizado()) {
                    return empregado.getMembroSindicato().getIdMembro();
                }
                throw new Exception("Empregado nao eh sindicalizado.");
            case "taxasindical":
                if (empregado.isSindicalizado()) {
                    return formatarValor(empregado.getMembroSindicato().getTaxaSindical());
                }
                throw new Exception("Empregado nao eh sindicalizado.");
            case "agendapagamento":
                return empregado.getAgendaPagamento();
            default:
                throw new Exception("Atributo nao existe.");
        }
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        List<Empregado> empregadosEncontrados = new ArrayList<>();
        for (Empregado e : this.empregados.values()) {
            if (e.getNome().equals(nome)) {
                empregadosEncontrados.add(e);
            }
        }
        int indexDaLista = indice - 1;
        if (!empregadosEncontrados.isEmpty() && indexDaLista >= 0 && indexDaLista < empregadosEncontrados.size()) {
            return empregadosEncontrados.get(indexDaLista).getId();
        } else {
            throw new Exception("Nao ha empregado com esse nome.");
        }
    }

    public void removerEmpregado(String idEmpregado) throws Exception {
        salvarEstado();
        if (idEmpregado == null || idEmpregado.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (empregados.containsKey(idEmpregado)) {
            empregados.remove(idEmpregado);
        } else {
            throw new EmpregadoNaoExisteException();
        }
    }

    public String totalFolha(String dataStr) throws Exception {
        LocalDate data = parseDataComValidacao(dataStr, "Data invalida.");
        double total = 0.0;
        for (Empregado empregado : empregados.values()) {
            if (isDiaDePagamento(empregado, data)) {
                double salarioBruto = empregado.calcularSalario(data);
                double deducoes = empregado.calcularDeducoes(data);
                total += salarioBruto - deducoes;
            }
        }
        return formatarValor(total);
    }

    public void rodaFolha(String dataStr, String nomeArquivo) throws Exception {
        salvarEstado();
        LocalDate data = parseDataComValidacao(dataStr, "Data invalida.");
        List<String> pagamentos = new ArrayList<>();
        for (Empregado empregado : empregados.values()) {
            if (isDiaDePagamento(empregado, data)) {
                double salarioBruto = empregado.calcularSalario(data);
                double deducoes = empregado.calcularDeducoes(data);
                double salarioLiquido = salarioBruto - deducoes;
                pagamentos.add(empregado.getId() + " " + empregado.getNome() + " " + formatarValor(salarioBruto) + " " + formatarValor(deducoes) + " " + formatarValor(salarioLiquido));
                limparDadosDePagamento(empregado, data);
            }
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            writer.println("FOLHA DE PAGAMENTO DO DIA " + data.format(DateTimeFormatter.ofPattern("uuuu-MM-dd")));
            for (String pagamento : pagamentos) {
                writer.println(pagamento);
            }
        } catch (IOException e) {
            throw new IOException("Nao foi possivel gerar o arquivo de saida: " + e.getMessage());
        }
        this.dataSistema = data;
    }

    private void limparDadosDePagamento(Empregado empregado, LocalDate data) {
        if (empregado instanceof EmpregadoHorista) {
            EmpregadoHorista horista = (EmpregadoHorista) empregado;
            horista.getCartaoDePontos().removeIf(cartao -> !cartao.getData().isAfter(data));
        } else if (empregado instanceof EmpregadoComissionado) {
            EmpregadoComissionado comissionado = (EmpregadoComissionado) empregado;
            comissionado.getResultadosDeVenda().removeIf(venda -> !venda.getDataVenda().isAfter(data));
        }
        if (empregado.isSindicalizado()) {
            empregado.getMembroSindicato().getTaxasDeServico().removeIf(taxa -> !taxa.getData().isAfter(data));
        }
        empregado.setDataUltimoPagamento(data);
    }

    private boolean isDiaDePagamento(Empregado empregado, LocalDate data) {
        if (empregado instanceof EmpregadoHorista) {
            return data.getDayOfWeek() == DayOfWeek.FRIDAY;
        } else if (empregado instanceof EmpregadoAssalariado) {
            if (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
                return false;
            }
            return data.getDayOfMonth() == data.lengthOfMonth();
        } else if (empregado instanceof EmpregadoComissionado) {
            if (data.getDayOfWeek() != DayOfWeek.FRIDAY) {
                return false;
            }
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            int weekNumber = data.get(weekFields.weekOfWeekBasedYear());
            return weekNumber % 2 != 0;
        }
        return false;
    }

    public Empregado getEmpregado(String id) {
        return empregados.get(id);
    }

    private LocalDate parseDataComValidacao(String dataStr, String mensagemErro) throws Exception {
        if (dataStr == null || dataStr.isEmpty()) throw new Exception(mensagemErro);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
        try {
            return LocalDate.parse(dataStr, formatter);
        } catch (DateTimeParseException ex) {
            throw new Exception(mensagemErro);
        }
    }

    public int getNumeroDeEmpregados() {
        return empregados.size();
    }
}