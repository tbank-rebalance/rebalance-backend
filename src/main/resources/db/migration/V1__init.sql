-- База данных Rebalance

-- Справочники 

CREATE TABLE limit_type (
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE CHECK (name IN ('PERCENT', 'AMOUNT'))
);
INSERT INTO limit_type (id, name) VALUES (1, 'PERCENT'), (2, 'AMOUNT');

CREATE TABLE transaction_type (
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE CHECK (name IN ('PURCHASE', 'REFUND', 'TRANSFER', 'TOP_UP'))
);
INSERT INTO transaction_type (id, name) VALUES (1, 'PURCHASE'), (2, 'REFUND'), (3, 'TRANSFER'), (4, 'TOP_UP');

CREATE TABLE transaction_source (
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE CHECK (name IN ('BANK_IMPORT', 'MANUAL'))
);
INSERT INTO transaction_source (id, name) VALUES (1, 'BANK_IMPORT'), (2, 'MANUAL');

CREATE TABLE classification_method (
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE CHECK (name IN ('AUTO', 'MANUAL', 'UNCLASSIFIED'))
);
INSERT INTO classification_method (id, name) VALUES (1, 'AUTO'), (2, 'MANUAL'), (3, 'UNCLASSIFIED');

CREATE TABLE goal_status (
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE CHECK (name IN ('ACTIVE', 'COMPLETED', 'EXPIRED'))
);
INSERT INTO goal_status (id, name) VALUES (1, 'ACTIVE'), (2, 'COMPLETED'), (3, 'EXPIRED');

-- Пользователь

CREATE TABLE "user" (
    user_id       SERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    avatar_url    VARCHAR(500),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP
);

-- Уникальность email без учета регистра
CREATE UNIQUE INDEX idx_user_email_lower ON "user" (LOWER(email));

-- Бюджет и счета

CREATE TABLE budget (
    budget_id    SERIAL PRIMARY KEY,
    user_id      INT NOT NULL,
    month        DATE NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL CHECK (total_amount >= 0),
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_budget_user_month UNIQUE (user_id, month)
);

CREATE TABLE bank_account (
    account_id          SERIAL PRIMARY KEY,
    user_id             INT NOT NULL,
    external_account_id VARCHAR(255),
    bank_name           VARCHAR(100),
    account_name        VARCHAR(100),
    currency            CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    CONSTRAINT fk_bank_account_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_account_external UNIQUE NULLS NOT DISTINCT (user_id, bank_name, external_account_id)
);

-- Категории, цели и история

CREATE TABLE category (
    category_id   SERIAL PRIMARY KEY,
    budget_id     INT NOT NULL,
    name          VARCHAR(100) NOT NULL,
    limit_type_id INT NOT NULL,
    limit_value   NUMERIC(12,2) NOT NULL CHECK (limit_value >= 0),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP,
    CONSTRAINT fk_category_budget FOREIGN KEY (budget_id) REFERENCES budget(budget_id) ON DELETE CASCADE,
    CONSTRAINT fk_category_limit_type FOREIGN KEY (limit_type_id) REFERENCES limit_type(id),
    CONSTRAINT uq_category_budget_name UNIQUE (budget_id, name)
);

-- Проверка лимита в зависимости от выбранного типа
CREATE FUNCTION validate_category_limit()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_limit_type_name VARCHAR(50);
BEGIN
    -- Получаем имя типа лимита
    SELECT name INTO v_limit_type_name FROM limit_type WHERE id = NEW.limit_type_id;

    -- Проверяем проценты (должны быть от 0 до 100)
    IF v_limit_type_name = 'PERCENT' AND (NEW.limit_value < 0 OR NEW.limit_value > 100) THEN
        RAISE EXCEPTION 'Процентный лимит категории должен быть от 0 до 100';
    END IF;

    -- Проверяем деньги (не могут быть отрицательными)
    IF v_limit_type_name = 'AMOUNT' AND NEW.limit_value < 0 THEN
        RAISE EXCEPTION 'Денежный лимит категории не может быть отрицательным';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validate_category_limit
BEFORE INSERT OR UPDATE OF limit_type_id, limit_value
ON category
FOR EACH ROW
EXECUTE FUNCTION validate_category_limit();

-- ------------------------------------------------------------

CREATE TABLE savings_goal (
    goal_id        SERIAL PRIMARY KEY,
    user_id        INT NOT NULL,
    title          VARCHAR(150) NOT NULL,
    target_amount  NUMERIC(12,2) NOT NULL CHECK (target_amount > 0),
    current_amount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (current_amount >= 0),
    deadline       DATE NOT NULL,
    status_id      INT NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP,
    CONSTRAINT fk_savings_goal_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_savings_goal_status FOREIGN KEY (status_id) REFERENCES goal_status(id)
);

