# Automanager - Atualização de Segurança (ATVIV)

Este projeto implementa a camada de autenticação e autorização com JSON Web Tokens (JWT) e controle de acesso baseado em perfis (RBAC) para o microserviço automanager.

## Como Executar

### Pré-requisitos
- JDK 17 ou superior
- Maven (ou utilizar o wrapper `./mvnw`)

### Execução
No terminal, execute o comando:
```bash
mvn spring-boot:run
```
Ou utilizando o wrapper no Windows / Linux:
```cmd
.\mvnw.cmd spring-boot:run
```
```bash
./mvnw spring-boot:run
```

A aplicação subirá por padrão na porta `8080`.

---

## Usuários de Demonstração e Perfis Pré-configurados

Na primeira inicialização, um administrador inicial é criado automaticamente (caso ainda não exista). Adicionalmente, dados de teste são carregados:

| Perfil | Usuário | Senha |
|---|---|---|
| ADMINISTRADOR | `admin` | `admin123` |
| GERENTE | `gerente` | `gerente123` |
| VENDEDOR | `vendedor` | `vendedor123` |
| CLIENTE | `cliente` | `cliente123` |

As credenciais do administrador inicial e a chave de assinatura JWT podem ser customizadas via variáveis de ambiente ou no `application.properties`:
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`

---

## Matriz de Permissões

| Perfil | Permissões |
|---|---|
| **ADMINISTRADOR** | Acesso total (CRUD) a empresas, credenciais, usuários, veículos, mercadorias, serviços e vendas. Pode criar/remover qualquer perfil de usuário, inclusive outros administradores. |
| **GERENTE** | CRUD em usuários com perfis GERENTE, VENDEDOR e CLIENTE. CRUD de serviços, vendas e mercadorias. Leitura de veículos e empresas. Proibido de criar ou alterar usuários ADMINISTRADOR e acessar `/credenciais`. |
| **VENDEDOR** | CRUD em usuários com perfil CLIENTE. Leitura de mercadorias e serviços. Criação de vendas atribuídas a ele próprio e leitura das vendas que ele realizou. Não pode criar ou alterar mercadorias, serviços ou usuários de cargos superiores. |
| **CLIENTE** | Leitura restrita ao próprio cadastro e leitura restrita às vendas nas quais é o cliente/comprador. Leitura dos próprios veículos. Sem permissão de escrita em vendas, estoques ou cadastros gerais. |

---

## Suposições e Regras de Negócio Adotadas

1. **Veículos**: Seguem a propriedade do usuário dono. Administrador, Gerente e Vendedor realizam gerenciamento completo; o Cliente só visualiza seus próprios veículos.
2. **Empresas e Credenciais**: Apenas o perfil ADMINISTRADOR tem autorização para criar, alterar e gerenciar credenciais e dados empresariais.
3. **Criação de Vendas**: Ao criar uma venda, se o usuário logado possuir perfil VENDEDOR, a venda é obrigatoriamente vinculada ao ID do vendedor extraído do token JWT, impedindo que um vendedor registre vendas em nome de outro.
4. **Tratamento de Violações de Propriedade**: Tentativas de clientes ou vendedores de acessar recursos ou cadastros pertencentes a terceiros retornam status HTTP 403 Forbidden.
5. **Autenticação Stateless**: Todas as rotas (exceto o endpoint público `POST /login`) exigem o cabeçalho `Authorization: Bearer <token>`. Requisições sem token ou com token inválido respondem com 401 Unauthorized.

---

## Arquivo de Testes

Os testes manuais de todos os perfis e dos cenários de autorização e negação estão disponíveis no arquivo [requisicoes.http](file:///C:/Users/T13r/Downloads/atviv-autobots-microservico-spring-master/atviv-autobots-microservico-spring-master/automanager/requisicoes.http), pronto para execução via extensão REST Client do VS Code.
