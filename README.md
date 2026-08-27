# 🛒 E-commerce Microservices Backend Platform

Hệ thống Backend thương mại điện tử hoàn chỉnh được xây dựng trên kiến trúc Microservices gồm 9 dịch vụ chuyên biệt, giải quyết các bài toán thực tế của một nền tảng bán lẻ hiện đại. Dự án mô phỏng toàn bộ luồng nghiệp vụ từ lúc người dùng đăng ký, tìm kiếm sản phẩm cho đến khi đặt hàng và thanh toán trực tuyến.

---

## 🏗️ Kiến trúc hệ thống (System Architecture)

Hệ thống bao gồm **9+ microservices**

### 1. Hạ tầng lõi (Core Infrastructure)
*   **Config Server:** Quản lý cấu hình tập trung cho toàn bộ hệ thống (Native/Git).
*   **Discovery Server (Eureka):** Quản lý danh bạ dịch vụ, cho phép các service tự tìm thấy nhau.
*   **API Gateway:** Điểm điều hướng duy nhất, xử lý bảo mật JWT và định tuyến.

### 2. Dịch vụ nghiệp vụ (Business Services)
*   **Auth Service:** Quản lý định danh, cấp phát JWT & Refresh Token.
*   **User Service:** Quản lý thông tin hồ sơ (Profile), địa chỉ và Ví điện tử (Wallet).
*   **Product Service:** Quản lý danh mục, sản phẩm và tích hợp **Elasticsearch** để tìm kiếm.
*   **Order Service:** Xử lý quy trình đặt hàng, tính toán hóa đơn.
*   **Payment Service:** Tích hợp cổng thanh toán **VNPay**, xử lý IPN và điều phối giao dịch.
*   **Cart Service:** Quản lý giỏ hàng tạm thời.
*   **Notification Service:** Gửi Email/Thông báo dựa trên sự kiện hệ thống.

---

## 🛠️ Tech Stack & Key Highlights

### 🚀 Backend & Communication
*   **Java 17 & Spring Boot 3:** Tận dụng các tính năng mới nhất của Java và hiệu năng của Boot 3.
*   **Event-Driven Architecture (Kafka):** Sử dụng Kafka để điều phối các tiến trình bất đồng bộ (Ví dụ: Thanh toán thành công -> Tự động trừ kho & Cộng tiền ví).
*   **Synchronous Communication (OpenFeign):** Gọi API trực tiếp giữa các service cho các tác vụ cần kết quả tức thì (ví dụ: Checkout lấy giá sản phẩm).
*   **API-First Development:** Định nghĩa hợp đồng dữ liệu qua **OpenAPI/Swagger** trước khi triển khai code, giúp đồng bộ tuyệt đối giữa các bên.

### 🔎 Search & Data
*   **Elasticsearch:** Triển khai Full-text search cho sản phẩm.
*   **MySQL & Spring Data JPA:** Quản lý dữ liệu quan hệ với cấu trúc DB tách biệt cho từng service để đảm bảo tính độc lập.

### 🏗️ DevOps & CI/CD
*   **Docker & Docker Compose:** Container hóa toàn bộ hệ sinh thái (Apps, DB, Kafka, ES).
*   **GitHub Actions CI:** Tự động hóa quy trình Build Maven, tạo Docker Image và Push lên Docker Hub.

---

## 🚀 Hướng dẫn cài đặt (Local Setup)

### Điều kiện tiên quyết
*   Docker & Docker Compose.
*   RAM tối thiểu: 12GB (khuyến nghị 16GB để chạy mượt toàn bộ hệ thống).

### Các bước khởi chạy
1.  **Clone dự án:**
    ```bash
    git clone https://github.com/vietthong812/ecom-microservices-backend.git
    cd ecom-microservices-backend
    ```
2.  **Khởi tạo Database:**
    Script `init.sql` sẽ tự động chạy để tạo 5+ database riêng biệt khi bạn khởi động Docker.
3.  **Chạy toàn bộ hệ thống:**
    ```bash
    docker-compose up -d --build
    ```

### Danh sách cổng (Endpoints)
*   **Eureka Dashboard:** `http://localhost:8761`
*   **API Gateway:** `http://localhost:8080`
*   **Kafka UI:** `http://localhost:8090`
*   **Config Server:** `http://localhost:8888`
*   **Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## 📈 Sơ đồ các luồng sự kiện (Kafka Topics)
*   `user-registration`: Đồng bộ thông tin khi User đăng ký thành công.
*   `wallet-update-topic`: Cập nhật số dư ví sau khi thanh toán/nạp tiền.
*   `order-placed-topic`: Thông báo trừ kho sản phẩm khi có đơn hàng mới.
*   `deposit/payment`: Kích hoạt gửi Email thông báo giao dịch thành công.

