# Sistema de Gestão de Solicitações de Acesso

Sistema de gerenciamento de solicitações de acesso a módulos corporativos desenvolvido com Spring Boot 3.2.0 e Java 21. O sistema permite que usuários solicitem acesso a módulos específicos, com validação de regras de negócio, controle de departamentos permitidos e gerenciamento de histórico de solicitações.

## Descrição do Projeto

Sistema RESTful para gestão de solicitações de acesso a módulos de sistemas corporativos. Implementa autenticação baseada em tokens, validação de regras de negócio, controle de departamentos permitidos por módulo, gerenciamento de histórico de solicitações e controle de acessos concedidos.

Principais funcionalidades:
- Autenticação de usuários com geração de tokens de sessão
- Criação e gerenciamento de solicitações de acesso a módulos
- Validação de regras de negócio (departamentos permitidos, módulos incompatíveis)
- Renovação e cancelamento de solicitações
- Consulta de módulos disponíveis
- Histórico de alterações de solicitações
- Controle de acessos ativos

## Tecnologias Utilizadas e Versões

- **Java**: 21
- **Spring Boot**: 3.2.0
- **Spring Data JPA**: 3.2.0
- **Spring Security Crypto**: 6.2.0
- **PostgreSQL**: 17-alpine
- **Flyway**: 9.22.3
- **Hibernate**: 6.3.1.Final
- **SpringDoc OpenAPI**: 2.2.0
- **Maven**: 3.9.9
- **JaCoCo**: 0.8.11
- **Docker**: 24.x
- **Docker Compose**: 2.x
- **Nginx**: alpine (load balancer)

## Pré-requisitos

- Docker 24.0 ou superior
- Docker Compose 2.0 ou superior
- Maven 3.9 ou superior (para execução local sem Docker)
- Java 21 (para execução local sem Docker)

## Como Executar Localmente com Docker

1. Clone o repositório:
```bash
git clone <url-do-repositorio>
cd pg17-xboot-j21
```

2. Execute o Docker Compose para subir todos os serviços:
```bash
docker-compose up -d --build
```

Este comando irá:
- Construir as imagens das aplicações Spring Boot
- Subir o banco de dados PostgreSQL na porta 15432
- Executar 3 instâncias da aplicação (app1:8080, app2:8081, app3:8082)
- Configurar o Nginx como load balancer na porta 80
- Executar as migrations do Flyway automaticamente

3. Verifique se os containers estão rodando:
```bash
docker ps
```

4. Acesse a aplicação:
- API principal: http://localhost:80 (através do Nginx)
- Instância 1: http://localhost:8080
- Instância 2: http://localhost:8081
- Instância 3: http://localhost:8082
- Swagger UI: http://localhost:80/swagger-ui.html
- API Docs: http://localhost:80/api-docs

5. Para parar os serviços:
```bash
docker-compose down
```

6. Para parar e remover volumes (limpar banco de dados):
```bash
docker-compose down -v
```

## Como Executar os Testes

### Executar todos os testes:
```bash
mvn clean test
```

### Executar testes com cobertura JaCoCo:
```bash
mvn clean test jacoco:report
```

### Executar testes de uma classe específica:
```bash
mvn test -Dtest=NomeDaClasseTest
```

### Executar testes de um pacote específico:
```bash
mvn test -Dtest=com.pg17xbootj21.service.*Test
```

Os relatórios do Surefire são gerados em: `target/surefire-reports/`

## Como Visualizar Relatório de Cobertura

### Relatório HTML JaCoCo:
Após executar `mvn clean test jacoco:report`, abra o arquivo:
```
target/site/jacoco/index.html
```

### Relatório PDF:
Um relatório PDF consolidado é gerado automaticamente em:
```
reports/relatorio-testes-jacoco.pdf
```

Para gerar o PDF manualmente:
```bash
python3 /tmp/generate_report.py
```

O relatório PDF contém:
- Resumo dos testes (total, falhas, erros, ignorados, tempo de execução)
- Métricas de cobertura (instruções, linhas, branches, complexidade, métodos, classes)
- Referências para relatórios HTML e XML

## Credenciais para Teste

As seguintes credenciais são criadas automaticamente pelas migrations:

### Usuário TI:
- **Email**: admin.ti@empresa.com
- **Senha**: senha123
- **Departamento**: TI

### Usuário Financeiro:
- **Email**: usuario.financeiro@empresa.com
- **Senha**: senha123
- **Departamento**: Financeiro

### Usuário RH:
- **Email**: usuario.rh@empresa.com
- **Senha**: senha123
- **Departamento**: RH

### Usuário Operações:
- **Email**: usuario.operacoes@empresa.com
- **Senha**: senha123
- **Departamento**: Operações

