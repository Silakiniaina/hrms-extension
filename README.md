# HRMS Extension

A **Spring Boot application** that extends the **ERPNext HRMS** module, focusing on all functionalities related to **Employee Salary**.  
This service consumes and enhances ERPNext’s HRMS **APIs**, providing a cleaner way to manage payroll and integrate with external applications.

---

## Features
- Spring Boot microservice to extend **ERPNext HRMS**
- Full support for **Employee Salary** workflows:
  - Salary Structure
  - Salary Slip
  - Payroll processing
- REST API exposure for salary-related operations
- Designed for easy integration with:
  - Payroll applications
  - Financial dashboards
  - Third-party HR systems

---

## Requirements
- **Java 17+**
- **Maven 3.8+**
- **ERPNext** (v13+ recommended) running with HRMS module enabled
- Access to **ERPNext API** credentials

---

## Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Silakiniaina/hrms-extension.git
   cd hrms-extension
    ```

2. **Build the application**

   ```bash
   mvn clean install
   ```

3. **Run the application**

   ```bash
   java -jar target/hrms-extension-0.0.1-SNAPSHOT.jar
   ```

   The service will start on [http://localhost:8080](http://localhost:8080) by default.

---

## Configuration

Update the `application.yml` or `application.properties` with your ERPNext connection details:

```yaml
erpnext:
  url: http://your-erpnext-instance
  apiKey: your-api-key
  apiSecret: your-api-secret
```

## Project Structure

```
hrms-extension/
├── src/main/java/com/example/hrms/   # Source code
│   ├── controller/                   # REST controllers
│   ├── service/                      # Business logic
│   └── model/                        # Data models
├── src/main/resources/
│   └── application.properties               # Configurations
├── pom.xml                           # Maven build file
└── README.md
```

---

## Development Notes

* Built with **Spring Boot** and integrates with **ERPNext APIs**.
* Intended as a **POC** for extending ERPNext HRMS capabilities externally.
* Can be containerized with Docker for deployment.

---

## Contributing

Contributions are welcome!
You can:

* Add more endpoints for HRMS functionalities
* Improve ERPNext API integration
* Enhance validation and error handling

---

## License

This project is licensed under the MIT License.
