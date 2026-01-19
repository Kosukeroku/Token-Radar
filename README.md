# **Token Radar — http://token-radar.com/**  
## _A cryptocurrency tracking platform with detailed coin info, price updates every 10 minutes, favorite coins follow option, and real-time notifications._  

---

## Technologies used:
### Backend:
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

### Testing:
– JUnit 5  
– Mockito  
– EmbeddedKafka

### Frontend:
– React 18+  
– TypeScript  
– TanStack Query  
– STOMP over WebSocket  

### Infrastructure & CI/CD:
– Docker + Docker Compose  
– GitHub Actions  
– Apache Kafka  
– Nginx  
– TimeWeb Cloud VPS  

---

### Features:
**Price Display:** Shows prices for the top 500 cryptocurrencies sourced from CoinGecko.  
**Currency Tracking:** Users can add/remove specific cryptocurrencies to their personal watchlist.  
**Detailed Coin Pages:** Each tracked cryptocurrency has its own dedicated page with in-depth information.  
**Custom Price Alerts:** Users can set various types of notifications:  
&emsp;• Price rises above/falls below an absolute value.  
&emsp;• Price changes by a specified percentage (+/-).  
**Notification Management:** Alerts transition through statuses (ACTIVE → TRIGGERED → READ) and are delivered via WebSocket/Kafka.  
**User Profile:** View user information and manage tracked currencies/alerts.

---

## Database Schema

### Core Tables

#### `users` - User Accounts
| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key, auto-increment |
| `username` | VARCHAR | Unique username |
| `email` | VARCHAR | Unique email address |
| `password` | VARCHAR | BCrypt hashed password |
| `created_at` | TIMESTAMP | Account creation time |

#### `coins` - Cryptocurrency Data
| Column | Type | Description |
|--------|------|-------------|
| `id` | VARCHAR | Primary key (CoinGecko ID like "bitcoin") |
| `symbol` | VARCHAR | Coin symbol (BTC, ETH) |
| `name` | VARCHAR | Coin name |
| `image_url` | TEXT | URL to coin image |
| `current_price` | DECIMAL(30,12) | Current USD price |
| `market_cap_rank` | INTEGER | Market capitalization ranking |
| `market_cap` | DECIMAL(30,2) | Total market value |
| `total_volume` | DECIMAL(30,2) | 24-hour trading volume |
| `price_change_percentage_24h` | DOUBLE PRECISION | 24h price change percentage |
| `sparkline_data` | TEXT | 7-day price chart data (JSON array) |
| `high_24h` | DECIMAL(30,12) | 24-hour high price |
| `low_24h` | DECIMAL(30,12) | 24-hour low price |
| `ath` | DECIMAL(30,12) | All-time high price |
| `ath_date` | TIMESTAMP | All-time high date |
| `atl` | DECIMAL(30,12) | All-time low price |
| `atl_date` | TIMESTAMP | All-time low date |
| `circulating_supply` | DECIMAL(30,2) | Circulating coin supply |
| `last_updated` | TIMESTAMP | Last data update time |
| `active` | BOOLEAN | Whether coin is active (true/false) |

#### `tracked_currencies` - User's Tracked Coins
| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key |
| `user_id` | BIGINT | Foreign key to `users.id` |
| `coin_id` | VARCHAR | Foreign key to `coins.id` |
| `added_at` | TIMESTAMP | When coin was added to tracking |

#### `price_alerts` - User Price Alerts
| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key |
| `user_id` | BIGINT | Foreign key to `users.id` |
| `coin_id` | VARCHAR | Foreign key to `coins.id` |
| `type` | VARCHAR | Alert type: `PRICE_ABOVE`, `PRICE_BELOW`, `PERCENTAGE_UP`, `PERCENTAGE_DOWN` |
| `status` | VARCHAR | Status: `ACTIVE`, `TRIGGERED`, `READ` |
| `threshold_value` | DECIMAL(30,12) | Target value for alert |
| `initial_price` | DECIMAL(30,12) | Price when alert was created |
| `last_checked_price` | DECIMAL(30,12) | Last checked price |
| `triggered_at` | TIMESTAMP | When alert was triggered (nullable) |
| `triggered_price` | DECIMAL(30,12) | Price when triggered (nullable) |
| `notification_message` | TEXT | Alert notification message |
| `created_at` | TIMESTAMP | Alert creation time |
| `updated_at` | TIMESTAMP | Last update time |

