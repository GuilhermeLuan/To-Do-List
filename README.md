<h1 align='center'> Todo List API </h1>

<p align='center'>
Esta é uma API REST de gerenciamento de tarefas desenvolvida em Spring Boot. O sistema permite criar, listar, atualizar e excluir tarefas, incluindo suporte a subtarefas com validações de negócio específicas.
</p>

## 🔧 Ferramentas

- Java 21
- Spring Boot 3.5.4
- MySQL 9.0.1
- Docker
- MapStruct
- JUnit 5
- Maven

## 📍 Endpoints

- `GET /v1/tasks`: Retorna a lista paginada de todas as tarefas com filtros opcionais.
- `POST /v1/tasks`: Cria uma nova tarefa.
- `POST /v1/tasks/{parentId}/subtasks`: Cria uma nova subtarefa vinculada a uma tarefa pai.
- `PUT /v1/tasks/{id}`: Atualiza as informações de uma tarefa específica.
- `PATCH /v1/tasks/{id}/status`: Atualiza apenas o status de uma tarefa específica.
- `DELETE /v1/tasks/{id}`: Exclui uma tarefa específica com base no ID.

## 📄 Modelo de Dados

### `Task`:
- `id` (Long, gerado automaticamente): Identificador único da tarefa
- `title` (String, obrigatório): Título da tarefa
- `description` (String, opcional): Descrição detalhada da tarefa
- `dueDate` (ZonedDateTime, opcional): Data de vencimento da tarefa
- `status` (TaskStatus, opcional): Status atual da tarefa
  - `TO_DO`: Tarefa a fazer
  - `IN_PROGRESS`: Tarefa em progresso
  - `DONE`: Tarefa concluída
- `priority` (Priority, opcional): Prioridade da tarefa
  - `LOW`: Baixa prioridade
  - `MEDIUM`: Média prioridade
  - `HIGH`: Alta prioridade
- `parentTask` (Task, opcional): Referência para a tarefa pai (apenas para subtarefas)
- `isSubTask` (Boolean): Indica se é uma subtarefa
- `subTasks` (List<Task>): Lista de subtarefas associadas

## ⚙️ Funcionalidades da API

A API oferece:

- **Gerenciamento completo de tarefas**: Criação, listagem, atualização e exclusão
- **Sistema de subtarefas**: Criação de subtarefas vinculadas a tarefas principais
- **Validações de negócio**: 
  - Impede aninhamento de subtarefas (subtarefa de subtarefa)
  - Valida conclusão de subtarefas antes de finalizar tarefa principal
- **Filtros e paginação**: Busca por status, prioridade e data de vencimento
- **Ordenação**: Suporte a ordenação por diferentes campos

## 💻 Pré-requisitos

Antes de executar o projeto, certifique-se de ter instalado:

- **Java 21** ou superior
- **Maven 3.6** ou superior
- **Docker** e **Docker Compose**
- **Git**

## 🚀 Como Executar o Projeto

Siga estas etapas para configurar e executar a API em seu ambiente:

### 1. Clone o repositório:

```shell
git clone https://github.com/GuilhermeLuan/todo-list.git
```

### 2. Navegue até o diretório do projeto:

```shell
cd todo-list/
```

### 3. Inicie o banco de dados MySQL com Docker:

```shell
docker compose up -d
```

### 4. Execute a aplicação:

```shell
./mvnw clean install
./mvnw spring-boot:run
```

**No Windows, use:**
```shell
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

A API estará acessível em **http://localhost:8080/**

## 🧪 Executar Testes

Para executar todos os testes unitários:

```shell
./mvnw test
```

## 🌐 Exemplos de Uso

### Criar uma Nova Tarefa

**Método:** `POST`  
**URL:** `http://localhost:8080/v1/tasks`

**Corpo da Solicitação:**
```json
{
  "title": "Develop the authentication API",
  "description": "Implement JWT authentication with protected routes, as per the optional item in the spec.",
  "dueDate": "2025-08-18T23:59:00-03:00[America/Sao_Paulo]",
  "status": "TO_DO",
  "priority": "HIGH"
}
```

### Criar uma Subtarefa

**Método:** `POST`  
**URL:** `http://localhost:8080/v1/tasks/1/subtasks`

**Corpo da Solicitação:**
```json
{
  "title": "Criar endpoint de login",
  "description": "O endpoint deve receber email/senha e retornar um token JWT válido.",
  "status": "TO_DO",
  "priority": "HIGH"
}
```

### Listar Tarefas com Filtros

**Método:** `GET`  
**URL:** `http://localhost:8080/v1/tasks?priority=HIGH&sort=dueDate,asc`

### Atualizar Status de uma Tarefa

**Método:** `PATCH`  
**URL:** `http://localhost:8080/v1/tasks/1/status`

**Corpo da Solicitação:**
```json
{
  "status": "IN_PROGRESS"
}
```

### Atualizar uma Tarefa Completa

**Método:** `PUT`  
**URL:** `http://localhost:8080/v1/tasks/1`

**Corpo da Solicitação:**
```json
{
  "title": "Develop the authentication API",
  "description": "Implement JWT authentication with protected routes, as per the optional item in the spec.",
  "dueDate": "2025-08-24T23:59:00-03:00[America/Sao_Paulo]",
  "status": "IN_PROGRESS",
  "priority": "HIGH"
}
```


## 🔄 Regras de Negócio

1. **Subtarefas**: Não é possível criar subtarefas de outras subtarefas
2. **Conclusão de Tarefas**: Uma tarefa principal só pode ser marcada como "DONE" se todas suas subtarefas estiverem concluídas
3. **Validações**: Título é obrigatório, demais campos são opcionais
4. **Cascata**: Ao excluir uma tarefa principal, todas suas subtarefas também são excluídas


