# 💰 Carteira Crypto API

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

Uma API RESTful robusta para gerenciamento de portfólio de criptomoedas, capaz de calcular o valor total dos ativos em tempo real (BRL, USD, EUR) integrando-se com a API da CoinGecko.

O projeto foca em arquitetura limpa, segurança, performance e boas práticas de engenharia de software.

## 🚀 Funcionalidades Principais

* **Gestão de Usuários:** Cadastro e autenticação segura.
* **Autenticação & Segurança:** Implementação completa de **Spring Security** com **JWT (JSON Web Tokens)** stateless.
* **Gestão de Portfólio:** Adicionar e remover moedas de uma carteira pessoal.
* **Cotação em Tempo Real:** Integração com a API externa **CoinGecko**.
* **Performance:** Sistema de **Cache** (`@Cacheable`) para evitar múltiplas chamadas à API externa e contornar rate limits.
* **Cálculos Monetários:** Uso de `BigDecimal` para precisão financeira e conversão automática para Real, Dólar e Euro.
* **Separação de Papéis:** Arquitetura de serviços segregada entre **User** e **Admin**.

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 17
* **Framework:** Spring Boot 3
* **Banco de Dados:** MySQL 8
* **Containerização:** Docker & Docker Compose
* **Segurança:** Spring Security, BCrypt, JWT
* **Testes:** JUnit 5, Mockito, MockMvc (Integração)
* **Ferramentas:** Lombok, Maven

## 🏗️ Arquitetura e Decisões de Design

* **Injeção de Dependência:** Uso estrito de IoC para evitar acoplamento e facilitar testes (sem uso de métodos estáticos para configurações).
* **DTO Pattern:** Uso de Records (`RequestDTO` e `ResponseDTO`) para separar a camada de persistência da camada de apresentação e evitar ciclos de JSON.
* **Tratamento Global de Erros:** Uso de `@ControllerAdvice` para padronizar respostas de erro (404, 400, 401, 409) em toda a API.
* **Strategy de Segurança:** Filtro JWT customizado (`OncePerRequestFilter`) que intercepta requisições e popula o `SecurityContext`.

## 🐳 Como Rodar (Docker)

Basta ter o Docker instalado.

1.  Clone o repositório:
    ```bash
    git clone [https://github.com/seu-usuario/seu-repo.git](https://github.com/seu-usuario/seu-repo.git)
    cd seu-repo
    ```

2.  Suba a aplicação e o banco de dados com um único comando:
    ```bash
    docker-compose up --build
    ```

3.  A API estará disponível em: `http://localhost:8080`

## 🧪 Testes

O projeto possui uma suíte de testes abrangente cobrindo as camadas críticas:

* **Testes Unitários (Service Layer):** Testam a lógica de negócio, cálculos financeiros e validações usando `Mockito` para isolar dependências externas.
* **Testes de Integração (Controller Layer):** Usam `@SpringBootTest` e `MockMvc` com um banco H2 em memória para validar o fluxo completo, incluindo segurança (JWT), serialização JSON e persistência.

Para rodar os testes:
```bash
mvn test
