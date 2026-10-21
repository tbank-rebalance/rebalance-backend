# Как мы работаем

## Задачи

Все задачи заводятся как issue и живут на доске GitHub Projects:

`Бэклог -> На неделю -> В работе -> Ревью -> Тестирование -> Готово`

## Ветки

`main` всегда рабочая, прямые пуши запрещены. На каждую задачу создаётся отдельная ветка от `main`:

```
feature/<номер issue>-<коротко>    feature/12-budget-validation
fix/<номер issue>-<коротко>        fix/31-duplicate-import
```

## Коммиты

Формат [Conventional Commits](https://www.conventionalcommits.org/ru/):

```
<тип>(<область>): <что сделано>

feat(budget): validate that category shares sum to 100%
fix(sync): skip already imported transactions
```

Популярные типы: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `ci`, `build`, `ops`.

## Pull request

1. Перед пушем: `./mvnw spotless:apply` и `./mvnw verify`.
2. В описании PR укажи `Closes #<номер issue>`.
3. Нужен один апрув второго разработчика и зелёный CI.
4. Слияние через **Merge commit**, ветка удаляется.

## Задача готова, когда

- PR одобрен, CI зелёный
- На новую логику есть тесты
- Эндпоинт описан в Swagger и добавлен в Postman

## Стиль кода

- Форматирование &mdash; Palantir Java Format с табами, проверяется в CI. Отступы для IDE задаёт `.editorconfig`.
- Линтер &mdash; Error Prone, ошибки ломают сборку.
- Javadoc для классов и методов.
