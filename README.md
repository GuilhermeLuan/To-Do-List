<h1 align='center'> Todo List API </h1>

<p align='center'>
Esta é uma API REST de gerenciamento de tarefas desenvolvida em Spring Boot. O sistema permite criar, listar, atualizar e excluir tarefas, incluindo suporte a subtarefas com validações de negócio específicas. A API implementa autenticação JWT para garantir que cada usuário acesse apenas suas próprias tarefas.
</p>

## Ferramentas

- Java 21
- Spring Boot 3
- MySQL
- Docker
- MapStruct
- JUnit 5
- Maven
- Spring Security
- JWT (JSON Web Tokens)
- Swagger/OpenAPI 3

## Autenticação

A API utiliza autenticação JWT (JSON Web Token) para proteger os endpoints de tarefas. Todos os endpoints de gerenciamento de tarefas requerem autenticação válida.

### Endpoints de Autenticação:
- `POST /auth/login`: Realiza login e retorna token JWT
- `POST /auth/register`: Registra novo usuário no sistema

### Como Usar:
1. Registre um usuário ou faça login para obter o token JWT
2. Inclua o token no header `Authorization: Bearer {token}` nas requisições
3. Cada usuário só pode acessar suas próprias tarefas

## Endpoints

### Autenticação
- `POST /auth/login`: Autentica usuário e retorna token JWT
- `POST /auth/register`: Registra novo usuário no sistema

### Tarefas (Requer Autenticação)
- `GET /v1/tasks`: Retorna a lista paginada de tarefas do usuário autenticado com filtros opcionais
- `POST /v1/tasks`: Cria uma nova tarefa para o usuário autenticado
- `POST /v1/tasks/{parentId}/subtasks`: Cria uma nova subtarefa vinculada a uma tarefa pai
- `PUT /v1/tasks/{id}`: Atualiza as informações de uma tarefa específica (apenas do próprio usuário)
- `PATCH /v1/tasks/{id}/status`: Atualiza apenas o status de uma tarefa específica (apenas do próprio usuário)
- `DELETE /v1/tasks/{id}`: Exclui uma tarefa específica com base no ID (apenas do próprio usuário)

## Modelo de Dados

### `User`:
- `id` (Long, gerado automaticamente): Identificador único do usuário
- `login` (String, obrigatório): Nome de usuário único
- `password` (String, obrigatório): Senha criptografada com BCrypt
- `role` (UserRole, obrigatório): Papel do usuário no sistema
  - `USER`: Usuário comum
  - `ADMIN`: Administrador do sistema

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
- `user` (User, obrigatório): Usuário proprietário da tarefa
- `parentTask` (Task, opcional): Referência para a tarefa pai (apenas para subtarefas)
- `isSubTask` (Boolean): Indica se é uma subtarefa
- `subTasks` (List<Task>): Lista de subtarefas associadas

## Funcionalidades da API

A API oferece:

- **Sistema de autenticação completo**: Login, registro e proteção JWT
- **Isolamento de dados por usuário**: Cada usuário acessa apenas suas tarefas
- **Validação de propriedade**: Verificação automática se a tarefa pertence ao usuário
- **Gerenciamento completo de tarefas**: Criação, listagem, atualização e exclusão
- **Sistema de subtarefas**: Criação de subtarefas vinculadas a tarefas principais
- **Validações de negócio**: 
  - Impede aninhamento de subtarefas (subtarefa de subtarefa)
  - Valida conclusão de subtarefas antes de finalizar tarefa principal
  - Previne acesso a tarefas de outros usuários
- **Filtros e paginação**: Busca por status, prioridade e data de vencimento
- **Ordenação**: Suporte a ordenação por diferentes campos
- **Documentação interativa**: Interface Swagger para testar endpoints
- **Segurança avançada**: Criptografia de senhas e tokens JWT seguros

## Pré-requisitos

Antes de executar o projeto, certifique-se de ter instalado:

- **Java 21** ou superior
- **Maven 3.6** ou superior
- **Docker** e **Docker Compose**
- **Git**

## Como Executar o Projeto

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
docker compose up
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

## Documentação da API

A documentação da API está disponível através do Swagger UI:

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## Executar Testes

Para executar todos os testes unitários:

```shell
./mvnw test
```

## Exemplos de Uso

### Registrar um Novo Usuário

**Método:** `POST`  
**URL:** `http://localhost:8080/auth/register`

**Corpo da Solicitação:**
```json
{
  "login": "novoUsuario",
  "password": "minhasenhasegura123",
  "role": "USER"
}
```

### Fazer Login

**Método:** `POST`  
**URL:** `http://localhost:8080/auth/login`

**Corpo da Solicitação:**
```json
{
  "login": "novoUsuario",
  "password": "minhasenhasegura123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Criar uma Nova Tarefa (Autenticado)

**Método:** `POST`  
**URL:** `http://localhost:8080/v1/tasks`  
**Headers:** `Authorization: Bearer {seu-token-jwt}`

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

### Criar uma Subtarefa (Autenticado)

**Método:** `POST`  
**URL:** `http://localhost:8080/v1/tasks/1/subtasks`  
**Headers:** `Authorization: Bearer {seu-token-jwt}`

**Corpo da Solicitação:**
```json
{
  "title": "Criar endpoint de login",
  "description": "O endpoint deve receber email/senha e retornar um token JWT válido.",
  "status": "TO_DO",
  "priority": "HIGH"
}
```

### Listar Tarefas com Filtros (Autenticado)

**Método:** `GET`  
**URL:** `http://localhost:8080/v1/tasks?priority=HIGH&sort=dueDate`  
**Headers:** `Authorization: Bearer {seu-token-jwt}`

### Atualizar Status de uma Tarefa (Autenticado)

**Método:** `PATCH`  
**URL:** `http://localhost:8080/v1/tasks/1/status`  
**Headers:** `Authorization: Bearer {seu-token-jwt}`

**Corpo da Solicitação:**
```json
{
  "status": "IN_PROGRESS"
}
```

### Atualizar uma Tarefa Completa (Autenticado)

**Método:** `PUT`  
**URL:** `http://localhost:8080/v1/tasks/1`  
**Headers:** `Authorization: Bearer {seu-token-jwt}`

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


## Regras de Negócio

### Autenticação e Segurança
1. **Registro**: Não é possível registrar usuários com o mesmo login
2. **Autenticação**: Todos os endpoints de tarefas requerem token JWT válido
3. **Isolamento**: Usuários só podem acessar suas próprias tarefas
4. **Propriedade**: Operações (atualizar, excluir) verificam se a tarefa pertence ao usuário

### Gerenciamento de Tarefas
1. **Subtarefas**: Não é possível criar subtarefas de outras subtarefas
2. **Conclusão de Tarefas**: Uma tarefa principal só pode ser marcada como "DONE" se todas suas subtarefas estiverem concluídas
3. **Validações**: Título é obrigatório, demais campos são opcionais
4. **Cascata**: Ao excluir uma tarefa principal, todas suas subtarefas também são excluídas

## Segurança

- **Tokens JWT**: Assinados com chave secreta para verificação de integridade
- **Validação de Propriedade**: Automática em todas as operações de tarefa
- **Isolamento de Dados**: Usuários não conseguem acessar dados de outros usuários
