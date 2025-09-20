# README.md for AdaCommerce E-Commerce Project

## Table of Contents
1. [Project Structure and Folders](#project-structure-and-folders)
2. [Programming Languages Used](#programming-languages-used)
3. [Dependencies and Installation Instructions](#dependencies-and-installation-instructions)
4. [Running the Project and Executing Tests](#running-the-project-and-executing-tests)
5. [Code File Explanations](#code-file-explanations)
6. [Usage Examples](#usage-examples)
7. [Best Practices and Contribution Guidelines](#best-practices-and-contribution-guidelines)

## Project Structure and Folders
The project follows a standard Maven project structure:
- `src/main/java`: Contains all Java source code.
  - `br.com.adacommerce.ecommerce`: Base package.
    - `config`: Configuration settings and enums.
    - `controller`: Controllers to handle user requests.
    - `exceptions`: Custom exceptions.
    - `model`: Domain models.
    - `repository`: Interfaces for data access logic.
    - `service`: Business logic and service layer.
    - `validators`: Input validation logic.
- `src/test/java`: Contains all test code.

## Programming Languages Used
- **Java**: Primary programming language used for the backend service logic.

## Dependencies and Installation Instructions
### Key Dependencies
- **Spring Boot**: Framework for building stand-alone, production-grade Spring-based Applications.
- **Maven**: Dependency management.
- **Lombok**: Automates logging variable definitions.
- **H2 Database Engine**: In-memory database.
- **JUnit**: For unit testing.

### Installation
1. **Clone the repository**:
    ```bash
    git clone https://github.com/user/repo.git
    cd repo
    ```
2. **Build the project using Maven**:
    ```bash
    mvn clean install
    ```
3. **Run the Spring Boot application**:
    ```bash
    mvn spring-boot:run
    ```

## Running the Project and Executing Tests
Run the application using:
```bash
mvn spring-boot:run
```
Execute tests with:
```bash
mvn test
```

## Code File Explanations
### Main Classes
- **`ECommerceSystem.java`**: The main class that starts up the e-commerce service. 
- **`ECommerceController.java`**: Facilitates user interaction through a console-based menu system. Handles operations on customers, products, and orders.
- **`Persistencia.java`**: Enum that lists supported persistence methods (MEMORY, FILE, DATABASE).
- **`ClienteService.java`, `ProdutoService.java`, `VendaService.java`**: Service layers handling business logic related to clients, products, and sales.

### Models
- **`Cliente.java`**, **`Produto.java`**, **`Pedido.java`**: Domain models representing Clients, Products, and Orders, respectively.

### Repositories
- **`ClienteRepository.java`**, **`ProdutoRepository.java`**, **`PedidoRepository.java`**: Interfaces providing data access methods.

## Usage Examples
### Example of Creating an Order
```java
ECommerceController controller = new ECommerceController(new ClienteService(), new ProdutoService(), new VendaService());
controller.iniciar();
```

## Best Practices and Contribution Guidelines
- **Code Style**: Follow existing coding conventions and Java best practices. Use meaningful variable names and comments.
- **Contributions**: Fork the repository, create a feature branch, and submit a Pull Request.
- **Testing**: Write JUnit tests for new code modules.
- **Documentation**: Update README.md with relevant changes.

For detailed instructions on contributing, see `CONTRIBUTING.md`. Please ensure all contributions are documented and include relevant unit tests.

---