Todas as senhas utilizam hash BCrypt com salt rounds 10.

## Exemplos de Requisições

### 1. Autenticação (Login)

**POST** `/auth/login`

```json
{
  "email": "admin.ti@empresa.com",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "name": "Administrador TI",
  "email": "admin.ti@empresa.com"
}
```

### 2. Listar Módulos Disponíveis

**GET** `/modules`

**Headers:**
```
Authorization: Bearer <token>
```

**Resposta:**
```json
[
  {
    "id": "PORTAL",
    "name": "Portal do Colaborador",
    "description": "Portal de acesso geral ao sistema",
    "active": true
  }
]
```

### 3. Criar Solicitação de Acesso

**POST** `/requests`

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "modules": ["PORTAL", "RELATORIOS"],
  "justification": "Necessário para análise de dados"
}
```

**Resposta:**
```json
{
  "protocol": "SOL-20261126-0001",
  "status": "EM_ANALISE",
  "message": "Solicitação criada com sucesso"
}
```

### 4. Buscar Solicitações

**POST** `/requests/search`

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "page": 0,
  "size": 10,
  "status": "EM_ANALISE"
}
```

### 5. Obter Detalhes de Solicitação

**GET** `/requests/{protocol}`

**Headers:**
```
Authorization: Bearer <token>
```

**Exemplo:**
```
GET /requests/SOL-20261126-0001
```

### 6. Renovar Acesso

**POST** `/requests/{protocol}/renew`

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "requestProtocol": "SOL-20261126-0001"
}
```

### 7. Cancelar Solicitação

**POST** `/requests/{protocol}/cancel`

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "reason": "Motivo detalhado do cancelamento"
}
```

### 8. Verificar Uptime

**GET** `/api/uptime`

**Resposta:**
```json
{
  "status": "ok",
  "uptimeSeconds": 3600,
  "uptimeFormatted": "1h 0m 0s",
  "startTime": "2026-11-26T10:00:00Z"
}
```

## Arquitetura da Solução

### Estrutura de Camadas

O projeto segue o padrão de arquitetura em camadas com separação clara de responsabilidades:

#### 1. Camada de Apresentação (Controller)
**Pacote**: `com.pg17xbootj21.controller`

Responsável por receber requisições HTTP e retornar respostas. Contém:
- `AuthController`: Endpoints de autenticação (`/auth/login`)
- `RequestController`: Endpoints de gerenciamento de solicitações (`/requests/*`)
- `ModuleController`: Endpoints de consulta de módulos (`/modules`)
- `PingController`: Endpoints de health check (`/api/uptime`)

**Responsabilidades**:
- Validação de entrada via `@Valid`
- Extração de tokens de autenticação
- Conversão de DTOs para respostas HTTP
- Tratamento de erros via `GlobalExceptionHandler`

#### 2. Camada de Serviço (Service)
**Pacote**: `com.pg17xbootj21.service`

Contém a lógica de negócio da aplicação:
- `AuthService`: Autenticação de usuários e gerenciamento de sessões
- `RequestService`: Lógica de criação, busca, renovação e cancelamento de solicitações
- `ModuleService`: Consulta e validação de módulos disponíveis
- `AccessService`: Gerenciamento de acessos concedidos
- `BusinessRuleService`: Validação de regras de negócio (departamentos permitidos, módulos incompatíveis)
- `SessionService`: Validação e gerenciamento de tokens de sessão
- `UserService`: Operações relacionadas a usuários

**Responsabilidades**:
- Implementação das regras de negócio
- Orquestração de operações entre múltiplos repositórios
- Validações complexas
- Geração de protocolos e tokens

#### 3. Camada de Persistência (Repository)
**Pacote**: `com.pg17xbootj21.repository`

Interfaces Spring Data JPA para acesso a dados:
- `UserRepository`: Operações com usuários
- `RequestRepository`: Operações com solicitações
- `ModuleRepository`: Operações com módulos
- `AccessRepository`: Operações com acessos
- `RequestHistoryRepository`: Operações com histórico

**Responsabilidades**:
- Abstração de acesso ao banco de dados
- Queries customizadas quando necessário
- Operações CRUD básicas

#### 4. Camada de Modelo (Model)
**Pacote**: `com.pg17xbootj21.model`

Entidades JPA que representam as tabelas do banco:
- `User`: Usuários do sistema
- `Request`: Solicitações de acesso
- `Module`: Módulos disponíveis
- `Access`: Acessos concedidos
- `RequestHistory`: Histórico de alterações de solicitações
- `SessionInfo`: Informações de sessão (em memória)

