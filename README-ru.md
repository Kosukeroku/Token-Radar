# **Token Radar — http://token-radar.com/**  
## _Платформа для мониторинга криптовалют с подробной информацией о монетах, обновлениями цен каждые 10 минут, опцией отслеживания выбранных монет и оповещениями в реальном времени._  
---

## Используемые технологии:
### Бэкенд:
– Java 17+  
– Spring Boot 3  
– Spring Data JPA  
– Spring Web MVC  
– Spring Security  
– Spring Kafka  
– Spring WebClient  
– PostgreSQL  
– Redis  
– Gradle  

### Тестирование:
– JUnit 5  
– Mockito  
– EmbeddedKafka

### Фронтенд:
– React 18+  
– TypeScript  
– TanStack Query  
– STOMP over WebSocket  

### Инфраструктура и CI/CD:
– Docker + Docker Compose  
– GitHub Actions  
– Apache Kafka  
– Nginx  
– TimeWeb Cloud VPS  

---

### Функционал:
**Отображение цен:** Показывает цены топ-500 криптовалют, полученные от CoinGecko.  
**Отслеживание валют:** Пользователи могут добавлять/удалять конкретные криптовалюты в свой личный список наблюдения.  
**Страницы с детальной информацией:** Каждая валюта имеет свою страницу с подробными данными.  
**Пользовательские уведомления:** Посетители могут настраивать оповещения следующих типов:  
&emsp;• Цена поднимается выше/падает ниже абсолютного значения.  
&emsp;• Цена меняется на указанный процент в большую или меньшую сторону.  
**Управление уведомлениями:** Все оповещения проходят через три статуса (ACTIVE → TRIGGERED → READ) и доставляются через WebSocket/Kafka.  
**Профиль пользователя:** Участники платформы имеют персональную страницу, на которой они могут управлять отслеживаемыми валютами и уведомлениями.  

---

## Схема базы данных

### Основные таблицы

#### `users` - Аккаунты пользователей
| Столбец | Тип | Описание |
|--------|------|-------------|
| `id` | BIGSERIAL | Первичный ключ, автоинкремент |
| `username` | VARCHAR | Уникальное имя пользователя |
| `email` | VARCHAR | Уникальный email-адрес |
| `password` | VARCHAR | Пароль, захешированный с помощью BCrypt |
| `created_at` | TIMESTAMP | Время создания аккаунта |

#### `coins` - Данные о криптовалютах
| Столбец | Тип | Описание |
|--------|------|-------------|
| `id` | VARCHAR | Первичный ключ (CoinGecko ID, например, "bitcoin") |
| `symbol` | VARCHAR | Символ монеты (BTC, ETH) |
| `name` | VARCHAR | Название монеты |
| `image_url` | TEXT | URL изображения монеты |
| `current_price` | DECIMAL(30,12) | Текущая цена в USD |
| `market_cap_rank` | INTEGER | Рейтинг по рыночной капитализации |
| `market_cap` | DECIMAL(30,2) | Общая рыночная стоимость |
| `total_volume` | DECIMAL(30,2) | 24-часовой объем торгов |
| `price_change_percentage_24h` | DOUBLE PRECISION | Процентное изменение цены за 24 часа |
| `sparkline_data` | TEXT | Данные графика цен за 7 дней (массив JSON) |
| `high_24h` | DECIMAL(30,12) | Максимальная цена за 24 часа |
| `low_24h` | DECIMAL(30,12) | Минимальная цена за 24 часа |
| `ath` | DECIMAL(30,12) | Исторический максимум цены |
| `ath_date` | TIMESTAMP | Дата исторического максимума |
| `atl` | DECIMAL(30,12) | Исторический минимум цены |
| `atl_date` | TIMESTAMP | Дата исторического минимума |
| `circulating_supply` | DECIMAL(30,2) | Количество монет в обращении |
| `last_updated` | TIMESTAMP | Время последнего обновления данных |
| `active` | BOOLEAN | Активна ли монета (true/false) |

#### `tracked_currencies` - Отслеживаемые монеты пользователя
| Столбец | Тип | Описание |
|--------|------|-------------|
| `id` | BIGSERIAL | Первичный ключ |
| `user_id` | BIGINT | Внешний ключ к users.id |
| `coin_id` | VARCHAR | Внешний ключ к coins.id |
| `added_at` | TIMESTAMP | Дата начала отслеживания монеты |

#### `price_alerts` - Ценовые оповещения пользователя
| Столбец | Тип | Описание |
|--------|------|-------------|
| `id` | BIGSERIAL | Первичный ключ |
| `user_id` | BIGINT | Внешний ключ к users.id |
| `coin_id` | VARCHAR | Внешний ключ к coins.id |
| `type` | VARCHAR | Тип оповещения: `PRICE_ABOVE`, `PRICE_BELOW`, `PERCENTAGE_UP`, `PERCENTAGE_DOWN` |
| `status` | VARCHAR | Статус: `ACTIVE`, `TRIGGERED`, `READ` |
| `threshold_value` | DECIMAL(30,12) | Целевое значение для оповещения |
| `initial_price` | DECIMAL(30,12) | Цена при создании оповещения |
| `last_checked_price` | DECIMAL(30,12) | Последняя проверенная цена |
| `triggered_at` | TIMESTAMP | Когда оповещение было сработало (может быть NULL) |
| `triggered_price` | DECIMAL(30,12) | Цена при срабатывании (может быть NULL) |
| `notification_message` | TEXT | 	Сообщение уведомления |
| `created_at` | TIMESTAMP | Время создания оповещения |
| `updated_at` | TIMESTAMP | Время последнего обновления |

