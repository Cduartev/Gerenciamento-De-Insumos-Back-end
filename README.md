# Gerenciamento de Insumos & Otimização de Produção — API REST

> **Desafio Técnico Full Stack — P&D**

API REST desenvolvida em Java (Spring Boot) como solução para o desafio de P&D (Projedata).

##  O Desafio e Requisitos Atendidos

**Cenário:** Uma fábrica precisa controlar o estoque e decidir o que fabricar para obter o **maior lucro possível** com os insumos disponíveis.

-  **Entidades Mapeadas:** `RawMaterial` (Código, Nome, Quantidade) e `Product` (Código, Nome, Valor, +Lista de Composição).
-  **Otimização de Produção:** Rota dedicada que executa algoritmo de *Backtracking* para sugerir o cenário de **Maior Valor Total de Venda**, priorizando produtos com maior retorno financeiro e resolvendo os conflitos de disputa pelas mesmas matérias-primas.
-  **Stack Obrigatória:** Construído em Java (Spring Boot) lidando com banco de dados relacional (PostgreSQL).
-  **Clean Code e Padrões:** Arquitetura limpa (DDD), 100% programada em Inglês, sem lixos de código e tipagem forte usando `Records`.
- **Segurança e Autenticação**: Proteção de rotas com **Spring Security** e emissão de tokens **JWT (JSON Web Token)**, possuindo um fluxo completo de Login e Registro.
- **CORS Global Configurado**: Configuração de CORS nativa no `SecurityConfig.java` via `CorsConfigurationSource`, permitindo que qualquer front-end (Vue, React, etc.) consuma a API diretamente sem proxy intermediário — incluindo suporte ao preflight `OPTIONS`.
- **Integração Realista (Camada Visual):** Embora seja uma API de Back-end, testável de ponta a ponta com um cliente Front-end moderno tematizado (identidade corporativa visual da Vale) para simulação exata de métricas.
- **Testes Unitários Obrigatórios e Diferenciais**: Cobertura expressiva da lógica do cálculo ótimo e de todas as regras de negócio dos CRUDs usando JUnit 5 e Mockito.

## Tecnologias Utilizadas

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| Persistência | Spring Data JPA / Hibernate |
| Banco de Dados | PostgreSQL |
| Segurança | Spring Security + Auth0 JWT |
| Validação | Bean Validation (Jakarta) |
| Testes | JUnit 5 + Mockito + AssertJ |
| Build | Maven Wrapper |

## Pré-requisitos

- **Java 21** (JDK)
- **PostgreSQL** instalado e rodando localmente

## Como Executar

