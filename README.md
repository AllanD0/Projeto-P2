📑 Sistema de Folha de Pagamento - WePayU

Este repositório contém a implementação do sistema de folha de pagamento WePayU, desenvolvido como parte de um projeto acadêmico.

⚠️ Aviso: o sistema ainda não passou em 100% dos testes de aceitação. Algumas funcionalidades podem conter erros ou comportamentos inesperados.

O foco do projeto é a lógica de negócio (business logic): administrar corretamente os pagamentos dos empregados, respeitando os diferentes tipos de contratos, agendas de pagamento e regras de cálculo (salário, horas extras, comissões e deduções sindicais).

✨ Funcionalidades (User Stories)

US1: Adição de novos empregados (horistas, assalariados e comissionados).

US2: Remoção de empregados.

US3: Lançamento de cartões de ponto (horistas).

US4: Lançamento de resultados de venda (comissionados).

US5: Lançamento de taxas de serviço sindicais.

US6: Alteração de dados de empregados (nome, endereço, tipo, método de pagamento, filiação sindical, etc.).

US7: Execução da folha de pagamento em uma data específica.

US8: Sistema de Undo/Redo para operações que alteram o estado.

US9: Suporte a diferentes agendas de pagamento (semanais, mensais, quinzenais).

US10: Criação de novas agendas de pagamento customizadas.

⚖️ Regras de Negócio

Horistas: recebem por hora trabalhada; horas extras (>8h/dia) pagas a 150%. Pagos toda sexta-feira.

Assalariados: recebem um salário fixo mensal; pagos no último dia útil do mês.

Comissionados: salário fixo + comissão sobre vendas. Pagos a cada 2 sextas-feiras.

Sindicato: empregados podem ser sindicalizados, pagando taxa sindical fixa + taxas de serviço ocasionais.

Métodos de Pagamento: cheque em mãos, cheque pelos correios ou depósito em conta bancária.

🏗️ Arquitetura

Facade → ponto de entrada para os testes.

Controller (SistemaFolha) → centraliza a lógica de negócio.

Models → entidades principais (Empregado, CartaoDePonto, ResultadoDeVenda, etc.).

Persistência → dados salvos em estado.xml via XMLEncoder/XMLDecoder.

Undo/Redo (Memento) → snapshots do estado armazenados em pilhas.

Strategy → cada tipo de empregado implementa sua própria regra de pagamento.

🧪 Testes de Aceitação

Os testes utilizam EasyAccept para validar os requisitos.

Scripts localizados na pasta tests/ (ex.: us1.txt, us7.txt, ...).

Execução: configure a Main class como entry point e rode os testes com o working directory definido como a pasta WePayU.

🚀 Como Rodar

Clone o repositório:

git clone https://github.com/AllanD0/Projeto-P2.git
cd Projeto-P2


Compile o projeto (Java 11+).

Configure o classpath para incluir easyaccept.jar.

Rode a classe Main para executar os testes automatizados.

📘 Glossário

Horista: empregado pago por hora trabalhada.

Assalariado: empregado com salário mensal fixo.

Comissionado: empregado com salário fixo + comissão sobre vendas.

Cartão de Ponto: registro de horas trabalhadas por dia.

Resultado de Venda: registro de uma venda feita por um comissionado.

Taxa Sindical: desconto periódico do sindicato.

Taxa de Serviço: desconto avulso lançado pelo sindicato.

Rodar Folha: calcular e efetuar pagamentos em uma data.
