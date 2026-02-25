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

1. Certifique-se de que o **PostgreSQL** está instalado e em execução.
2. Crie a base de dados:
   ```sql
   CREATE DATABASE gerenciamento_de_insumos;
   ```
3. A aplicação está configurada para conectar com as seguintes credenciais padrão:
   - **URL**: `jdbc:postgresql://localhost:5432/gerenciamento_de_insumos`
   - **Usuário**: `postgres`
   - **Senha**: `admin123`
4. Caso suas credenciais sejam diferentes, edite o arquivo `src/main/resources/application.yaml`.

### 2. Popular com dados iniciais (opcional)

Um script SQL de carga inicial está disponível em `src/main/resources/db/seed.sql`. Ele insere 7 matérias-primas e 3 produtos com suas respectivas composições.

Execute via `psql`, pgAdmin, DBeaver ou outra ferramenta de sua preferência:

```bash
psql -h localhost -U postgres -d gerenciamento_de_insumos -f src/main/resources/db/seed.sql
```

### 3. Iniciar a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

### 4. Executar os testes unitários

```bash
./mvnw test
```

## Endpoints da API
![Banco De Dados](https://github.com/user-attachments/assets/b75446d3-96d2-4c37-9556-3958521685a9)
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

## Testes Unitários

O projeto possui **27 testes unitários** cobrindo a lógica de negócio dos services, utilizando JUnit 5 + Mockito:

| Classe | Testes | Cobertura |
|---|---|---|
| `PlanoProducaoServiceTest` | 8 | Algoritmo de otimização, gargalo, desempate, consumos e saldos |
| `MateriaPrimaServiceTest` | 9 | CRUD completo e validações de código duplicado |
| `ProdutoServiceTest` | 10 | CRUD completo, composição com MP repetida e inexistente |