### Связи между сущностями

- **Один пользователь → Много отслеживаемых валют**: Пользователь может отслеживать несколько криптовалют
- **Один пользователь → Много ценовых оповещений**: Пользователь может создавать несколько ценовых оповещений
- **Одна монета → Много отслеживаемых валют**: Монета может отслеживаться несколькими пользователями
- **Одна монета → Много ценовых оповещений**: У монеты может быть несколько оповещений от разных пользователей

---

## Документация API

### Аутентификация
| Метод | Эндпоинт | Описание | Требует аутентификации |
|--------|----------|-------------|----------------|
| POST | `/api/auth/register` | Зарегистрировать нового пользователя | Нет |
| POST | `/api/auth/login` | Войти в аккаунт | Нет |

### Криптовалюты
| Метод | Эндпоинт | Описание | Требует аутентификации |
|--------|----------|-------------|----------------|
| GET | `/api/coins/dashboard` | Получить список монет с пагинацией | Нет |
| GET | `/api/coins/search?query={query}` | Найти монету по названию/символу | Нет |
| GET | `/api/coins/{coinId}` | Получить детальную информацию о монете | Нет |

### Профиль пользователя и отслеживание
| Метод | Эндпоинт | Описание | Требует аутентификации |
|--------|----------|-------------|----------------|
| GET | `/api/profile` | Получить профиль пользователя с отслеживаемыми монетами | Да |
| GET | `/api/tracked-currencies` | Получить отслеживаемые монеты пользователя | Да |
| POST | `/api/tracked-currencies` | Добавить криптовалюту в отслеживаемые | Да |
| DELETE | `/api/tracked-currencies/{coinId}` | Удалить криптовалюту из отслеживаемых | Да |

### Ценовые оповещения и уведомления
| Метод | Эндпоинт | Описание | Требует аутентификации |
|--------|----------|-------------|----------------|
| GET | `/api/alerts` | Получить активные оповещения пользователя | Да |
| POST | `/api/alerts` | Создать новое ценовое оповещение | Да |
| GET | `/api/alerts/notifications` | Получить уведомления об оповещениях | Да |
| GET | `/api/alerts/stats` | Получить статистику оповещений | Да |
| GET | `/api/alerts/unread-count` | Получить количество непрочитанных уведомлений | Да |
| PATCH | `/api/alerts/{alertId}/read` | Пометить оповещение как прочитанное | Да |
| POST | `/api/alerts/read-all` | Пометить все оповещения как прочитанные | Да |
| POST | `/api/alerts/clear-read` | Очистить прочитанные оповещения | Да |
| DELETE | `/api/alerts/{alertId}` | Удалить оповещение | Да |
| GET | `/api/alerts/coin/{coinId}` | Получить оповещения для конкретной монеты | Да |
| GET | `/api/alerts/status/{status}` | Получить оповещения по статусу | Да |

---

### CI/CD-пайплайн

**Платформа:** Весь процесс CI/CD управляется через *GitHub Actions*.  

**Запускаемые workflow:**  
&nbsp;• **Pull Request в `master`:**  
        &emsp;• Действие: Запускается автоматически при открытии или обновлении PR.  
        &emsp;• Цель: Проверка изменений в коде.  
        &emsp;• Шаги: Выполнение полного набора *юнит-тестов и интеграционных тестов* (JUnit 5, Mockito, Spring Test, AssertJ, H2, EmbeddedKafka) для предлагаемых изменений.  
&nbsp;• **Push в ветку `master`:**  
        &emsp;• Действие: Запускается автоматически при слиянии изменений в `master`.  
        &emsp;• Цель: Сборка, тестирование и деплой новой версии.  
        &emsp;• Шаги: Сборка *Docker-образов* для компонентов приложения, запуск тех же *юнит-тестов и интеграционных тестов*, выполнение *автоматического деплоя* на сервер (размещенный на Timeweb Cloud VPS).  
        
