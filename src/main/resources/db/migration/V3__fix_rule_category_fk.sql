-- Добавление уникального ограничения в таблицу category
ALTER TABLE category 
ADD CONSTRAINT uq_category_budget_category UNIQUE (budget_id, category_id);

-- удаление старого внешнего ключа из таблицы правил
ALTER TABLE classification_rule 
DROP CONSTRAINT fk_rule_category;

-- добавление составного ключа, который юудет проверять, 
-- что пара (budget_id, category_id) существует в таблице category.
-- то есть что категория обязательно принадлежит этому же бюджету
ALTER TABLE classification_rule 
ADD CONSTRAINT fk_rule_category_budget 
FOREIGN KEY (budget_id, category_id) 
REFERENCES category(budget_id, category_id) 
ON DELETE CASCADE;