CREATE TABLE goal_contribution (
    contribution_id SERIAL PRIMARY KEY,
    goal_id         INT NOT NULL,
    amount          NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contribution_goal FOREIGN KEY (goal_id) REFERENCES savings_goal(goal_id) ON DELETE CASCADE
);

CREATE TABLE change_history (
    history_id  SERIAL PRIMARY KEY,
    user_id     INT NOT NULL,
    entity_type VARCHAR(50) NOT NULL CHECK (entity_type IN ('BUDGET', 'CATEGORY', 'TRANSACTION', 'GOAL', 'GOAL_CONTRIBUTION')),
    entity_id   INT NOT NULL,
    action      VARCHAR(50) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'IMPORT', 'CONTRIBUTION')),
    old_values  JSON,
    new_values  JSON,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_change_history_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE
);

-- Правила, уведомления и транзакции

CREATE TABLE classification_rule (
    rule_id          SERIAL PRIMARY KEY,
    budget_id        INT NOT NULL,
    category_id      INT NOT NULL,
    merchant_pattern VARCHAR(255),
    mcc              VARCHAR(4) CHECK (mcc ~ '^[0-9]{4}$'),
    keyword_pattern  VARCHAR(255),
    priority         INT NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT true,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT fk_rule_budget FOREIGN KEY (budget_id) REFERENCES budget(budget_id) ON DELETE CASCADE,
    CONSTRAINT fk_rule_category FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE,
    CONSTRAINT chk_rule_condition CHECK (merchant_pattern IS NOT NULL OR mcc IS NOT NULL OR keyword_pattern IS NOT NULL)
);

CREATE TABLE notification (
    notification_id   SERIAL PRIMARY KEY,
    user_id           INT NOT NULL,
    budget_id         INT NOT NULL,
    category_id       INT NOT NULL,
    threshold_percent NUMERIC(5,2) NOT NULL CHECK (threshold_percent IN (80, 100)),
    message           TEXT NOT NULL,
    is_read           BOOLEAN NOT NULL DEFAULT false,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    push_sent_at      TIMESTAMP,
    updated_at        TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_budget FOREIGN KEY (budget_id) REFERENCES budget(budget_id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_category FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE,
    CONSTRAINT uq_notification_threshold UNIQUE (budget_id, category_id, threshold_percent)
);

CREATE TABLE transaction (
    transaction_id           SERIAL PRIMARY KEY,
    user_id                  INT NOT NULL,
    budget_id                INT,
    category_id              INT,
    source_account_id        INT,
    destination_account_id   INT,
    original_transaction_id  INT,
    external_transaction_id  VARCHAR(255),
    amount                   NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    transaction_type_id      INT NOT NULL,
    source_id                INT NOT NULL,
    merchant                 VARCHAR(255),
    mcc                      VARCHAR(4) CHECK (mcc ~ '^[0-9]{4}$'),
    classification_method_id INT NOT NULL,
    description              TEXT,
    transaction_date         TIMESTAMP NOT NULL,
    created_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_transaction_budget FOREIGN KEY (budget_id) REFERENCES budget(budget_id) ON DELETE SET NULL,
    CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE SET NULL,
    CONSTRAINT fk_transaction_source_acc FOREIGN KEY (source_account_id) REFERENCES bank_account(account_id) ON DELETE SET NULL,
    CONSTRAINT fk_transaction_dest_acc FOREIGN KEY (destination_account_id) REFERENCES bank_account(account_id) ON DELETE SET NULL,
    CONSTRAINT fk_transaction_original FOREIGN KEY (original_transaction_id) REFERENCES transaction(transaction_id) ON DELETE SET NULL,
    CONSTRAINT fk_transaction_type FOREIGN KEY (transaction_type_id) REFERENCES transaction_type(id),
    CONSTRAINT fk_transaction_source FOREIGN KEY (source_id) REFERENCES transaction_source(id),
    CONSTRAINT fk_transaction_class_method FOREIGN KEY (classification_method_id) REFERENCES classification_method(id),
    
    -- Внешний ID уникален в пределах источника (если указан)
    CONSTRAINT uq_transaction_external UNIQUE NULLS NOT DISTINCT (source_id, external_transaction_id),
    
    -- Для TRANSFER (id=3) должны быть указаны оба счета и они не должны совпадать
    CONSTRAINT chk_transfer_accounts CHECK (
        transaction_type_id <> 3 OR 
        (source_account_id IS NOT NULL AND destination_account_id IS NOT NULL AND source_account_id <> destination_account_id)
    )
);