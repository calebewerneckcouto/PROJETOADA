# README.md for the E-Commerce System

This repository contains the implementation of an e-commerce system developed in Java, designed to manage customer interactions, product listings, orders, and reports efficiently. Below is a comprehensive overview of the project's structure, coding languages, dependencies, installation, how to run the project, detailed explanations of the code files, usage examples, and contribution guidelines.

## Project Structure and Directories

```
ecommerce-system/
│
├── src/
│   ├── br.com.adacommerce.ecommerce/
│   │   ├── configure/
│   │   │   └── Persistencia.java
│   │   ├── controller/
│   │   │   └── ECommerceController.java
│   │   ├── exceptions/
│   │   │   ├── BusinessException.java
│   │   │   ├── InsufficientStockException.java
│   │   │   └── ValidationException.java
│   │   ├── model/
│   │   │   ├── Cliente.java
│   │   │   ├── ItemPedido.java
│   │   │   ├── Pedido.java
│   │   │   ├── Produto.java
│   │   │   └── StatusPedido.java
│   │   ├── notifications/
│   │   │   ├── EmailNotificador.java
│   │   │   └── Notificador.java
│   │   ├── repository/
│   │   │   ├── ClienteRepositoryDB.java
│   │   │   ├── ProdutoRepositoryDB.java
│   │   │   ├── Repository.java
│   │   │   └── RepositoryFactory.java
│   │   ├── service/
│   │   │   ├── ClienteService.java
│   │   │   ├── EmailService.java
│   │   │   ├── ProdutoService.java
│   │   │   ├── VendaService.java
│   │   │   └── RelatorioPDFService.java
│   │   └── validators/
│   │       ├── DocumentoValidator.java
│   │       ├── EmailValidator.java
│   │       └── EstoqueValidator.java
│   ├── META-INF/
│   │   └── persistence.xml
│   └── resources/
│       └── logging.properties
│
├── .gitignore
├── build.xml
└── README.md
```

## Programming Languages Used

- **Java**: Used for all backend logic, interactions, database communications, and validation mechanisms.

## Dependencies and Installation

### Dependencies

- **Java SDK**: Version 17 or higher.
- **Apache Ant**: To manage builds and dependencies.
- **JPA (Java Persistence API)**: For ORM-based database management.
- **Apache PDFBox**: For generating PDF reports.
- **JUnit**: For testing purposes.

### Installation

1. **Clone the repository:**

    ```bash
    git clone https://github.com/yourusername/ecommerce-system.git
    cd ecommerce-system
    ```

2. **Build the project:**

    ```bash
    ant build
    ```

3. **Setup Database (Optional):** Configure `src/META-INF/persistence.xml` to connect to your database.

4. **Run migrations (Optional):** Setup initial database schemas if connected to a DB.

## How to Run the Project and Execute Tests

To run the project, execute:

```bash
java -jar build/jar/ECommerceSystem.jar
```

To execute tests:

```bash
ant test
```

## Code File Explanations

**ECommerceController.java:**
- Manages user interactions and ties together services like `ClienteService`, `ProdutoService`, `VendaService`, and `RelatorioPDFService`.

**Persistencia.java:**
- Enum used to determine the type of data persistence (in-memory, file-based, database).

**Validation Classes (EmailValidator, DocumentoValidator, EstoqueValidator):**
- Perform validations on emails, documents (CPF/CNPJ), and stock levels.

**Service Classes (ClienteService, ProdutoService, VendaService):**
- Handle business logic related to customers, products, and orders.

**Repository Classes (ClienteRepositoryDB, ProdutoRepositoryDB):**
- Interface with the database to perform CRUD operations.

**RelatorioPDFService.java:**
- Generates detailed PDF reports for completed orders.

## Examples of Use

1. **Adding a New Customer:**

    ```java
    ClienteService clienteService = new ClienteService();
    Cliente newClient = clienteService.cadastrarCliente("John Doe", "john.doe@example.com", "123.456.789-09");
    ```

2. **Creating an Order:**

    ```java
    Pedido order = vendaService.criarPedido(newClient);
    ```

3. **Generating a Report:**

    ```java
    relatorioPDFService.gerarRelatorio("/path/to/report.pdf", "manager_password");
    ```

## Best Practices and Contribution Guidelines

- **Code Style:** Follow Java coding conventions.
- **Testing:** Write JUnit tests for new features and bug fixes.
- **Documentation:** Update comments and documentation in the README for major changes.
- **Pull Requests:** Use clear, descriptive messages for commits and pull requests.

Contribute by creating a pull request or opening issues for bugs or feature suggestions on GitHub. Detailed contributions guidelines can be found in the `CONTRIBUTING.md` file (to be created).