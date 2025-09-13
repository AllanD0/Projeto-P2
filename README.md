istema de Folha de Pagamento - WePayU
Este repositório contém a implementação do sistema de folha de pagamento WePayU, desenvolvido como parte de um projeto acadêmico. O foco principal do projeto é a construção da lógica de negócio (

business logic) de um sistema robusto, capaz de gerenciar diferentes tipos de empregados, agendas de pagamento e regras de cálculo complexas.


O sucesso do projeto é validado pela aprovação em 100% de uma suíte de testes de aceitação automatizados, fornecidos para simular os requisitos do cliente.

✨ Funcionalidades Implementadas (User Stories)
O sistema foi construído de forma incremental, seguindo as User Stories definidas na especificação do projeto. As funcionalidades implementadas incluem:



US1: Adição de novos empregados (horistas, assalariados e comissionados).


US2: Remoção de empregados do sistema.


US3: Lançamento de cartões de ponto para empregados horistas.


US4: Lançamento de resultados de venda para empregados comissionados.


US5: Lançamento de taxas de serviço sindicais.


US6: Alteração de detalhes de um empregado (nome, endereço, tipo, método de pagamento, filiação sindical, etc.).


US7: Funcionalidade completa para rodar a folha de pagamento em uma data específica.


US8: Sistema de Undo/Redo para todas as transações que alteram o estado do sistema.


US9: Suporte a diferentes agendas de pagamento, que podem ser alteradas por empregado.


US10: Criação de novas agendas de pagamento customizadas pela administração.

⚖️ Conceitos e Regras de Negócio
O núcleo do sistema reside na sua capacidade de lidar com diferentes regras de pagamento e deduções.

Tipos de Empregados
Horista: Recebe por hora. Horas trabalhadas além de 8 por dia são pagas com um adicional de 50% (taxa de 1.5). O pagamento é realizado toda sexta-feira.


Assalariado: Recebe um salário mensal fixo. O pagamento é realizado no último dia útil do mês.


Comissionado: Recebe um salário base mais uma comissão percentual sobre suas vendas. O pagamento é quinzenal (a cada 2 sextas-feiras) e consiste em 2 semanas de salário fixo mais as comissões do período.



Sindicato
Empregados podem ser filiados a um sindicato, pagando uma taxa. Nos testes, essa taxa é definida como um valor 

diário para flexibilizar o cálculo em diferentes agendas.

O sindicato também pode lançar taxas de serviços avulsas que são descontadas no próximo pagamento do empregado.

🏗️ Arquitetura e Design
Arquitetura em Camadas: O sistema é separado em:

Facade: A camada de entrada que "traduz" os comandos dos testes para o sistema.

Controller (SistemaFolha): A classe central que orquestra toda a lógica de negócio.

Models: As classes que representam as entidades do sistema (Empregado, CartaoDePonto, etc.), contendo os dados e as lógicas de cálculo específicas.

Padrão Memento (Undo/Redo): A funcionalidade de Undo/Redo foi implementada utilizando duas pilhas (undoStack, redoStack) que armazenam snapshots (cópias profundas) do estado do sistema antes de cada alteração.

Padrão Strategy: O cálculo de salários e deduções é implementado através de métodos abstratos na classe Empregado e implementações concretas nas subclasses, permitindo que cada tipo de empregado tenha sua própria "estratégia" de cálculo.


Persistência: Os dados do sistema são salvos e carregados de um arquivo estado.xml utilizando as bibliotecas java.beans.XMLEncoder e java.beans.XMLDecoder.

🚀 Como Executar os Testes
O projeto utiliza a biblioteca EasyAccept para rodar os testes de aceitação.

Pré-requisitos: Certifique-se de que o arquivo easyaccept.jar está configurado no classpath do projeto.

Configuração: Na sua IDE, configure a execução da classe Main para que o Working Directory (Diretório de Trabalho) seja a pasta WePayU (a pasta que contém os diretórios tests, ok, src, etc.).

Execução: Execute a classe Main, que contém as chamadas para os scripts de teste (ex: tests/us1.txt, tests/us7.txt, etc.).
