# NEO-ENERGY — MVP da API (Handoff para o Front-end)

> Documento de integração entre back-end e front-end. Descreve modelo de dados, fluxo de autenticação, todos os endpoints e jornadas principais.
> **Stack do back**: Spring Boot 3 + Spring Security + JPA/Hibernate + PostgreSQL + JWT (HS256).
> **Base URL local**: `http://localhost:8080`.

---

## 1. Visão geral

O **NEO-ENERGY** é um sistema de gestão de uma residência ligada a três tipos de equipamento:

- **Painel Solar** (`Psolar`) — gera energia.
- **Solo** — área monitorada que pode estar seca ou molhada.
- **Irrigador** — equipamento que rega um solo, e que pode ser **ligado/desligado** ao longo do tempo, acumulando consumo de água.

Cada residência é uma **Casa**, que agrega vários painéis solares e vários solos. Cada solo, por sua vez, pode ter vários irrigadores. Cada vez que um irrigador é ligado e depois desligado, o sistema cria uma **Sessão** que registra o período, a duração e quanto de água foi consumido.

**Usuários** se cadastram no sistema, fazem login via JWT, e cada um pode estar associado a uma casa.

---

## 2. Modelo de dados (relacionamentos)

```
Usuario  ──(OneToOne)──▶  Casa
                            │
                            │ (OneToMany)
                            ├─▶ PsolarEntity   (vários painéis por casa)
                            └─▶ SoloEntity     (vários solos por casa)
                                    │
                                    │ (OneToMany)
                                    └─▶ IrrigadorEntity (vários irrigadores por solo)
                                              │
                                              │ (OneToMany)
                                              └─▶ SessaoIrrigador (vários ciclos)
```

**Pontos-chave:**

- Todo `Irrigador` **precisa** pertencer a um `Solo` (FK obrigatória).
- Todo `Psolar` e todo `Solo` **precisam** pertencer a uma `Casa`.
- Um `Usuario` pode ter uma `Casa` (opcional).
- `SessaoIrrigador` é criado automaticamente no `ligar` e fechado no `desligar` — o front **não cria sessão manualmente**.

### Enums

| Enum | Valores |
|---|---|
| `STATUS_OBJETOS` | `LIGADO`, `DESLIGADO`, `EM_MANUNTENCAO` |
| `RoleEnum` | `ADMIN`, `NORMAL`, `PRO` |
| `TiposDoSolo` | `SECO`, `MOLHADO` |

### Tipos comuns nos payloads

- `UUID` → string no formato `"550e8400-e29b-41d4-a716-446655440000"`.
- `BigDecimal` → número (envie como `1.5`, não string).
- `LocalDateTime` → ISO-8601 sem timezone (`"2026-05-29T14:30:00"`).
- `Long` → número inteiro.

---

## 3. Autenticação (JWT)

API **stateless**. O front faz login uma vez, guarda o token e o envia em todas as requisições subsequentes no header `Authorization: Bearer <token>`.

### 3.1 Cadastro de usuário — `POST /usuarios` (público)

**Request**:
```json
{
  "nome": "Pedro Camargo",
  "login": "pedro01",
  "senha": "minhaSenhaForte123",
  "email": "pedro@example.com"
}
```

Todos os campos são obrigatórios. `email` precisa de formato válido. A senha é hashada com **BCrypt** antes de ser salva. A role default é `NORMAL`.

**Response `201 Created`** (`UsuarioRespostaDTO` — **sem senha**):
```json
{
  "id": "uuid...",
  "login": "pedro01",
  "email": "pedro@example.com",
  "nome": "Pedro Camargo",
  "role": "NORMAL",
  "casaId": null,
  "dataCadastro": "2026-05-29T14:30:00",
  "dataAtualizacao": "2026-05-29T14:30:00"
}
```

**Erros**: `409` (login ou email duplicado), `422` (validação).

### 3.2 Login JWT — `POST /auth/login` (público — **usar este**)

**Request** (atenção: o campo é `username`, mas o valor é o `login` do usuário):
```json
{
  "username": "pedro01",
  "senha": "minhaSenhaForte123"
}
```

