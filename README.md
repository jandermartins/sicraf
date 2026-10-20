# SICRAF — Backend

API REST do **SICRAF (Sistema de Crateús Para Gestão de Frotas)**, desenvolvida para o Governo Municipal de Crateús (CE).

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-red)

---

## 📋 Sobre o Projeto

O SICRAF é um sistema de gestão de frota municipal que centraliza o controle de veículos, motoristas, abastecimentos, manutenções e ocorrências. Este repositório contém o **backend** da aplicação, uma API RESTful construída com **Spring Boot 3.x** e **Java 21**.

### 🎯 Problema Resolvido

A gestão de frota do município era feita por planilhas e papel, gerando:
- Falta de visibilidade da operação
- Manutenções corretivas caras
- Multas por documentos vencidos
- Ausência de indicadores de custo

### ✅ Solução

Uma API centralizada que oferece:
- Cadastro de veículos, motoristas e tipos de manutenção
- Check-list digital obrigatório
- Controle de abastecimento com cálculo de consumo
- Ordens de serviço com workflow
- Alertas automáticos de manutenção preventiva
- Dashboard de KPIs e relatórios gerenciais

---

## 🛠️ Stack Técnica

| Camada | Tecnologia |
|--------|-----------|
| **Linguagem** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.x |
| **Segurança** | Spring Security + JWT (jjwt) |
| **Persistência** | Spring Data JPA + Hibernate 6 |
| **Banco de Dados** | PostgreSQL (Supabase) |
| **Build** | Gradle 8.x |
| **Documentação** | SpringDoc OpenAPI (Swagger UI) |
| **Utilitários** | Lombok, Jakarta Validation, Spring Scheduling |

---

## 📁 Estrutura do Projeto

src/main/java/br/ce/crateus/fpo/sicraf/
├── application/
│ ├── config/ → SecurityConfig, ApplicationConfig
│ ├── filter/ → JwtAuthenticationFilter
│ ├── resource/ → Controllers REST + GlobalExceptionHandler
│ └── scheduler/ → AlertaManutencaoScheduler
├── domain/
│ ├── model/ → Entidades JPA
│ ├── dto/
│ │ ├── request/ → DTOs de entrada
│ │ └── response/ → DTOs de saída
│ ├── service/ → Regras de negócio + JwtService
│ └── exception/ → BusinessException, ResourceNotFoundException
└── infrastructure/
└── repository/ → Spring Data JPA Repositories

text

---

## 🚀 Como Executar

### Pré-requisitos

