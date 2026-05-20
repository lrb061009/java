# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build and run
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw -Dtest=CanteenDemoApplicationTests test
```

The app starts on `http://localhost:8080`. Java 17, Spring Boot 4.0.6, Maven wrapper included.

## Database

MySQL database `canteen` at `localhost:3306`. JPA/Hibernate with `ddl-auto=update` auto-creates tables from entity definitions and prints formatted SQL to stdout. Connection credentials are in `application.properties`.

## Architecture

Standard Spring Boot layered architecture:
- `entity/` — 7 JPA entities with Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- `repository/` — Spring Data JPA interfaces
- `service/` — business logic, uses `@RequiredArgsConstructor` injection
- `controller/` — split into two:
  - `WebController` — server-rendered Thymeleaf pages (login, register, home, menu detail, my orders, my reviews)
  - `ApiController` — REST API under `/api/*`
- `config/DataInitializer` — `CommandLineRunner` that seeds 3 users, 3 canteens, and 10 menu items on first run (when user table is empty). Cleans old orders on startup to handle schema migrations.

## Domain Model

- **User** — `username` (unique), `password`, `realName`, `role` (`USER`/`ADMIN`), `phone`, `department`, `balance` (Decimal, default 0.00). Balance is deducted on order, refunded on cancel, topped up via `/api/user/charge`.
- **Canteen** — `name` (食堂). Menu items belong to a canteen.
- **Menu** — `date`, `mealType` (`BREAKFAST`/`LUNCH`/`DINNER`), `name`, `price`, `stock`, `status` (`AVAILABLE`/`SOLD_OUT`), `@ManyToOne` to Canteen (`canteen_id`)
- **Order** — `@ManyToOne` to User, `@OneToMany` to OrderDetail (cascade ALL), `totalPrice`, `status` (`PENDING`→`CONFIRMED`→`COMPLETED`, cancel to `CANCELLED`), `remark`, `takeCode`, `orderTime`. Maps to `orders` table. Order creation checks balance and deducts; cancellation refunds balance and restores stock.
- **OrderDetail** — `@ManyToOne` to Order/Menu, `quantity`, `price` (snapshot at order time). Maps to `order_detail`. Enables multi-item orders.
- **Payment** — `@OneToOne` to Order (unique), `paymentMethod`, `paymentTime`, `paymentStatus` (`UNPAID`/`PAID`)
- **Review** — `@ManyToOne` to User, Menu, and Order. `order_id` unique (one review per order). Only COMPLETED orders can be reviewed. `checkStatus` field (`PENDING`→`APPROVED`/`REJECTED`, admin approval workflow).

## Auth

No Spring Security. Session-based auth: `WebController` stores the full `User` object in `HttpSession` under `"user"`. User is refreshed from DB on every page load to keep balance current. Pages check for null and redirect to `/login`. Passwords are stored and compared as plaintext.

## Templates

Thymeleaf templates in `src/main/resources/templates/` — `index.html`, `login.html`, `register.html`, `home.html`, `menu-detail.html`, `my-orders.html`, `my-reviews.html`. All logged-in pages show balance and a recharge modal. Templates reference `user` from session and model attributes set by `WebController`.
