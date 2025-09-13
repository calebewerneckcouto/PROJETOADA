# Documentação do Sistema ECommerce AdaCommerce

Este documento serve para explicar a estrutura e o funcionamento do sistema `AdaCommerce`, um sistema de e-commerce simples que permite o gerenciamento de clientes, produtos e pedidos com notificações por e-mail e suporte a diferentes métodos de persistência de dados.

## Estrutura de Diretórios e Principais Componentes

O sistema é dividido nos seguintes pacotes:

- `br.com.adacommerce.ecommerce.configure`: Contém a enumeração `Persistencia` que define os tipos de persistência disponíveis.
- `br.com.adacommerce.ecommerce.controller`: Contém o controlador `ECommerceController` que gerencia toda a interação com o usuário através do console.
- `br.com.adacommerce.ecommerce.exceptions`: Contém as exceções customizadas utilizadas no sistema.
- `br.com.adacommerce.ecommerce.main`: Contém a classe principal `ECommerceSystem` que inicializa e executa o sistema.
- `br.com.adacommerce.ecommerce.model`: Contém as entidades do sistema.
- `br.com.adacommerce.ecommerce.notifications`: Contém as definições e implementações das notificações enviadas por e-mail.
- `br.com.adacommerce.ecommerce.repository`: Contém as interfaces e implementações dos repositórios de dados.
- `br.com.adacommerce.ecommerce.service`: Contém serviços que encapsulam a lógica de negócios.
- `br.com.adacommerce.ecommerce.validators`: Contém validadores para dados de entrada como e-mail e documento.

### Principais Classes e Enumerações

#### Modelos

- `Cliente`: Representa um cliente no sistema.
- `Produto`: Representa um produto disponível para venda.
- `Pedido`: Representa um pedido feito por um cliente.
- `ItemPedido`: Representa um item dentro de um pedido.
- `StatusPedido`: Enum que representa os possíveis estados de um pedido.

#### Repositórios

- `Repository<T, ID>`: Interface genérica para repositórios.
- Classes como `ClienteRepositoryDB`, `ProdutoRepositoryDB`, etc., que implementam esta interface para diferentes entidades e métodos de persistência (memória, arquivo, banco de dados).

#### Serviços

- `ClienteService`: Serviço para operações relacionadas a clientes.
- `ProdutoService`: Serviço para operações relacionadas a produtos.
- `VendaService`: Serviço para operações relacionadas a pedidos.

#### Controlador

- `ECommerceController`: Controlador que interage com o usuário para executar operações no sistema.

#### Notificações

- `Notificador`: Interface para serviços de notificação.
- `EmailNotificador`: Implementação de `Notificador` que envia e-mails.

#### Validações

- `EmailValidator`, `DocumentoValidator`, `EstoqueValidator`: Classes que proporcionam métodos estáticos para validar e-mails, documentos e estoques.

### Fluxo da Aplicação

1. **Inicialização**: A classe `ECommerceSystem` inicializa o sistema configurando o tipo de persistência e criando os repositórios, serviços e controlador necessários.
2. **Execução Interativa**: `ECommerceController` apresenta um menu interativo onde o usuário pode escolher realizar diferentes operações como cadastro de cliente, produtos, criação de pedidos, adição de itens, entre outros.
3. **Processamento**: Cada ação no menu executa uma série de operações que podem envolver leitura de dados, validações, alterações no estado dos objetos, e interação com o usuário através do console.
4. **Persistência de Dados**: Dependendo da configuração inicial, o sistema pode persistir dados em memória, arquivos ou banco de dados.
5. **Notificações**: Em determinadas ações, o sistema envia notificações por e-mail ao cliente.

### Execução e Uso

- O sistema é iniciado executando o arquivo `ECommerceSystem.java`.
- O usuário interage com o sistema via console, seguindo as instruções e escolhendo opções do menu.
- Para interagir adequadamente com o sistema, o usuário deve seguir as validações e formatos esperados para cada tipo de entrada (ex.: e-mails válidos, CPF/CNPJ válidos).

Este sistema oferece uma visão simplificada mas extensível de um sistema de e-commerce, com potencial para expansão e integração com outras ferramentas e interfaces.