**Response `200 OK`**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "k8c5p_Vj...",
  "tipo": "Bearer",
  "username": "pedro01",
  "role": "NORMAL"
}
```

- **Duração do access token (JWT)**: **24 horas** (`app.jwt.expiration-ms`).
- **Duração do refresh token**: **7 dias** (`app.jwt.refresh-expiration-ms`).
- **Issuer**: `neo-energy`.
- **Claims extras no JWT**: `id` (UUID do usuário), `role`.

**Erro**: `401` (credenciais inválidas, sem body).

### 3.2.1 Renovação — `POST /auth/refresh` (público)

Quando o access token expirar (front recebe `401`), troque o refresh por um par novo:

**Request**:
```json
{ "refreshToken": "k8c5p_Vj..." }
```

**Response `200 OK`** — mesmo formato do login, com **`token` E `refreshToken` novos** (rotação automática — o refresh antigo é invalidado):
```json
{
  "token": "...novo JWT...",
  "refreshToken": "...novo refresh...",
  "tipo": "Bearer",
  "username": "pedro01",
  "role": "NORMAL"
}
```

**Erros**:
- `404` — refresh token não existe.
- `500` (será refinado pra `401`) — token revogado ou expirado.

> **Importante**: a cada `/refresh`, o refresh token antigo deixa de funcionar. Sempre guarde o novo que veio na resposta.

### 3.2.2 Logout — `POST /auth/logout` (público)

Revoga **todas** as sessões do usuário (todos os refresh tokens ativos dele).

**Request**:
```json
{ "refreshToken": "k8c5p_Vj..." }
```

**Response `204 No Content`** — sem body.

O access token (JWT) **continua válido até expirar** (24h) — JWT é stateless, não dá pra revogar do back. O front deve **descartar localmente** o access token ao fazer logout.

---

### 3.3 Como mandar o token

Em toda requisição protegida:

```
Authorization: Bearer <token>
```

#### Exemplo em `fetch`
```js
// 1. Login
const r = await fetch('http://localhost:8080/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'pedro01', senha: 'minhaSenhaForte123' })
});
const { token } = await r.json();
localStorage.setItem('jwt', token);