- Java 21 ([Temurin](https://adoptium.net/))
- Gradle 8.x (ou usar o wrapper `./gradlew`)
- Conta no [Supabase](https://supabase.com/) (ou PostgreSQL local)

### 1. Clonar o repositório

```bash
git clone https://github.com/seu-usuario/sicraf.git
cd sicraf
2. Configurar variáveis de ambiente
Crie um arquivo .env ou configure no application.properties:

properties
spring.datasource.url=${SUPABASE_DB_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

jwt.secret=SuaChaveSuperSecretaComPeloMenos32CaracteresAqui!
jwt.access-token-expiration-ms=900000
jwt.refresh-token-expiration-ms=604800000
Defina a variável SUPABASE_DB_URL com a string do Session Pooler (porta 5432):

bash
export SUPABASE_DB_URL='jdbc:postgresql://aws-0-[região].pooler.supabase.com:5432/postgres?user=postgres.[PROJECT_REF]&password=[SENHA]&sslmode=require'
3. Executar a aplicação
bash
./gradlew bootRun
A API estará disponível em http://localhost:8080.

4. Acessar a documentação
Swagger UI: http://localhost:8080/swagger-ui.html

OpenAPI JSON: http://localhost:8080/v3/api-docs

🔐 Autenticação
A API usa JWT com access token de curta duração e refresh token em cookie HttpOnly.

Endpoints de Autenticação
Método	Endpoint	Descrição
POST	/api/auth/login	Login (retorna JWT)
POST	/api/auth/refresh	Renova token via cookie
POST	/api/auth/logout	Remove cookie
POST	/api/auth/seed	Cria admin (dev)
Perfis de Acesso
Perfil	Permissões
GESTOR	Acesso total
COORDENADOR	Veículos, motoristas, operação, relatórios
MOTORISTA	Check-list, abastecimento, ocorrências
MECANICO	Tipos de manutenção, ordens de serviço, alertas
📡 Endpoints Principais
Cadastros Base
Método	Endpoint	Descrição
GET	/api/veiculos	Lista com filtros e paginação
POST	/api/veiculos	Cadastra veículo
PUT	/api/veiculos/{id}	Atualiza veículo
DELETE	/api/veiculos/{id}	Remove veículo
GET	/api/motoristas	Lista motoristas
POST	/api/motoristas	Cadastra motorista
GET	/api/motoristas/alertas/cnh-vencendo	CNH vencendo em 30 dias
GET	/api/tipos-manutencao	Lista tipos de manutenção
Operação Diária
Método	Endpoint	Descrição
GET	/api/checklists	Lista check-lists
POST	/api/checklists	Cria check-list
PATCH	/api/checklists/{id}/concluir	Conclui check-list
GET	/api/abastecimentos	Lista abastecimentos
POST	/api/abastecimentos	Registra abastecimento
GET	/api/ocorrencias	Lista ocorrências
POST	/api/ocorrencias	Lança ocorrência
Manutenção
Método	Endpoint	Descrição
GET	/api/ordens-servico	Lista ordens de serviço
POST	/api/ordens-servico	Cria OS
PATCH	/api/ordens-servico/{id}/concluir	Conclui OS
GET	/api/alertas-manutencao/pendentes	Alertas pendentes
POST	/api/alertas-manutencao/verificar	Força verificação
GET	/api/historico/veiculo/{id}	Histórico por veículo
Relatórios
Método	Endpoint	Descrição
GET	/api/dashboard	KPIs gerais
GET	/api/relatorios/custo-km	Custo por KM
GET	/api/relatorios/multas-motorista	Multas por motorista
🧪 Testes
bash
# Rodar todos os testes
./gradlew test

# Rodar com relatório de cobertura
./gradlew jacocoTestReport
O relatório HTML fica em build/reports/jacoco/test/html/index.html.

Cobertura Atual
Service	Testes
ChecklistService	✅
AbastecimentoService	✅
OrdemServicoService	✅
VeiculoService	✅
MotoristaService	✅
AlertaManutencaoService	✅
RelatorioCustoKmService	✅
RelatorioMultasService	✅
🐳 Docker
Build da imagem
bash
docker build -t sicraf-backend .
Executar
bash
docker run -p 8080:8080 \
  -e SUPABASE_DB_URL="jdbc:postgresql://..." \
  sicraf-backend
☁️ Deploy (Fly.io)
bash
# Instalar flyctl
iwr https://fly.io/install.ps1 -useb | iex

# Login
fly auth login

# Deploy
fly deploy --remote-only

# Ver logs
fly logs
O Dockerfile multi-stage já está configurado para build remoto.

📚 Documentação Adicional
Documento	Descrição
TAP	Termo de Abertura do Projeto
PGP	Plano de Gerenciamento de Projetos
DAS	Documento de Arquitetura de Software
ERS	Especificação de Requisitos
🤝 Contribuindo
Faça um fork do projeto

Crie uma branch (git checkout -b feature/nova-funcionalidade)

Commit suas mudanças (git commit -m 'feat: adiciona nova funcionalidade')

Push para a branch (git push origin feature/nova-funcionalidade)

Abra um Pull Request

Convenção de Commits
feat: Nova funcionalidade

fix: Correção de bug

docs: Documentação

test: Testes

refactor: Refatoração

📄 Licença
Este projeto é de uso acadêmico e institucional. Todos os direitos reservados ao Governo Municipal de Crateús.

👥 Autores
Ivila Costa da Silva — Gestora do Projeto

[Nome] — Arquiteto de Soluções

[Nome] — Desenvolvedor Backend

[Nome] — Desenvolvedor Frontend

📞 Contato
Prefeitura de Crateús: site oficial

Repositório: GitHub

