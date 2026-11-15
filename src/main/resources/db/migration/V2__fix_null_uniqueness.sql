-- Исправление ограничения для банковских счетов

ALTER TABLE bank_account DROP CONSTRAINT uq_account_external;

-- Проверка уникальности ТОЛЬКО если external_account_id не NULL с помощью частичного индекса
CREATE UNIQUE INDEX uq_account_external_idx 
ON bank_account (user_id, bank_name, external_account_id)
WHERE external_account_id IS NOT NULL;

-- Удаление старого ограничение для транзакций:
ALTER TABLE transaction DROP CONSTRAINT uq_transaction_external;

-- Проверка уникальности ТОЛЬКО если external_transaction_id не NULL с помощью частичного индекса
CREATE UNIQUE INDEX uq_transaction_external_idx 
ON transaction (source_id, external_transaction_id)
WHERE external_transaction_id IS NOT NULL;