### Entity Relationships

- **One User → Many TrackedCurrencies**: A user can track multiple cryptocurrencies
- **One User → Many PriceAlerts**: A user can create multiple price alerts
- **One Coin → Many TrackedCurrencies**: A coin can be tracked by multiple users
- **One Coin → Many PriceAlerts**: A coin can have multiple alerts from different users

---

## API Documentation
### Authentication
| Method | Endpoint | Description | Requires Auth |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login user | No |

### Cryptocurrencies
| Method | Endpoint | Description | Requires Auth |
|--------|----------|-------------|---------------|
| GET | `/api/coins/dashboard` | Get paginated coins list | No |
| GET | `/api/coins/search?query={query}` | Search coins by name/symbol | No |
| GET | `/api/coins/{coinId}` | Get detailed coin information | No |

### User Profile & Tracking
| Method | Endpoint | Description | Requires Auth |
|--------|----------|-------------|---------------|
| GET | `/api/profile` | Get user profile with tracked coins | Yes |
| GET | `/api/tracked-currencies` | Get user's tracked coins | Yes |
| POST | `/api/tracked-currencies` | Add coin to tracking | Yes |
| DELETE | `/api/tracked-currencies/{coinId}` | Remove coin from tracking | Yes |

### Price Alerts & Notifications
| Method | Endpoint | Description | Requires Auth |
|--------|----------|-------------|---------------|
| GET | `/api/alerts` | Get user's active alerts | Yes |
| POST | `/api/alerts` | Create new price alert | Yes |
| GET | `/api/alerts/notifications` | Get alert notifications | Yes |
| GET | `/api/alerts/stats` | Get alert statistics | Yes |
| GET | `/api/alerts/unread-count` | Get count of unread alerts | Yes |
| PATCH | `/api/alerts/{alertId}/read` | Mark alert as read | Yes |
| POST | `/api/alerts/read-all` | Mark all alerts as read | Yes |
| POST | `/api/alerts/clear-read` | Clear read alerts | Yes |
| DELETE | `/api/alerts/{alertId}` | Delete alert | Yes |
| GET | `/api/alerts/coin/{coinId}` | Get alerts for specific coin | Yes |
| GET | `/api/alerts/status/{status}` | Get alerts by status | Yes |

---

### CI/CD Pipeline

**Platform:** *GitHub Actions* manages the entire CI/CD pipeline.  

**Triggered Workflows:**  
&nbsp;• **Pull Request to `master`:**  
        &emsp;• Action: Runs automatically when a PR is opened or updated.  
        &emsp;• Purpose: Validates code changes.  
        &emsp;• Steps: Executes the full suite of *unit and integration tests* (JUnit 5, Mockito, Spring Test, AssertJ, H2, EmbeddedKafka) against the proposed changes.  
&nbsp;• **Push to `master` Branch:**  
        &emsp;• Action: Runs automatically when changes are merged into `master`.  
        &emsp;• Purpose: Builds, tests, and deploys the new version to production.  
        &emsp;• Steps: Builds *Docker images* for the application components, runs the same *unit and integration tests*, and performs an *automatic deployment* to the production server (hosted on Timeweb Cloud VPS).  
        