**Responsabilidades**:
- Mapeamento objeto-relacional
- Validações de integridade
- Relacionamentos entre entidades

#### 5. Camada de DTO (Data Transfer Object)
**Pacote**: `com.pg17xbootj21.dto`

Objetos de transferência de dados para comunicação entre camadas:
- `LoginRequest`, `LoginResponse`: Autenticação
- `CreateRequestRequest`, `CreateRequestResponse`: Criação de solicitações
- `RequestSummaryResponse`, `RequestDetailsResponse`: Consulta de solicitações
- `ModuleResponse`: Resposta de módulos
- `ErrorResponse`: Tratamento de erros
- `PagedResponse`: Respostas paginadas

**Responsabilidades**:
- Desacoplamento entre camadas
- Validação de entrada
- Formatação de saída

#### 6. Camada de Segurança (Security)
**Pacote**: `com.pg17xbootj21.security`

Implementação de segurança:
- `SecurityInterceptor`: Interceptor que valida tokens em requisições protegidas
- `SecurityConfig`: Configuração de segurança
- `HttpSecurityConfig`: Configuração de HTTP security
- `SecurityUtil`: Utilitários de segurança (geração de tokens)

**Responsabilidades**:
- Validação de tokens JWT
- Proteção de endpoints
- Gerenciamento de sessões

#### 7. Camada de Configuração (Config)
**Pacote**: `com.pg17xbootj21.config`

Configurações da aplicação:
- `OpenApiConfig`: Configuração do Swagger/OpenAPI
- `UptimeConfig`: Configuração de monitoramento de uptime
- `SecurityConfig`: Configurações de segurança
- `HttpSecurityConfig`: Configurações HTTP

#### 8. Camada de Utilidades (Util)
**Pacote**: `com.pg17xbootj21.util`

Classes utilitárias:
- `PasswordUtil`: Hash e validação de senhas (BCrypt)
- `SecurityUtil`: Geração e validação de tokens JWT

#### 9. Camada de Exceções (Exception)
**Pacote**: `com.pg17xbootj21.exception`

- `GlobalExceptionHandler`: Tratamento centralizado de exceções

### Separação de Responsabilidades

**Controller**: Recebe requisições HTTP, valida entrada, delega para serviços, retorna respostas HTTP.

**Service**: Contém lógica de negócio, orquestra operações, valida regras, coordena múltiplos repositórios.

**Repository**: Abstrai acesso a dados, executa queries, retorna entidades.

**Model**: Representa estrutura de dados, define relacionamentos, valida integridade.

**DTO**: Transporta dados entre camadas, valida entrada, formata saída.

**Security**: Protege endpoints, valida autenticação, gerencia sessões.

**Config**: Configura componentes do Spring, define beans, ajusta comportamento.

**Util**: Fornece funções auxiliares reutilizáveis, encapsula lógicas comuns.

### Fluxo de Requisição

1. Cliente envia requisição HTTP
2. `SecurityInterceptor` valida token (se endpoint protegido)
3. `Controller` recebe requisição e valida DTO de entrada
4. `Controller` delega para `Service` apropriado
5. `Service` executa lógica de negócio, valida regras
6. `Service` utiliza `Repository` para persistência
7. `Repository` executa operações no banco via JPA
8. Resposta retorna através das camadas
9. `Controller` converte resultado em DTO de resposta
10. Cliente recebe resposta HTTP

### Banco de Dados

O banco utiliza PostgreSQL 17 com migrations gerenciadas pelo Flyway. As migrations estão em `src/main/resources/db/migration/` e seguem o padrão `V{versao}__{descricao}.sql`.

Estrutura principal:
- `users`: Usuários do sistema
- `modules`: Módulos disponíveis
- `module_allowed_departments`: Departamentos permitidos por módulo
- `module_incompatible_modules`: Módulos incompatíveis entre si
- `requests`: Solicitações de acesso
- `request_modules`: Relação entre solicitações e módulos
- `request_history`: Histórico de alterações
- `accesses`: Acessos concedidos

### Infraestrutura

O sistema utiliza Docker Compose para orquestração:
- **PostgreSQL**: Banco de dados na porta 15432
- **App1, App2, App3**: Três instâncias da aplicação (8080, 8081, 8082)
- **Nginx**: Load balancer na porta 80 distribuindo requisições entre as instâncias

### Testes

A suíte de testes utiliza:
- JUnit 5 para estrutura de testes
- Mockito para mocks
- Spring Security Test para testes de segurança
- Instancio para geração de dados de teste
- H2 Database para testes de integração
- JaCoCo para cobertura de código

Cobertura atual: 93.34% de instruções, 91.36% de linhas, 76.45% de branches.

