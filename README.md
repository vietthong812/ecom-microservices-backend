# E-commerce Microservices Backend

Backend thương mại điện tử theo kiến trúc **microservices** xây dựng bằng **Java 17 + Spring Boot + Spring Cloud**.

> Ngôn ngữ: Tiếng Việt (kèm thuật ngữ tiếng Anh khi cần).

## 1) Giới thiệu & mục tiêu

Dự án tổ chức backend theo nhiều service độc lập để:
- tách biệt nghiệp vụ (auth, user, product, order, payment, cart, notification),
- dễ scale theo từng service,
- tăng khả năng bảo trì và triển khai.

## 2) Tổng quan kiến trúc / danh sách service

### Core infrastructure services
- `config_server`: Spring Cloud Config Server
- `discovery_server`: Eureka Server
- `api_gateway`: Spring Cloud Gateway (điểm vào chính)

### Business services
- `auth_service`
- `user_service`
- `product_service`
- `order_service`
- `payment_service`
- `cart_service`
- `notification-service`

### Thành phần hạ tầng local (Docker Compose)
- MySQL
- Kafka
- Kafka UI
- Elasticsearch
- Config Server
- Discovery Server
- API Gateway
- Các service nghiệp vụ: `auth-service`, `user-service`, `order-service`, `product-service`, `payment-service`

> Lưu ý: `cart_service` và `notification-service` đang có source trong repo nhưng **chưa được khai báo trong `docker-compose.yml` hiện tại**.

---

## 3) Technology stack

- **Java 17**
- **Spring Boot 3.x**
- **Spring Cloud** (Config, Eureka, Gateway, OpenFeign)
- **Spring Data JPA**, **MySQL**
- **Spring for Apache Kafka**
- **Spring Data Elasticsearch** (ở `product_service`)
- **Spring Security + JWT** (ở `auth_service`, `api_gateway`)
- **Spring Mail** (ở `notification-service`)
- **OpenAPI Generator + springdoc-openapi**
- **Docker / Docker Compose**

## 4) Prerequisites

- Docker Engine + Docker Compose v2
- Java 17 (nếu chạy manual)
- Maven 3.9+ hoặc dùng `./mvnw`
- (Khuyến nghị) Git để clone thêm config repo nếu cần

## 5) Cài đặt local

```bash
git clone https://github.com/vietthong812/ecom-microservices-backend.git
cd ecom-microservices-backend
```

Kiểm tra nhanh các thành phần chính:
- `docker-compose.yml`
- `init.sql`
- các module service trong thư mục gốc

## 6) Chạy bằng Docker Compose

### Cách 1: chạy toàn bộ compose hiện có
```bash
docker compose up -d --build
```

### Cách 2: chạy lại từ đầu
```bash
docker compose down -v
docker compose up -d --build
```

Kiểm tra trạng thái:
```bash
docker compose ps
```

Xem log 1 service:
```bash
docker compose logs -f api-gateway
```

## 7) Chạy manual từng service (nếu không dùng full compose)

### 7.1 Chạy hạ tầng trước
Có thể chạy các dependency bằng Docker:
```bash
docker compose up -d mysql-db kafka kafka-ui elasticsearch discovery-server config-server
```

### 7.2 Chạy từng service bằng Maven Wrapper
Ví dụ:
```bash
cd auth_service
./mvnw spring-boot:run
```

Tương tự cho các thư mục service còn lại (`user_service`, `product_service`, `order_service`, `payment_service`, `api_gateway`, `cart_service`, `notification-service`).

> Khi chạy manual nhiều service cùng máy, nên set `SERVER_PORT` khác nhau (hoặc cấu hình qua Config Server) để tránh trùng cổng.

## 8) Cấu hình & biến môi trường quan trọng

### Cấu hình phân tán (Config + Discovery)
- `SPRING_CONFIG_IMPORT=configserver:http://config-server:8888`
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-server:8761/eureka/`

### Database
- `SPRING_DATASOURCE_URL=jdbc:mysql://mysql-db:3306/<db_name>`
  - `auth_db`, `user_db`, `product_db`, `order_db`

### Messaging
- `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-broker:19092` (trong network docker)

### Search
- `SPRING_ELASTICSEARCH_URIS=http://elasticsearch:9200` (cho `product-service`)

