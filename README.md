# Finanças Web

App web de controle de ganhos para entregadores e motoristas de aplicativos: registre ganhos por corrida (valor, plataforma, data), controle despesas por categoria e acompanhe o saldo do mês.

## Stack

- **Backend:** Java 25, Spring Boot (MVC), JDBC com pool HikariCP, MySQL
- **Frontend:** HTML, CSS e JavaScript (servido como conteúdo estático pelo Spring)
- **Segurança:** senhas com BCrypt, sessões com cookie HttpOnly/SameSite=strict, tokens de recuperação de senha com hash SHA-256
- **Infra:** Dockerfile multi-stage, GitHub Actions

## Como rodar localmente

Pré-requisitos: JDK 25 e um MySQL com o banco `app_financas` criado.

1. Execute as migrations em `database/migrations/` (na ordem numérica) para criar/ajustar as tabelas.
2. Configure as variáveis de ambiente:

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `DB_URL` | JDBC do MySQL | `jdbc:mysql://localhost:3306/app_financas` |
| `DB_USER` | usuário do banco | `root` |
| `DB_PASSWORD` | senha do banco | vazio |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP para recuperação de senha | Gmail/587 |
| `APP_BASE_URL` | URL pública do app (links de e-mail) | `http://localhost:8080` |
| `PORT` | porta do servidor | `8080` |

3. Rode com Maven Wrapper:

```bash
./mvnw spring-boot:run
```

4. Acesse `http://localhost:8080`.

## Docker

```bash
docker build -t financas-web .
docker run -p 8080:8080 \
  -e DB_URL='jdbc:mysql://host:3306/app_financas' \
  -e DB_USER=root \
  -e DB_PASSWORD=senha \
  financas-web
```

Health check: `GET /actuator/health`.

## Estrutura

```
src/main/java/com/app/financas/
├── config/       # interceptor de autenticação, handler de erros
├── controller/   # REST controllers (auth, ganhos, despesas)
├── dao/          # acesso ao banco via JDBC (DataSource/HikariCP)
├── model/        # entidades
└── service/      # recuperação de senha por e-mail
src/main/resources/static/  # frontend (html, css, js)
database/migrations/        # scripts SQL versionados
```

## Testes

```bash
./mvnw verify
```

Os testes usam H2 em memória, sem precisar de MySQL.

## Licença

MIT
