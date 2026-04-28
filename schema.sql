-- ============================================================
-- SCHEMA - MicroBio
-- Banco: PostgreSQL
-- ============================================================

-- Limpar tabelas se existirem (ordem respeitando FKs)
DROP TABLE IF EXISTS pedido_analise CASCADE;
DROP TABLE IF EXISTS analise CASCADE;
DROP TABLE IF EXISTS pedido CASCADE;
DROP TABLE IF EXISTS orcamento CASCADE;
DROP TABLE IF EXISTS pessoa CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS endereco CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

-- ============================================================
-- TABELA: usuario
-- ============================================================
CREATE TABLE usuario (
    id     SERIAL PRIMARY KEY,
    login  VARCHAR(50)  NOT NULL UNIQUE,
    senha  VARCHAR(255) NOT NULL  -- BCrypt hash
);

-- ============================================================
-- TABELA: endereco
-- ============================================================
CREATE TABLE endereco (
    id      SERIAL PRIMARY KEY,
    rua     VARCHAR(150),
    numero  VARCHAR(20),
    bairro  VARCHAR(100),
    cidade  VARCHAR(100),
    estado  VARCHAR(50),
    cep     VARCHAR(20)
);

-- ============================================================
-- TABELA: cliente
-- ============================================================
CREATE TABLE cliente (
    id             SERIAL PRIMARY KEY,
    tipo_cliente   VARCHAR(50),
    cpf_cnpj       VARCHAR(20) UNIQUE,
    data_cadastro  TIMESTAMP DEFAULT NOW(),
    endereco_id    INT REFERENCES endereco(id) ON DELETE SET NULL
);

-- ============================================================
-- TABELA: pessoa
-- ============================================================
CREATE TABLE pessoa (
    id         SERIAL PRIMARY KEY,
    nome       VARCHAR(150) NOT NULL,
    email      VARCHAR(150),
    telefone   VARCHAR(20),
    data_sol   TIMESTAMP DEFAULT NOW(),
    cliente_id INT REFERENCES cliente(id) ON DELETE CASCADE
);

-- ============================================================
-- TABELA: orcamento
-- ============================================================
CREATE TABLE orcamento (
    id             SERIAL PRIMARY KEY,
    usuario_id     INT REFERENCES usuario(id) ON DELETE SET NULL,
    pessoa_id      INT REFERENCES pessoa(id)  ON DELETE SET NULL,
    valor_total    DECIMAL(10, 2),
    data_orcamento TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABELA: pedido
-- ============================================================
CREATE TABLE pedido (
    id           SERIAL PRIMARY KEY,
    orcamento_id INT REFERENCES orcamento(id) ON DELETE SET NULL,
    status       VARCHAR(20) DEFAULT 'PENDENTE',
    observacoes  TEXT,
    data_pedido  TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABELA: analise
-- ============================================================
CREATE TABLE analise (
    id        SERIAL PRIMARY KEY,
    nome      VARCHAR(150) NOT NULL,
    descricao TEXT,
    valor     DECIMAL(10, 2)
);

-- ============================================================
-- TABELA: pedido_analise (N:N entre pedido e analise)
-- ============================================================
CREATE TABLE pedido_analise (
    id         SERIAL PRIMARY KEY,
    pedido_id  INT NOT NULL REFERENCES pedido(id)  ON DELETE CASCADE,
    analise_id INT NOT NULL REFERENCES analise(id) ON DELETE CASCADE
);

-- ============================================================
-- ÍNDICES úteis
-- ============================================================
CREATE INDEX idx_pessoa_cliente     ON pessoa(cliente_id);
CREATE INDEX idx_orcamento_usuario  ON orcamento(usuario_id);
CREATE INDEX idx_orcamento_pessoa   ON orcamento(pessoa_id);
CREATE INDEX idx_pedido_orcamento   ON pedido(orcamento_id);
CREATE INDEX idx_pedido_analise_p   ON pedido_analise(pedido_id);
CREATE INDEX idx_pedido_analise_a   ON pedido_analise(analise_id);

-- ============================================================
-- DADOS INICIAIS - usuário admin para login
-- senha: admin123 (BCrypt)
-- ============================================================
INSERT INTO usuario (login, senha)
VALUES ('admin', '$2b$10$uYkTQdD9ZnZTB.t8L6W.puv.I.cTkrFIUOqUdIDIh7SJjVhMF5RUe');