### Config Server
- `GIT_PASSWORD` (khi dùng backend Git cho Config Server)
- profile `native` trong compose:
  - `SPRING_PROFILES_ACTIVE=native`
  - `SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS=file:/app/config-repo`

### Security / Payment / Mail
- JWT secret đang có trong một số `application.yml`; nên **override bằng biến môi trường** ở môi trường thật.
- `payment_service` dùng các key:
  - `payment.vnpay.tmn-code`
  - `payment.vnpay.secret-key`
  - `payment.vnpay.pay-url`
  - `payment.vnpay.return-url`
- `notification-service` cần cấu hình mail (ví dụ `spring.mail.*`).

## 9) Khởi tạo database

File `init.sql` được mount vào MySQL container để tự tạo DB khi khởi động:
- `auth_db`
- `user_db`
- `product_db`
- `cart_db`
- `order_db`

Nếu DB đã tồn tại từ volume cũ, có thể cần:
```bash
docker compose down -v
```
rồi chạy lại.

## 10) Endpoints / ports từ `docker-compose.yml`

| Thành phần | Port host | Port container |
|---|---:|---:|
| MySQL | `3306` | `3306` |
| Kafka broker | `9092` | `9092` |
| Kafka UI | `8090` | `8080` |
| Elasticsearch HTTP | `9200` | `9200` |
| Elasticsearch transport | `9300` | `9300` |
| Config Server | `8888` | `8888` |
| Discovery Server (Eureka) | `8761` | `8761` |
| API Gateway | `8080` | `8080` |

> Các service nghiệp vụ khác trong compose hiện tại không publish cổng ra host (chạy nội bộ network Docker).

## 11) Ghi chú Kafka / Eureka / Config Server / Elasticsearch

### Kafka
Các topic xuất hiện trong code:
- `user-registration`
- `wallet-update-topic`
- `order_update`
- `stock-update`
- `deposit`
- `payment`

### Eureka
- `discovery_server` chạy ở cổng `8761`.
- Service client đăng ký qua `eureka.client.service-url.defaultZone`.

### Config Server
- Chạy cổng `8888`.
- Có thể dùng Git backend hoặc native file backend (compose đang dùng native mount local path).

### Elasticsearch
- Được dùng trong `product_service` (Spring Data Elasticsearch).
- Endpoint nội bộ: `http://elasticsearch:9200`.

## 12) Gợi ý cấu trúc thư mục

```text
.
├── api_gateway/
├── auth_service/
├── cart_service/
├── config_server/
├── discovery_server/
├── notification-service/
├── order_service/
├── payment_service/
├── product_service/
├── user_service/
├── docker-compose.yml
└── init.sql
```

## 13) Troubleshooting

1. **Config Server không đọc được config**
   - Kiểm tra mount path `./ecommerce_config_repo/config_repo:/app/config-repo` trong compose.
   - Kiểm tra profile `native`/`git` và `GIT_PASSWORD` nếu dùng Git backend.

2. **Service không register vào Eureka**
   - Đảm bảo `discovery-server` healthy trước khi start service.
   - Kiểm tra `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`.

3. **Lỗi Kafka connection**
   - Trong Docker network dùng `kafka-broker:19092`.
   - Từ host local thường dùng `localhost:9092`.

4. **Elasticsearch chưa sẵn sàng**
   - Kiểm tra `http://localhost:9200/_cat/health`.
   - Chờ health trạng thái ổn định trước khi test search.

5. **MySQL không tạo lại DB**
   - Vì dùng volume, script init chỉ chạy lần tạo data directory mới.
   - Xóa volume bằng `docker compose down -v` rồi khởi động lại.

6. **Trùng cổng khi chạy manual**
   - Gán `SERVER_PORT` khác nhau cho từng service hoặc cấu hình từ Config Server.

## 14) Contributing / License

### Contributing
- Tạo branch riêng cho từng thay đổi.
- Commit nhỏ, rõ ràng.
- Mở Pull Request kèm mô tả mục tiêu và cách kiểm thử.

### License
Hiện chưa thấy file `LICENSE` trong repository. Bạn có thể bổ sung license phù hợp (ví dụ MIT/Apache-2.0) cho dự án public.