**Automatic Deployment:** Successful completion of the `master` branch workflow results in zero-downtime deployment to the live application.  
**Workflow Definition:** The CI/CD process is defined in [`.github/workflows/deploy.yml`](https://github.com/Kosukeroku/Token-Radar/actions/workflows/deploy.yml).  

---

### Security

The application implements several measures to protect user data and ensure secure communication.  

**Authentication:** User authentication is handled using *JWT*. Tokens are signed with a strong secret key, stored securely, and have an expiration time.  
**Password Storage:** User passwords are securely hashed using the *BCrypt* hashing algorithm before being stored in the database.  
**Authorization:** *Spring Security* is configured to protect API endpoints, ensuring users can only access their own data (e.g., tracked currencies, alerts).  

---

### Roadmap

Planned features for future development:  

**User Portfolio Tracking:** Allow users to add their own cryptocurrency holdings and track the value of their personal portfolio over time.  
**Password Recovery:** Implement a password reset functionality allowing users to securely recover their accounts via email.  
**Russian Language Support:** Internationalize the frontend and backend to support the Russian language.  
**Email Notifications:** Extend the notification system to include email alerts for triggered price notifications.  

---

### Screenshots

<img width="1738" height="1186" alt="main_page_no_login" src="https://github.com/user-attachments/assets/3c1cddc0-3785-4014-a50e-45e6f00874e8" />

_Main page, unauthorized_  

<br>

<img width="1742" height="1188" alt="main_page" src="https://github.com/user-attachments/assets/9dc6179b-af3b-4b9b-8b5d-a8394ed3b68e" />

_Main page, logged in_

<br>

<img width="1680" height="1185" alt="search_results" src="https://github.com/user-attachments/assets/c1ae37f0-a283-428d-a2de-d0c8d9ff4123" />

_Search results_

<br>

<img width="1741" height="1185" alt="empty_search" src="https://github.com/user-attachments/assets/b3d208b6-4ede-43ea-a802-dd9113f4d591" />

_Empty search_

<br>

<img width="1728" height="1185" alt="details_page" src="https://github.com/user-attachments/assets/8fb387ee-e5fc-4ebb-a048-448f208a500d" />

_Detailed info page_

<br>

<img width="1632" height="1186" alt="sign_up_page" src="https://github.com/user-attachments/assets/d598f147-9b26-4f2b-b94a-098a10d632c1" />

_Sign up page_

<br>

<img width="440" height="705" alt="sign_up_form_bad_creds" src="https://github.com/user-attachments/assets/925a7c81-b2cd-4f45-8947-e11f524ef7dc" />

<img width="440" height="679" alt="sign_up_form_email_exists" src="https://github.com/user-attachments/assets/5df208df-0c48-4a7a-8bdb-e21c6f6f72fa" />

<img width="444" height="644" alt="sign_up_page_password_nomatch" src="https://github.com/user-attachments/assets/c2091368-6329-47e4-b927-54ae9ddbe27d" />

<img width="444" height="644" alt="sign_up_page_short_password" src="https://github.com/user-attachments/assets/2ac6d959-197b-402c-8ae7-03d852823c9d" />

_Sign up errors_

<br>

<img width="447" height="446" alt="login_page" src="https://github.com/user-attachments/assets/85f64bfb-3b66-43b9-ac5c-8b16aee57f44" />

_Login page_

<br>

<img width="449" height="509" alt="login_page_bad_creds" src="https://github.com/user-attachments/assets/aeec9bc5-9b81-4da3-bdba-1ec0ac11a2a1" />

_Login page – invalid credentials_

<br>

<img width="1676" height="1148" alt="profile_page" src="https://github.com/user-attachments/assets/16c763b0-1fbc-442e-bef2-089c264e6f6d" />

_Profile page_

<br>

<img width="1366" height="185" alt="invalid_alert1" src="https://github.com/user-attachments/assets/90c6c72e-9521-48e2-bd3a-a54d50cd514c" />

<img width="1359" height="153" alt="invalid_alert2" src="https://github.com/user-attachments/assets/b330598a-e517-42d2-9da4-9a57afd4a4cc" />

<img width="1355" height="147" alt="invalid_alert3" src="https://github.com/user-attachments/assets/884f3db6-e8ab-419e-a65a-8ab622b5c9b3" />

<img width="1331" height="136" alt="invalid_alert4" src="https://github.com/user-attachments/assets/55f725d8-15f3-42ec-b733-596d885c4587" />

_Invalid alert examples_

<br>

<img width="749" height="335" alt="empty_bell" src="https://github.com/user-attachments/assets/f73199eb-5820-45e4-b317-81734cdf0ced" />

_Empty bell_

<br>

<img width="654" height="123" alt="bell_with_notifs" src="https://github.com/user-attachments/assets/a4a4b3ce-c11a-418f-b10c-15314d910723" />

_Bell with notifications (TRIGGERED but not READ)_

<br>

<img width="682" height="550" alt="bell_with_notifs_open" src="https://github.com/user-attachments/assets/bc50a924-ab5e-45e1-8169-736ddf45deb7" />

_Bell with notifications (READ)_