// 2. Chamada autenticada
const irrigadores = await fetch('http://localhost:8080/irrigador', {
  headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());
```

#### Exemplo em `curl`
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"pedro01","senha":"minhaSenhaForte123"}'

curl http://localhost:8080/usuarios \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 3.4 Roles e autorização

| Role | Uso |
|---|---|
| `ADMIN` | Acesso administrativo — listar/inspecionar/deletar qualquer usuário |
| `NORMAL` | Usuário comum (default ao cadastrar) |
| `PRO` | Plano pago (reservado, sem regra específica ainda) |

No `@PreAuthorize` os controllers usam `hasRole('ADMIN')` — o Spring prefixa com `ROLE_` automaticamente.

**Falhas de auth retornam:**
- `401 Unauthorized` — sem token, token inválido, token expirado, senha errada.
- `403 Forbidden` — token válido, mas role insuficiente.

---

## 4. Endpoints públicos vs protegidos

| Path | Acesso |
|---|---|
| `POST /auth/**` | Público |
| `POST /usuarios` (cadastro) | Público |
| `GET/PUT/DELETE /usuarios/**` | **ADMIN** apenas |
| Todo o resto | Requer autenticação (qualquer role) |

> ℹ️ **Conhecido**: hoje todas as rotas de `Casa`, `Psolar`, `Solo`, `Irrigador` e `SessaoIrrigador` estão como `permitAll()` no `@PreAuthorize`. Isso é intencional para o MVP — mas o `SecurityConfiguration` exige token mesmo assim (já que `anyRequest().authenticated()`). Resumo prático: **o front precisa do JWT pra tudo, exceto cadastro e login**.

---

## 5. Endpoints por recurso

> Convenções:
> - Erros de validação → `422` com `ErroRespostaDTO` (ver §7).
> - Recurso não encontrado → `404` com mensagem.
> - Conflito (duplicidade) → `409`.
> - IDs em path são sempre `UUID`.

### 5.1 Casa — `/casa`

`CasaDTO` (entrada) é um **record vazio** — pode mandar `{}`. Painéis e solos são gerenciados pelos próprios endpoints.

`CasaRespostaDTO`: `{ id, dataCadastro, dataAtualizacao }`.

| Método | Path | Params/Body | Resposta | Notas |
|---|---|---|---|---|
| POST | `/casa` | body `{}` | `201` + `CasaRespostaDTO` | Cria casa vazia |
| GET | `/casa` | — | `List<CasaRespostaDTO>` | Lista todas |
| GET | `/casa/{id}` | path `id` | `CasaRespostaDTO` | 404 se não achar |
| GET | `/casa/pesquisar` | query `idPainelSolar?`, `idSolo?` | `List<CasaRespostaDTO>` | Filtros opcionais |
| PUT | `/casa/{id}` | path + body `{}` | `CasaRespostaDTO` | No-op funcional |
| DELETE | `/casa/{id}` | path | `204` | |

### 5.2 Painel Solar — `/painel_solar`

`PsolarDTO`:
| Campo | Tipo | Validação |
|---|---|---|
| `nome` | string | `@NotBlank` |
| `energia` | BigDecimal | `@NotNull` |
| `status` | `STATUS_OBJETOS` | `@NotNull` |
| `casaId` | UUID | `@NotNull` — casa precisa existir |

`PsolarRespostaDTO`: `{ id, nome, energiaPsolar, status, casaId, dataCadastro, dataAtualizacao }`.

| Método | Path | Notas |
|---|---|---|
| POST | `/painel_solar` | `201`. 404 se `casaId` não existir |
| GET | `/painel_solar` | Lista |
| GET | `/painel_solar/{id}` | Por id |
| GET | `/painel_solar/pesquisar` | query `status?`, `energiaMin?`, `energiaMax?` |
| PUT | `/painel_solar/{id}` | Body completo |
| PATCH | `/painel_solar/{id}/status` | query `status` (sem body) |
| DELETE | `/painel_solar/{id}` | `204` |

### 5.3 Solo — `/solo`

`SoloDTO`:
| Campo | Tipo | Validação |
|---|---|---|
| `nomeDoSolo` | string | `@NotBlank` |
| `statusSolo` | boolean | (primitivo) |
| `tiposDoSolo` | `TiposDoSolo` | `@NotNull` (`SECO`/`MOLHADO`) |
| `casaId` | UUID | `@NotNull` |

`SoloRespostaDTO`: `{ id, nomeDoSolo, statusSolo, tiposDoSolo, casaId, dataCadastro, dataAtualizacao }`.

Endpoints simétricos aos do painel solar:
- `POST /solo`, `GET /solo`, `GET /solo/{id}`, `GET /solo/pesquisar` (query `statusSolo?`, `plantacoesMin?`, `plantacoesMax?`), `PUT /solo/{id}`, `PATCH /solo/{id}/status?status=true|false`, `DELETE /solo/{id}`.

### 5.4 Irrigador — `/irrigador` ⭐

`IrrigadorDTO`:
| Campo | Tipo | Validação |
|---|---|---|
| `nome` | string | `@NotBlank` |
| `status` | `STATUS_OBJETOS` | `@NotNull` |
| `soloId` | UUID | `@NotNull` — solo precisa existir |

`IrrigadorRespostaDTO`: `{ id, nome, status, agua, tempoTotalLigadoSegundos, soloId, dataCadastro, dataAtualizacao }`.

Os campos **`agua`** e **`tempoTotalLigadoSegundos`** são **acumulados ao longo do tempo** — a cada ciclo de ligar/desligar, eles crescem. **Não precisam ser enviados no POST/PUT** — o back gerencia.

| Método | Path | Notas |
|---|---|---|
| POST | `/irrigador` | `201`. 404 se `soloId` não existir |
| GET | `/irrigador` | Lista |
| GET | `/irrigador/{id}` | Por id |
| GET | `/irrigador/pesquisar` | query `status?`, `aguaMin?`, `aguaMax?` |
| PUT | `/irrigador/{id}` | Body completo |
| PATCH | `/irrigador/{id}/status?status=...` | Troca status manualmente (raro) |
| **POST** | **`/irrigador/{id}/ligar`** | **Body**: `{ "climaApto": boolean }` (`@NotNull`). **Inicia uma sessão se climaApto=true.** 409 se já estava LIGADO ou se `climaApto=false` |
| **POST** | **`/irrigador/{id}/desligar`** | **Fecha a sessão atual e acumula água/duração.** 409 se já estava DESLIGADO |
| DELETE | `/irrigador/{id}` | `204` |

**Regra de negócio (importante pro front):**
- Ao chamar `ligar`, o front precisa enviar `{ "climaApto": true|false }` no body. O back **só inicia a sessão se `climaApto=true`** — se for `false`, retorna `409` com mensagem "Clima não está apto para iniciar a irrigação." e nada é alterado no banco.
- Quem decide o `climaApto` é o **front** (consulta de previsão do tempo, sensor de chuva, etc.). O back só confia e age.
- Ao chamar `ligar` com sucesso, o back cria uma `SessaoIrrigador` com `tempoLigado = agora` e marca o irrigador como `LIGADO`.
- Ao chamar `desligar`, o back fecha a sessão (calcula `duracaoSegundos = agora - tempoLigado`, computa `agua = (duracao em minutos) * 1.5 L/min`), soma esses valores nos acumulados do irrigador, e marca como `DESLIGADO`.
- **Não chame ligar duas vezes seguidas** — retorna 409. Idem para desligar.

### 5.5 Sessões de Irrigador — `/sessoes-irrigador`

`SessaoIrrigadorRespostaDTO`: `{ id, irrigadorId, agua, tempoLigado, tempoDesligado, duracaoSegundos }`.

Quando a sessão está aberta, `tempoDesligado`, `agua` e `duracaoSegundos` ficam `null`.

| Método | Path | Notas |
|---|---|---|
| GET | `/sessoes-irrigador/irrigador/{irrigadorId}` | Histórico completo de sessões |
| GET | `/sessoes-irrigador/irrigador/{irrigadorId}/aberta` | Sessão aberta agora (ou 404) |

> Não há POST/PUT/DELETE aqui — o ciclo de vida é controlado por `POST /irrigador/{id}/ligar` e `/desligar`.

### 5.6 Usuário — `/usuarios`

`UsuarioDTO`:
| Campo | Tipo | Validação |
|---|---|---|
| `nome` | string | `@NotBlank` |
| `login` | string | `@NotBlank` |
| `senha` | string | `@NotBlank` |
| `email` | string | `@NotBlank` + `@Email` |

`UsuarioRespostaDTO` (**nunca expõe senha**): `{ id, login, email, nome, role, casaId, dataCadastro, dataAtualizacao }`.

| Método | Path | Acesso | Notas |
|---|---|---|---|
| POST | `/usuarios` | Público | Cadastro. 409 em duplicidade |
| GET | `/usuarios` | **ADMIN** | Lista todos |
| GET | `/usuarios/{id}` | **ADMIN** | Por id |
| GET | `/usuarios/pesquisar` | **ADMIN** | query `username?`, `role?`, `idCasa?` |
| PUT | `/usuarios/{id}` | Autenticado | Atualiza dados de usuário |
| PATCH | `/usuarios/{id}/role?role=...` | **ADMIN** | Promover/rebaixar role |
| DELETE | `/usuarios/{id}` | **ADMIN** | `204` |

---

## 6. Jornadas principais

### 6.1 Onboarding de usuário
1. `POST /usuarios` com nome, login, email, senha → recebe `UsuarioRespostaDTO`.
2. `POST /auth/login` com `username` e `senha` → recebe `token`.
3. Front guarda o token e segue.

### 6.2 Configurando a residência
1. `POST /casa` com `{}` → recebe `casaId`.
2. `POST /painel_solar` com `casaId` (n vezes).
3. `POST /solo` com `casaId` (n vezes) — define `nomeDoSolo`, `tiposDoSolo` e `statusSolo`.
4. `POST /irrigador` com `soloId` (n vezes por solo) — define `nome` e `status` inicial.

### 6.3 Ciclo de irrigação (o coração do app)
1. Usuário aperta "Ligar".
2. **Front consulta clima** (API de tempo, sensor, ou regra interna) e calcula `climaApto`.
3. Front chama `POST /irrigador/{id}/ligar` com `{ "climaApto": true }` (ou `false`).
   - Se `false`, back retorna **409** sem ligar — UI deve mostrar mensagem ao usuário.
   - Se `true`, back cria sessão e marca irrigador como LIGADO.
4. (tempo passa) — UI pode mostrar timer em tempo real chamando `GET /sessoes-irrigador/irrigador/{id}/aberta`.
5. Usuário aperta "Desligar" → `POST /irrigador/{id}/desligar`.
6. O response já vem com `agua` e `tempoTotalLigadoSegundos` atualizados do irrigador.
7. Histórico fica em `GET /sessoes-irrigador/irrigador/{id}`.

### 6.4 Dashboard de consumo
- Lista de irrigadores: `GET /irrigador` (cada um já traz `agua` acumulada).
- Detalhe de um irrigador: `GET /irrigador/{id}` + `GET /sessoes-irrigador/irrigador/{id}` (histórico de ciclos).
- Filtros por consumo: `GET /irrigador/pesquisar?aguaMin=...&aguaMax=...`.

---

## 7. Formato de erro padrão

Todas as exceções tratadas devolvem este JSON:

```json
{
  "status": 422,
  "mensagem": "Erro de validação",
  "erros": [
    { "mensagem": "Senha obrigatória", "campo": "senha" },
    { "mensagem": "Email inválido", "campo": "email" }
  ]
}
```

| Status | Quando |
|---|---|
| `404` | Recurso não encontrado (irrigador, solo, casa, usuário…) |
| `409` | Duplicidade (`login` ou `email` já existem) ou estado inválido (`ligar` em algo já ligado) |
| `422` | Validação de payload (`@NotBlank`, `@NotNull`, `@Email` falharam). Vem com lista `erros` |
| `401` | Sem token, token inválido, token expirado, senha errada no login |
| `403` | Token válido mas role insuficiente |
| `500` | Erro inesperado — `mensagem` carrega o nome da exceção (modo diagnóstico) |

**Recomendação pro front**: interceptor HTTP global que:
- Em `401`: limpa o `jwt` do storage e redireciona pro login.
- Em `422`: mostra os campos com erro (já vêm prontos com `mensagem` + `campo`).
- Em `5xx`: toast de erro genérico.

---

## 8. Observações técnicas

### 8.1 CORS
**Configurado para dev.** Origens aceitas:
- `http://localhost:3000` e `http://127.0.0.1:3000` (CRA padrão)
- `http://localhost:5173` e `http://127.0.0.1:5173` (Vite padrão)
- `http://localhost:8080` (mesmo origem)

Métodos: `GET, POST, PUT, PATCH, DELETE, OPTIONS`. Headers expostos: `Authorization`, `Location` (útil pra ler o id do recurso recém-criado).

Em produção isso vai mudar — o back vai aceitar só o domínio final do front.

### 8.2 CSRF
**Desabilitado.** Não precisa mandar token CSRF.

### 8.3 Cabeçalhos
- `Content-Type: application/json` em todos os POSTs/PUTs.
- `Authorization: Bearer <token>` em tudo que não for `/auth/**` ou `POST /usuarios`.

### 8.4 Configuração do segredo JWT (referência)
Em `application.yaml`:
```yaml
app:
  jwt:
    secret: <base64>
    expiration-ms: 86400000   # 24h
    issuer: neo-energy
```
Em produção o secret virá de env var. **Rotação do secret invalida todos os tokens vivos** → usuários precisam relogar. O interceptor de `401` já cobre.

### 8.5 Banco
- PostgreSQL local em `localhost:5432/neoenergy`, user/pass `postgres/postgres`.
- `ddl-auto: update` → o schema é gerado/atualizado pelo Hibernate.
- Timestamps usam timezone `America/Sao_Paulo`.

---

## 9. Pendências conhecidas (heads-up pro front)

1. **`PUT /usuarios/{id}` aceita qualquer usuário autenticado** — hoje qualquer um logado consegue editar qualquer perfil. Restrição "só pode editar a si mesmo" virá numa próxima iteração; front deve fazer essa checagem do lado cliente por enquanto.
2. **Acumulado de irrigador é só somatório** — não tem "zerar contador" ainda; se precisar, abrir issue.
3. **Refresh token expirado retorna 500** — vai ser refinado pra `401` num próximo ajuste. Por enquanto trate qualquer erro de `/auth/refresh` como "precisa relogar".

---

## 10. Resumo rápido — Cheat sheet

```
# Cadastro + login
POST /usuarios          { nome, login, senha, email }
POST /auth/login        { username, senha }    →  { token, refreshToken, tipo, username, role }
POST /auth/refresh      { refreshToken }       →  { token, refreshToken, tipo, username, role }
POST /auth/logout       { refreshToken }       →  204

# Em toda chamada protegida:
Authorization: Bearer <token>

# Setup da residência
POST /casa              {}                     →  { id }
POST /painel_solar      { nome, energia, status, casaId }
POST /solo              { nomeDoSolo, statusSolo, tiposDoSolo, casaId }
POST /irrigador         { nome, status, soloId }

# Ciclo de irrigação
POST /irrigador/{id}/ligar      { climaApto: true|false }
POST /irrigador/{id}/desligar
GET  /sessoes-irrigador/irrigador/{id}
GET  /sessoes-irrigador/irrigador/{id}/aberta
```

### Fluxo de token recomendado pro front
1. **Login** → guarda `token` e `refreshToken`.
2. Toda request usa `Authorization: Bearer <token>`.
3. Se receber `401`, faz `POST /auth/refresh` com `refreshToken` → atualiza ambos no storage e refaz a request original.
4. Se o refresh também falhar → manda pro login.
5. **Logout** → chama `POST /auth/logout` com `refreshToken` e limpa storage local.

---

*Documento mantido junto ao código em `docs/MVP-API.md`. Qualquer dúvida ou inconsistência, abrir issue.*