### 1. Configurar o banco de dados
Usei o Miro para gerar o meu modelo visual!!
![Banco De Dados](https://github.com/user-attachments/assets/b75446d3-96d2-4c37-9556-3958521685a9)
1. Certifique-se de que o **PostgreSQL** está instalado e em execução.
2. Crie a base de dados:
   ```sql
   CREATE DATABASE inventory_management;
   ```
3. A aplicação está configurada para conectar com as seguintes credenciais padrão:
   - **URL**: `jdbc:postgresql://localhost:5432/inventory_management`
   - **Usuário**: `postgres`
   - **Senha**: `admin123`
4. Caso suas credenciais sejam diferentes, edite o arquivo `src/main/resources/application.yaml`.

### 2. Popular com dados iniciais (opcional)

Um script SQL de carga inicial está disponível em `src/main/resources/db/seed.sql`.  Ele insere 1 usuario 7 matérias-primas e 3 produtos com suas respectivas composições.

Execute via `psql`, pgAdmin, DBeaver ou outra ferramenta de sua preferência:

```bash
psql -h localhost -U postgres -d inventory_management -f src/main/resources/db/seed.sql
```

### 3. Iniciar a aplicação

```bash
./mvnw clean spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

### 4. Executar os testes unitários

```bash
./mvnw clean test
```

## Endpoints da API


### Autenticação (JWT) — `/api/v1/auth`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Autentica o usuário e retorna o token JWT |
| `POST` | `/api/v1/auth/register` | Cadastra um novo usuário no sistema |

### Matérias-Primas — `/api/raw-materials`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/raw-materials` | Listar todas as matérias-primas |
| `GET` | `/api/raw-materials/{id}` | Buscar matéria-prima por ID |
| `POST` | `/api/raw-materials` | Cadastrar nova matéria-prima |
| `PUT` | `/api/raw-materials/{id}` | Atualizar matéria-prima existente |
| `DELETE` | `/api/raw-materials/{id}` | Remover matéria-prima |

### Produtos — `/api/products`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/products` | Listar todos os produtos |
| `GET` | `/api/products/{id}` | Buscar produto por ID |
| `POST` | `/api/products` | Cadastrar novo produto com composição |
| `PUT` | `/api/products/{id}` | Atualizar produto existente |
| `DELETE` | `/api/products/{id}` | Remover produto |

### Plano de Produção — `/api/plans-production`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/plans-production/suggest` | Gerar sugestão de plano ótimo de produção |

## Exemplos de Requisição

### Criar Matéria-Prima (Raw Material)

```json
POST /api/raw-materials
{
  "code": "RM-001",
  "name": "Farinha de Trigo",
  "stockQuantity": 50.0000,
  "unitOfMeasurement": "KILOGRAM"
}
```

### Criar Produto (Product)

```json
POST /api/products
{
  "code": "PROD-001",
  "name": "Bolo de Chocolate",
  "price": 45.00,
  "compositionItems": [
    { "rawMaterialId": 1, "requiredQuantity": 2.0000 },
    { "rawMaterialId": 2, "requiredQuantity": 0.5000 }
  ]
}
```

### Sugerir Plano de Produção (Production Plan)

```json
POST /api/plans-production/suggest

{
  "totalSalesValue": 225.00,
  "totalProducedQuantity": 5,
  "suggestedItems": [ ... ],
  "rawMaterialConsumptions": [ ... ],
  "rawMaterialBalances": [ ... ]
}
```

## Algoritmo de Otimização

O serviço `ProductionPlanService` implementa um algoritmo de **busca exaustiva (backtracking)** com **Poda (Branch & Bound)** e **Limite de Iterações** que:

1. **Filtra** apenas produtos com composição válida (`ProductCompositionItem`).
2. **Ordena** produtos por valor decrescente (prioridade aos mais lucrativos).
3. **Explora** combinações de quantidades respeitando o estoque, usando poda matemática para pular caminhos menos lucrativos.
4. **Interrompe** após 50.000 iterações (Safety Guard) para garantir resposta em milissegundos.
5. **Seleciona** a combinação que maximiza o valor total (`totalSalesValue`).

## Estrutura do Projeto

O código-fonte segue o padrão **DDD (Domain-Driven Design) simplificado** e nomenclatura 100% em **Inglês**:

```text
src/main/java/com/project/inventory/
├── controller/          # Endpoints REST (Controllers)
├── domain/
│   ├── entity/          # Entidades JPA (Product, RawMaterial, ProductCompositionItem)
│   ├── enumtype/        # Enums (UnitOfMeasurement, Role)
│   └── repository/      # Spring Data Repositories
├── dto/                 # Request e Response (Records Pattern)
│   ├── auth/            # DTOs de Autenticação
│   ├── product/         # DTOs de Produtos
│   ├── rawmaterial/     # DTOs de Matérias-Primas
│   ├── production/      # DTOs de Ordem de Produção
│   └── planproduction/  # DTOs do Plano de Otimização
├── exception/           # GlobalExceptionHandler (@RestControllerAdvice)
├── mapper/              # Classes de Mapeamento (MapStruct style)
├── security/            # Configurações de Security e JWT
└── service/             # Lógicas de negócio
    ├── optimization/    # ProductionPlanService (Otimização)
    └── (ProductService, RawMaterialService, etc.)
```

## Testes Unitários

O projeto possui **28 testes unitários** cobrindo a lógica de negócio dos services, utilizando JUnit 5 + Mockito com sintaxe BDD:

| Classe | Testes | Cobertura |
|---|---|---|
| `ProductionPlanServiceTest` | 8 | Algoritmo de otimização, gargalo de estoque, desempate de matrizes, consumos e saldos |
| `RawMaterialServiceTest` | 10 | CRUD completo, exceptions e validações de violação de Unique Entity |
| `ProductServiceTest` | 10 | CRUD completo, integridade da composição com MP repetida/inexistente e orphans |
