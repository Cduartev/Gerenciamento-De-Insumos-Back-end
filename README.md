# Gerenciamento de Insumos — API REST

> **Desafio Técnico Full Stack — Projedata**

API REST desenvolvida como solução para o teste prático de P&D da Projedata. A aplicação permite o cadastro de matérias-primas e produtos, a definição de composições (BOM — Bill of Materials) e a geração automática de um **plano de produção otimizado** que maximiza o valor total de venda respeitando o estoque disponível.

## Tecnologias Utilizadas

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| Persistência | Spring Data JPA / Hibernate |
| Banco de Dados | PostgreSQL |
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

Um script SQL de carga inicial está disponível em `src/main/resources/db/seed.sql`. Ele insere 7 matérias-primas e 3 produtos com suas respectivas composições.

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

### Plano de Produção — `/api/production-plans`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/production-plans/suggest` | Gerar sugestão de plano ótimo de produção |

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
POST /api/production-plans/suggest

{
  "totalSalesValue": 225.00,
  "totalProducedQuantity": 5,
  "suggestedItems": [ ... ],
  "rawMaterialConsumptions": [ ... ],
  "rawMaterialBalances": [ ... ]
}
```

## Algoritmo de Otimização

O serviço `ProductionPlanService` implementa um algoritmo de **busca exaustiva (backtracking)** que:

1. **Filtra** apenas produtos com composição válida (`ProductCompositionItem`).
2. **Ordena** produtos por valor decrescente (prioridade aos mais lucrativos).
3. **Explora** todas as combinações possíveis de quantidades respeitando o estoque da `RawMaterial`.
4. **Seleciona** a combinação que maximiza o valor total (`totalSalesValue`).
5. **Desempata** preferindo a combinação com maior quantidade total produzida.

## Estrutura do Projeto

O código-fonte segue o padrão **DDD (Domain-Driven Design) simplificado** e nomenclatura 100% em **Inglês**:

```text
src/main/java/com/project/inventory/
├── controller/          # Endpoints REST (Controllers)
├── domain/
│   ├── entity/          # Entidades JPA (Product, RawMaterial, ProductCompositionItem)
│   ├── enumtype/        # Enums (UnitOfMeasurement)
│   └── repository/      # Spring Data Repositories
├── dto/                 # Request e Response (Records Pattern)
├── exception/           # GlobalExceptionHandler (@RestControllerAdvice)
├── mapper/              # Classes de Mapamento entre DTOs e Entities
└── service/             # Lógicas de negócio (ProductService, RawMaterialService)
    └── optimization/    # ProductionPlanService (Algoritmo Backtracking)
```

## Testes Unitários

O projeto possui **28 testes unitários** cobrindo a lógica de negócio dos services, utilizando JUnit 5 + Mockito com sintaxe BDD:

| Classe | Testes | Cobertura |
|---|---|---|
| `ProductionPlanServiceTest` | 8 | Algoritmo de otimização, gargalo de estoque, desempate de matrizes, consumos e saldos |
| `RawMaterialServiceTest` | 10 | CRUD completo, exceptions e validações de violação de Unique Entity |
| `ProductServiceTest` | 10 | CRUD completo, integridade da composição com MP repetida/inexistente e orphans |
