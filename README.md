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
