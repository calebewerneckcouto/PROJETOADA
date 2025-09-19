# ADA Commerce System README

This is an e-commerce system designed for managing products, customers, and orders within an ADA Commerce platform. It consists of various services and components designed to handle the complex tasks associated with a modern e-commerce business, such as inventory management, orders, reporting, and customer management.

## Project Structure and Folders

- `src/`
  - `br.com.adacommerce.ecommerce/`
    - `controller/` - Contains the `ECommerceController` responsible for managing user interactions.
    - `exceptions/` - Custom exception classes like `BusinessException`, `ValidationException`, etc.
    - `model/` - Domain models/entities such as `Cliente`, `Pedido`, `Produto`.
    - `repository/` - Data access layers, repository interfaces and implementations.
    - `service/` - Business logic handlers like `ClienteService`, `VendaService`, `ProdutoService`.
    - `utils/` - Helper classes such as `DocumentoValidator`, `EmailValidator`.
    - `validators/` - Validators like `EstoqueValidator`.
- `resources/` - Configuration files and resources that are used within the application.
- `docs/` - Additional documentation including setup guidelines and API description.
- `tests/` - Test files for unit and integration tests.

## Programming Languages Used

- **Java** - The primary programming language used for building the application.

## Dependencies and Installation Guide

### Required Tools:

- Java Development Kit (JDK) - Version 11 or higher.
- Apache Maven - Dependency management and build tool.

### Libraries:

- **Spring Framework** - For Dependency Injection.
- **Hibernate ORM & JPA** - For database operations.
- **iText** for PDF generation.
- **JavaMail API** for email handling.

### Steps to Setup:

1. **Clone the repository:**
   ```sh
   git clone https://github.com/yourusername/ada-commerce-system.git
   cd ada-commerce-system
   ```
2. **Build the project:**
   ```sh
   mvn clean install
   ```
3. **Run the application:**
   ```sh
   java -jar target/ecommerce-1.0-SNAPSHOT.jar
   ```

## How to Run the Project and Execute Tests

- **Running the project:** Follow the installation steps above to build and run the project.
- **Executing tests:**
  ```sh
  mvn test
  ```

## Key Code Files Explanation

- `ECommerceController.java`: The main controller class orchestrating user interaction handling, integrating services like `ClienteService`, `ProdutoService`, and managing user input and output through console.
- `ClienteService.java`, `VendaService.java`, `ProdutoService.java`: Services doing the heavy lifting for business logic specific to Clients, Sales, and Products respectively.
- `Repository` interfaces in `br.com.adacommerce.ecommerce.repository` manage data persistence.

## Usage Examples

To operate the e-commerce system, start the application and use the console interface provided by `ECommerceController` to interact with the system such as creating orders, adding products, and managing customers.

## Best Practices and Contribution

**Code Standards:**
- Follow Java naming conventions.
- Use clear and understandable variable names.
- Comment your code where necessary.

**Contribution Guide:**

1. **Fork the repository** and create your branch from `main`.
2. **Make your changes** and test.
3. **Write clear commit messages** describing your changes.
4. **Open a pull request** with the changes.

## Conclusion

The ADA Commerce system is structured to be robust, providing a comprehensive suite of functionalities for an e-commerce platform. Its modular architecture ensures that adding new features or modifying existing ones is as streamlined as possible.