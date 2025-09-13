package br.ufal.ic.p2.wepayu.controller;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.models.*;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.beans.Encoder;

public class SistemaFolha {
    private Map<String, Empregado> empregados;
    private int proximoId;
    private final String ARQUIVO_ESTADO = "estado.xml";
    private Stack<Map<String, Empregado>> undoStack;
    private Stack<Map<String, Empregado>> redoStack;
    private List<String> agendasDisponiveis;

    public SistemaFolha() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        carregarEstado();
    }

    @SuppressWarnings("unchecked")
    private void carregarEstado() {
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {}
        }));
        try (XMLDecoder decoder = new XMLDecoder(new FileInputStream(ARQUIVO_ESTADO))) {
            this.empregados = (Map<String, Empregado>) decoder.readObject();
            this.proximoId = (int) decoder.readObject();
            this.agendasDisponiveis = (List<String>) decoder.readObject();
        } catch (Exception e) {
            this.empregados = new HashMap<>();
            this.proximoId = 1;
            this.agendasDisponiveis = new ArrayList<>(Arrays.asList("semanal 5", "mensal $", "semanal 2 5"));
        } finally {
            System.setErr(originalErr);
        }
    }

    public void encerrarSistema() {
        try (XMLEncoder encoder = new XMLEncoder(new FileOutputStream(ARQUIVO_ESTADO))) {
            encoder.setPersistenceDelegate(LocalDate.class,
                    new PersistenceDelegate() {
                        @Override
                        protected Expression instantiate(Object oldInstance, Encoder out) {
                            return new Expression(oldInstance, LocalDate.class, "parse", new Object[]{oldInstance.toString()});
                        }
                    });
            encoder.writeObject(this.empregados);
            encoder.writeObject(this.proximoId);
            encoder.writeObject(this.agendasDisponiveis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void zerarSistema() {
        salvarEstado();
        this.empregados = new HashMap<>();
        this.proximoId = 1;
        this.undoStack.clear();
        this.redoStack.clear();
        this.agendasDisponiveis = new ArrayList<>(Arrays.asList("semanal 5", "mensal $", "semanal 2 5"));
        encerrarSistema();
    }

    private void salvarEstado() {
        Map<String, Empregado> estadoCopia = deepCopyMap(this.empregados);
        undoStack.push(estadoCopia);
        redoStack.clear();
    }

    private String formatarHoras(double horas) {
        if (horas == (long) horas) return String.format("%d", (long) horas);
        return String.format("%.1f", horas).replace('.', ',');
    }

    private String formatarValor(double valor) {
        return String.format("%.2f", valor).replace('.', ',');
    }

    public String criarEmpregado(String nome, String endereco, String tipo, double salario, Double comissao) throws Exception {
        if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
        if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
        if (salario < 0) throw new Exception("Salario deve ser nao-negativo.");

        if (tipo.equalsIgnoreCase("comissionado")) {
            if (comissao == null) throw new Exception("Tipo nao aplicavel.");
            if (comissao < 0) throw new Exception("Comissao deve ser nao-negativa.");
        } else if (tipo.equalsIgnoreCase("horista") || tipo.equalsIgnoreCase("assalariado")) {
            if (comissao != null) throw new Exception("Tipo nao aplicavel.");
        } else {
            throw new Exception("Tipo invalido.");
        }

        salvarEstado();
        String id = String.valueOf(proximoId++);
        Empregado empregado;

        if (tipo.equalsIgnoreCase("horista")) {
            empregado = new EmpregadoHorista(id, nome, endereco, tipo, salario);
            empregado.setAgendaPagamento("semanal 5");
        } else if (tipo.equalsIgnoreCase("assalariado")) {
            empregado = new EmpregadoAssalariado(id, nome, endereco, tipo, salario);
            empregado.setAgendaPagamento("mensal $");
        } else { // Comissionado
            empregado = new EmpregadoComissionado(id, nome, endereco, tipo, salario, comissao);
            empregado.setAgendaPagamento("semanal 2 5");
        }

        empregados.put(id, empregado);
        return id;
    }


    public void criarAgendaDePagamentos(String descricao) throws Exception {
        if (agendasDisponiveis.contains(descricao)) throw new Exception("Agenda de pagamentos ja existe");
        salvarEstado();
        agendasDisponiveis.add(descricao);
    }

    private boolean isDiaDePagamento(Empregado empregado, LocalDate data) {
        String agenda = empregado.getAgendaPagamento();
        String[] partes = agenda.split(" ");
        String tipo = partes[0];

        if (tipo.equalsIgnoreCase("mensal")) {
            String dia = partes[1];
            if (dia.equals("$")) {
                LocalDate ultimoDiaDoMes = data.withDayOfMonth(data.lengthOfMonth());
                LocalDate diaDePagamentoReal = ultimoDiaDoMes;
                if (ultimoDiaDoMes.getDayOfWeek() == DayOfWeek.SATURDAY) diaDePagamentoReal = ultimoDiaDoMes.minusDays(1);
                else if (ultimoDiaDoMes.getDayOfWeek() == DayOfWeek.SUNDAY) diaDePagamentoReal = ultimoDiaDoMes.minusDays(2);
                return data.equals(diaDePagamentoReal);
            } else {
                return data.getDayOfMonth() == Integer.parseInt(dia);
            }
        } else if (tipo.equalsIgnoreCase("semanal")) {
            DayOfWeek diaDaSemanaAgendado = DayOfWeek.of(Integer.parseInt(partes[partes.length - 1]));
            if (data.getDayOfWeek() != diaDaSemanaAgendado) return false;

            if (partes.length == 2) return true;
            else {
                LocalDate dataDeReferencia = LocalDate.of(2004, 12, 31);
                long semanasDesdeReferencia = ChronoUnit.WEEKS.between(dataDeReferencia, data);
                int frequencia = Integer.parseInt(partes[1]);
                return semanasDesdeReferencia > 0 && semanasDesdeReferencia % frequencia == 0;
            }
        }
        return false;
    }

    public void undo() throws Exception {
        if (undoStack.isEmpty()) throw new Exception("Nao ha comando a desfazer.");
        Map<String, Empregado> estadoAtual = deepCopyMap(this.empregados);
        redoStack.push(estadoAtual);
        this.empregados = undoStack.pop();
    }

    public void redo() throws Exception {
        if (redoStack.isEmpty()) throw new Exception("Nao ha comando a refazer.");
        Map<String, Empregado> estadoAtual = deepCopyMap(this.empregados);
        undoStack.push(estadoAtual);
        this.empregados = redoStack.pop();
    }

    public Empregado getEmpregado(String id) throws EmpregadoNaoExisteException {
        if (id == null || id.isEmpty()) throw new EmpregadoNaoExisteException("Identificacao do empregado nao pode ser nula.");
        Empregado e = empregados.get(id);
        if (e == null) throw new EmpregadoNaoExisteException();
        return e;
    }

    private Map<String, Empregado> deepCopyMap(Map<String, Empregado> original) {
        Map<String, Empregado> copia = new HashMap<>();
        for (Map.Entry<String, Empregado> entry : original.entrySet()) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(entry.getValue());
                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bis);
                copia.put(entry.getKey(), (Empregado) ois.readObject());
            } catch (Exception e) {
                copia.put(entry.getKey(), entry.getValue());
            }
        }
        return copia;
    }

    public void removerEmpregado(String idEmpregado) throws Exception {
        if (idEmpregado == null || idEmpregado.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(idEmpregado)) throw new EmpregadoNaoExisteException();
        salvarEstado();
        empregados.remove(idEmpregado);
    }
    public void alteraEmpregado(String id, String atributo, String valor1, Object... extras) throws Exception {
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = empregados.get(id);
        if (e == null) throw new EmpregadoNaoExisteException();

        switch (atributo.toLowerCase()) {
            case "nome":
                if (valor1 == null || valor1.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
                e.setNome(valor1);
                break;
            case "endereco":
                if (valor1 == null || valor1.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
                e.setEndereco(valor1);
                break;
            case "salario":
                if (valor1 == null || valor1.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
                try {
                    double novoSalario = Double.parseDouble(valor1.replace(',', '.'));
                    if (novoSalario < 0) throw new Exception("Salario deve ser nao-negativo.");
                    e.setSalario(novoSalario);
                } catch (NumberFormatException ex) {
                    throw new Exception("Salario deve ser numerico.");
                }
                break;
            case "comissao":
                if (!(e instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
                if (valor1 == null || valor1.isEmpty()) throw new Exception("Comissao nao pode ser nula.");
                try {
                    double novaComissao = Double.parseDouble(valor1.replace(',', '.'));
                    if (novaComissao < 0) throw new Exception("Comissao deve ser nao-negativa.");
                    ((EmpregadoComissionado) e).setTaxaDeComissao(novaComissao);
                } catch (NumberFormatException ex) {
                    throw new Exception("Comissao deve ser numerica.");
                }
                break;
            case "metodopagamento":
                if ("banco".equalsIgnoreCase(valor1)) {
                    String banco = (String) extras[0];
                    String agencia = (String) extras[1];
                    String conta = (String) extras[2];
                    e.setMetodoPagamento(new Banco(banco, agencia, conta));
                } else if ("correios".equalsIgnoreCase(valor1)) {
                    e.setMetodoPagamento(new Correios());
                } else if ("emmaos".equalsIgnoreCase(valor1)) {
                    e.setMetodoPagamento(new EmMaos());
                } else {
                    throw new Exception("Metodo de pagamento invalido.");
                }
                break;
            case "sindicalizado":
                boolean ehSindicalizado = "true".equalsIgnoreCase(valor1);
                if (ehSindicalizado) {
                    String idSindicato = (String) extras[0];
                    double taxaSindical = Double.parseDouble(((String) extras[1]).replace(',', '.'));
                    if(taxaSindical < 0) throw new Exception("Taxa sindical deve ser nao-negativa.");
                    for (Empregado outro : empregados.values()) {
                        if (!outro.getId().equals(id) && outro.isSindicalizado() && outro.getMembroSindicato().getIdMembro().equals(idSindicato)) {
                            throw new Exception("Ha outro empregado com esta identificacao de sindicato");
                        }
                    }
                    e.setMembroSindicato(new MembroSindicato(idSindicato, taxaSindical));
                } else {
                    e.setMembroSindicato(null);
                }
                break;
            case "tipo":
                Empregado novoEmpregado;
                String valorExtra = (extras.length > 0 && extras[0] != null) ? (String) extras[0] : null;

                if (valor1.equalsIgnoreCase("horista")) {
                    double novoSalario = (valorExtra != null) ? Double.parseDouble(valorExtra.replace(',', '.')) : e.getSalario();
                    novoEmpregado = new EmpregadoHorista(e.getId(), e.getNome(), e.getEndereco(), valor1, novoSalario);
                } else if (valor1.equalsIgnoreCase("assalariado")) {
                    double novoSalario = (valorExtra != null) ? Double.parseDouble(valorExtra.replace(',', '.')) : e.getSalario();
                    novoEmpregado = new EmpregadoAssalariado(e.getId(), e.getNome(), e.getEndereco(), valor1, novoSalario);
                } else if (valor1.equalsIgnoreCase("comissionado")) {
                    if (valorExtra == null) throw new Exception("Comissao nao pode ser nula.");
                    double comissao = Double.parseDouble(valorExtra.replace(',', '.'));
                    novoEmpregado = new EmpregadoComissionado(e.getId(), e.getNome(), e.getEndereco(), valor1, e.getSalario(), comissao);
                } else {
                    throw new Exception("Tipo invalido.");
                }
                novoEmpregado.setMembroSindicato(e.getMembroSindicato());
                novoEmpregado.setMetodoPagamento(e.getMetodoPagamento());
                novoEmpregado.setAgendaPagamento(e.getAgendaPagamento());
                empregados.put(id, novoEmpregado);
                break;
        }
    }
    public void lancaCartao(String idEmpregado, String data, String horas) throws Exception {
        Empregado empregado = getEmpregado(idEmpregado);
        if (!(empregado instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        double horasTrabalhadas = Double.parseDouble(horas.replace(',', '.'));
        if (horasTrabalhadas <= 0) throw new Exception("Horas devem ser positivas.");
        LocalDate dataDoCartao = parseDataComValidacao(data, "Data invalida.");
        salvarEstado();
        CartaoDePonto novoCartao = new CartaoDePonto(dataDoCartao, horasTrabalhadas);
        ((EmpregadoHorista) empregado).adicionarCartaoDePonto(novoCartao);
    }
    public void lancaVenda(String idEmpregado, String data, String valor) throws Exception {
        Empregado empregado = getEmpregado(idEmpregado);
        if (!(empregado instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
        double valorDaVenda = Double.parseDouble(valor.replace(',', '.'));
        if (valorDaVenda <= 0) throw new Exception("Valor deve ser positivo.");
        LocalDate dataDaVenda = parseDataComValidacao(data, "Data invalida.");

        salvarEstado();

        ResultadoDaVenda novaVenda = new ResultadoDaVenda(dataDaVenda, valorDaVenda);
        ((EmpregadoComissionado) empregado).adicionarResultadoDeVenda(novaVenda);
    }

    public void lancaTaxaServico(String idMembro, String data, double valor) throws Exception {
        if (idMembro == null || idMembro.isEmpty()) throw new Exception("Identificacao do membro nao pode ser nula.");
        if (valor <= 0) throw new Exception("Valor deve ser positivo.");
        LocalDate dataDaTaxa = parseDataComValidacao(data, "Data invalida.");

        Empregado emp = null;
        for (Empregado e : empregados.values()) {
            if (e.isSindicalizado() && e.getMembroSindicato().getIdMembro().equals(idMembro)) {
                emp = e;
                break;
            }
        }
        if (emp == null) throw new Exception("Membro nao existe.");

        salvarEstado();

        TaxaServico novaTaxa = new TaxaServico(dataDaTaxa, valor);
        emp.getMembroSindicato().adicionaTaxaServico(novaTaxa);
    }
    public String getAtributoEmpregado(String id, String atributo) throws Exception {
        Empregado empregado = getEmpregado(id);
        switch (atributo.toLowerCase()) {
            case "nome": return empregado.getNome();
            case "endereco": return empregado.getEndereco();
            case "tipo": return empregado.getTipo();
            case "salario": return formatarValor(empregado.getSalario());
            case "sindicalizado": return String.valueOf(empregado.isSindicalizado());
            case "comissao":
                if (empregado instanceof EmpregadoComissionado) return formatarValor(((EmpregadoComissionado) empregado).getTaxaDeComissao());
                throw new Exception("Empregado nao eh comissionado.");
            case "metodopagamento":
                return empregado.getMetodoPagamento().getTipo();
            case "banco":
                if (empregado.getMetodoPagamento() instanceof Banco) return ((Banco) empregado.getMetodoPagamento()).getBanco();
                throw new Exception("Empregado nao recebe em banco.");
            case "agencia":
                if (empregado.getMetodoPagamento() instanceof Banco) return ((Banco) empregado.getMetodoPagamento()).getAgencia();
                throw new Exception("Empregado nao recebe em banco.");
            case "contacorrente":
                if (empregado.getMetodoPagamento() instanceof Banco) return ((Banco) empregado.getMetodoPagamento()).getContaCorrente();
                throw new Exception("Empregado nao recebe em banco.");
            case "idsindicato":
                if (empregado.isSindicalizado()) return empregado.getMembroSindicato().getIdMembro();
                throw new Exception("Empregado nao eh sindicalizado.");
            case "taxasindical":
                if (empregado.isSindicalizado()) return formatarValor(empregado.getMembroSindicato().getTaxaSindical());
                throw new Exception("Empregado nao eh sindicalizado.");
            case "agendapagamento": return empregado.getAgendaPagamento();
            default: throw new Exception("Atributo nao existe.");
        }
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        List<Empregado> encontrados = new ArrayList<>();
        for (Empregado e : this.empregados.values()) {
            if (e.getNome().equals(nome)) encontrados.add(e);
        }
        if (indice > 0 && indice <= encontrados.size()) return encontrados.get(indice - 1).getId();
        throw new Exception("Nao ha empregado com esse nome.");
    }

    public String getHorasNormaisTrabalhadas(String id, String dataInicialStr, String dataFinalStr) throws Exception {
        Empregado empregado = getEmpregado(id);
        if (!(empregado instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        LocalDate dInicial = parseDataComValidacao(dataInicialStr, "Data inicial invalida.");
        LocalDate dFinal = parseDataComValidacao(dataFinalStr, "Data final invalida.");
        if (dInicial.isAfter(dFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        double horas = ((EmpregadoHorista) empregado).getHorasNormaisTrabalhadas(dInicial, dFinal);
        return formatarHoras(horas);
    }

    public String getHorasExtrasTrabalhadas(String id, String dataInicialStr, String dataFinalStr) throws Exception {
        Empregado empregado = getEmpregado(id);
        if (!(empregado instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        LocalDate dInicial = parseDataComValidacao(dataInicialStr, "Data inicial invalida.");
        LocalDate dFinal = parseDataComValidacao(dataFinalStr, "Data final invalida.");
        if (dInicial.isAfter(dFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        double horas = ((EmpregadoHorista) empregado).getHorasExtrasTrabalhadas(dInicial, dFinal);
        return formatarHoras(horas);
    }

    public String getVendasRealizadas(String id, String dataInicialStr, String dataFinalStr) throws Exception {
        Empregado empregado = getEmpregado(id);
        if (!(empregado instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
        LocalDate dInicial = parseDataComValidacao(dataInicialStr, "Data inicial invalida.");
        LocalDate dFinal = parseDataComValidacao(dataFinalStr, "Data final invalida.");
        if (dInicial.isAfter(dFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        double totalVendas = ((EmpregadoComissionado) empregado).getVendasRealizadas(dInicial, dFinal);
        return formatarValor(totalVendas);
    }

    public String getTaxasServico(String id, String dataInicialStr, String dataFinalStr) throws Exception {
        Empregado empregado = getEmpregado(id);
        if (!empregado.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");
        LocalDate dInicial = parseDataComValidacao(dataInicialStr, "Data inicial invalida.");
        LocalDate dFinal = parseDataComValidacao(dataFinalStr, "Data final invalida.");
        if (dInicial.isAfter(dFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        double totalTaxas = empregado.getMembroSindicato().getTaxasDeServicoNoPeriodo(dInicial, dFinal);
        return formatarValor(totalTaxas);
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
        LocalDate data = parseDataComValidacao(dataStr, "Data invalida.");
        salvarEstado();
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            for (Empregado empregado : empregados.values()) {
                if (isDiaDePagamento(empregado, data)) {
                    double salarioBruto = empregado.calcularSalario(data);
                    double deducoes = empregado.calcularDeducoes(data);
                    double salarioLiquido = salarioBruto - deducoes;
                    writer.println(String.join(" ", empregado.getNome(), formatarValor(salarioBruto), formatarValor(deducoes), formatarValor(salarioLiquido)));
                    limparDadosDePagamento(empregado, data);
                }
            }
        } catch (IOException e) {
            throw new IOException("Nao foi possivel gerar o arquivo de saida: " + e.getMessage());
        }
    }

    private void limparDadosDePagamento(Empregado empregado, LocalDate data) {
        if (empregado instanceof EmpregadoHorista) ((EmpregadoHorista) empregado).getCartaoDePontos().clear();
        else if (empregado instanceof EmpregadoComissionado) ((EmpregadoComissionado) empregado).getResultadosDeVenda().clear();
        if (empregado.isSindicalizado()) empregado.getMembroSindicato().getTaxasDeServico().clear();
        empregado.setDataUltimoPagamento(data);
    }

    private LocalDate parseDataComValidacao(String dataStr, String mensagemErro) throws Exception {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
            return LocalDate.parse(dataStr, formatter);
        } catch (DateTimeParseException ex) {
            throw new Exception(mensagemErro);
        }
    }

    public int getNumeroDeEmpregados() {
        return empregados.size();
    }
}