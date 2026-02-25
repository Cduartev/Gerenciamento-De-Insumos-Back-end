# Gerenciamento de Insumos — API REST

API REST para gerenciamento de insumos industriais com otimização de plano de produção. Desenvolvida com **Spring Boot 3.5**, a aplicação permite cadastrar matérias-primas e produtos, definir composições (BOM) e gerar automaticamente o plano de produção que **maximiza o valor total de venda** respeitando o estoque disponível.

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5.11 |
| Persistência | Spring Data JPA / Hibernate |
| Banco de Dados | PostgreSQL 17 |
| Validação | Bean Validation (Jakarta) |
| Testes | JUnit 5 + Mockito + AssertJ |
| Build | Maven Wrapper |
| Infra | Docker Compose |

## Pré-requisitos

- **Java 21** (JDK)
- **Docker** e **Docker Compose** (para o banco de dados)

## Como Executar

### 1. Subir o banco de dados

```bash
docker-compose up -d
```

### 2. Criar a base de dados e popular com dados iniciais (opcional)

Conecte-se ao PostgreSQL e execute o script de seed:

```bash
psql -h localhost -U postgres -d gerenciamento_de_insumos -f src/main/resources/db/seed.sql
```

> A senha padrão é `admin123`.

### 3. Iniciar a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

### 4. Executar os testes

```bash
./mvnw test
```

## Endpoints da API

### Matérias-Primas — `/api/materias-primas`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/materias-primas` | Listar todas as matérias-primas |
| `GET` | `/api/materias-primas/{id}` | Buscar matéria-prima por ID |
| `POST` | `/api/materias-primas` | Cadastrar nova matéria-prima |
| `PUT` | `/api/materias-primas/{id}` | Atualizar matéria-prima existente |
| `DELETE` | `/api/materias-primas/{id}` | Remover matéria-prima |

### Produtos — `/api/produtos`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/produtos` | Listar todos os produtos |
| `GET` | `/api/produtos/{id}` | Buscar produto por ID |
| `POST` | `/api/produtos` | Cadastrar novo produto com composição |
| `PUT` | `/api/produtos/{id}` | Atualizar produto existente |
| `DELETE` | `/api/produtos/{id}` | Remover produto |

### Plano de Produção — `/api/planos-producao`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/planos-producao/sugerir` | Gerar sugestão de plano ótimo de produção |

## Exemplos de Requisição

### Criar Matéria-Prima

```json
POST /api/materias-primas
{
  "codigo": "MP-001",
  "nome": "Farinha de Trigo",
  "quantidadeEstoque": 50.0000,
  "unidadeMedida": "QUILOGRAMA"
}
```

### Criar Produto

```json
POST /api/produtos
{
  "codigo": "PROD-001",
  "nome": "Bolo de Chocolate",
  "valor": 45.00,
  "itensComposicao": [
    { "materiaPrimaId": 1, "quantidadeNecessaria": 2.0000 },
    { "materiaPrimaId": 2, "quantidadeNecessaria": 0.5000 }
  ]
}
```

### Sugerir Plano de Produção

```json
POST /api/planos-producao/sugerir

// Resposta
{
  "valorTotalVenda": 225.00,
  "quantidadeTotalProduzida": 5,
  "itensSugeridos": [ ... ],
  "consumosMateriasPrimas": [ ... ],
  "saldosMateriasPrimas": [ ... ]
}
```

## Algoritmo de Otimização

O serviço `PlanoProducaoService` implementa um algoritmo de **busca exaustiva (backtracking)** que:

1. **Filtra** apenas produtos com composição válida
2. **Ordena** produtos por valor decrescente (prioridade aos mais lucrativos)
3. **Explora** todas as combinações possíveis de quantidades respeitando o estoque
4. **Seleciona** a combinação que maximiza o valor total de venda
5. **Desempata** preferindo a combinação com maior quantidade total produzida

## Estrutura do Projeto

```
src/main/java/com/projeto/gerenciamento/de/insumos/
├── controller/          # Endpoints REST
├── domain/
│   ├── entity/          # Entidades JPA (Produto, MateriaPrima, ItemComposicaoProduto)
│   ├── enumtype/        # Enums (UnidadeMedida)
│   └── repository/      # Repositórios Spring Data
├── dto/                 # DTOs de request e response
├── exception/           # Exceções e tratamento global
├── mapper/              # Mapeamento entidade ↔ DTO
└── service/
    └── otimizacao/      # Algoritmo de otimização do plano de produção
```

## Testes

O projeto possui **27 testes unitários** utilizando JUnit 5 + Mockito:

| Classe | Testes | Cobertura |
|---|---|---|
| `PlanoProducaoServiceTest` | 8 | Algoritmo de otimização, gargalo, desempate, consumos e saldos |
| `MateriaPrimaServiceTest` | 9 | CRUD completo e validações de código duplicado |
| `ProdutoServiceTest` | 10 | CRUD completo, composição com MP repetida e inexistente |