**Автоматический деплой:** Успешное завершение workflow для ветки `master` приводит к деплою с нулевым временем простоя.  
**Конфигурация workflow:** Процесс CI/CD описан в файле [`.github/workflows/deploy.yml`](https://github.com/Kosukeroku/Token-Radar/actions/workflows/deploy.yml).  

---

### Безопасность

Приложение реализует несколько мер для защиты данных пользователей и обеспечения безопасной коммуникации.  

**Аутентификация:** Аутентификация пользователей обрабатывается с использованием *JWT*. Токены подписываются с помощью секретного ключа, безопасно хранятся и имеют время истечения.  
**Хранение паролей:** Пароли пользователей безопасно хэшируются с использованием алгоритма *BCrypt* перед сохранением в базу данных.  
**Авторизация:** *Spring Security* настроен для защиты конечных точек API и гарантирует, что пользователи могут получать доступ только к своим данным (например, отслеживаемым валютам, оповещениям).  

---

### Роадмап

Планируемые к добавлению функции:  

**Отслеживание пользовательского портфеля:** Позволить пользователям добавлять свои криптовалютные активы и отслеживать стоимость личного портфеля с течением времени.  
**Восстановление пароля:** Реализовать функционал сброса пароля, позволяющий пользователям безопасно восстанавливать доступ к своим аккаунтам через email.  
**Поддержка русского языка:** Добавить поддержку русского языка.  
**Email-уведомления:** Расширить систему уведомлений, включив email-оповещения о сработавших ценовых уведомлениях.  

---

### Скриншоты

<img width="1738" height="1186" alt="main_page_no_login" src="https://github.com/user-attachments/assets/3c1cddc0-3785-4014-a50e-45e6f00874e8" />

_Главная страница без аутентификации_  

<br>

<img width="1742" height="1188" alt="main_page" src="https://github.com/user-attachments/assets/9dc6179b-af3b-4b9b-8b5d-a8394ed3b68e" />

_Главная страница с аутентификацией_

<br>

<img width="1680" height="1185" alt="search_results" src="https://github.com/user-attachments/assets/c1ae37f0-a283-428d-a2de-d0c8d9ff4123" />

_Результаты поиска_

<br>

<img width="1741" height="1185" alt="empty_search" src="https://github.com/user-attachments/assets/b3d208b6-4ede-43ea-a802-dd9113f4d591" />

_Пустой поиск_

<br>

<img width="1728" height="1185" alt="details_page" src="https://github.com/user-attachments/assets/8fb387ee-e5fc-4ebb-a048-448f208a500d" />

_Страница с подробной информацией_

<br>

<img width="1632" height="1186" alt="sign_up_page" src="https://github.com/user-attachments/assets/d598f147-9b26-4f2b-b94a-098a10d632c1" />

_Страница регистрации_

<br>

<img width="440" height="705" alt="sign_up_form_bad_creds" src="https://github.com/user-attachments/assets/925a7c81-b2cd-4f45-8947-e11f524ef7dc" />

<img width="440" height="679" alt="sign_up_form_email_exists" src="https://github.com/user-attachments/assets/5df208df-0c48-4a7a-8bdb-e21c6f6f72fa" />

<img width="444" height="644" alt="sign_up_page_password_nomatch" src="https://github.com/user-attachments/assets/c2091368-6329-47e4-b927-54ae9ddbe27d" />

<img width="444" height="644" alt="sign_up_page_short_password" src="https://github.com/user-attachments/assets/2ac6d959-197b-402c-8ae7-03d852823c9d" />

_Ошибки при регистрации_

<br>

<img width="447" height="446" alt="login_page" src="https://github.com/user-attachments/assets/85f64bfb-3b66-43b9-ac5c-8b16aee57f44" />

_Страница логина_

<br>

<img width="449" height="509" alt="login_page_bad_creds" src="https://github.com/user-attachments/assets/aeec9bc5-9b81-4da3-bdba-1ec0ac11a2a1" />

_Неверные данные на странице логина_

<br>

<img width="1676" height="1148" alt="profile_page" src="https://github.com/user-attachments/assets/16c763b0-1fbc-442e-bef2-089c264e6f6d" />

_Страница профиля_

<br>

<img width="1366" height="185" alt="invalid_alert1" src="https://github.com/user-attachments/assets/90c6c72e-9521-48e2-bd3a-a54d50cd514c" />

<img width="1359" height="153" alt="invalid_alert2" src="https://github.com/user-attachments/assets/b330598a-e517-42d2-9da4-9a57afd4a4cc" />

<img width="1355" height="147" alt="invalid_alert3" src="https://github.com/user-attachments/assets/884f3db6-e8ab-419e-a65a-8ab622b5c9b3" />

<img width="1331" height="136" alt="invalid_alert4" src="https://github.com/user-attachments/assets/55f725d8-15f3-42ec-b733-596d885c4587" />

_Примеры невалидных оповещений_

<br>

<img width="749" height="335" alt="empty_bell" src="https://github.com/user-attachments/assets/f73199eb-5820-45e4-b317-81734cdf0ced" />

_Пустой колочольчик_

<br>

<img width="654" height="123" alt="bell_with_notifs" src="https://github.com/user-attachments/assets/a4a4b3ce-c11a-418f-b10c-15314d910723" />

_Колокольчик при срабатывании оповещений, но до их прочтения (TRIGGERED, но еще не READ)_

<br>

<img width="682" height="550" alt="bell_with_notifs_open" src="https://github.com/user-attachments/assets/bc50a924-ab5e-45e1-8169-736ddf45deb7" />

_Открытый колокольчик с оповещениями (статус оповещений READ)_
