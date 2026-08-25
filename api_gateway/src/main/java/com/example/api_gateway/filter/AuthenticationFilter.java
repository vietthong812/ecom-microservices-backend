package com.example.api_gateway.filter;


import com.example.api_gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    @Autowired
    private RouteValidator routeValidator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config { }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if(routeValidator.isSecured.test(exchange.getRequest())) {
                ServerHttpRequest request = exchange.getRequest();
               //kiem tra co header hay khong
                if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thiếu header Authorization");
                }
                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {
                    jwtUtil.validateToken(authHeader);
                    // Trích xuất thông tin từ token
                    String userId = jwtUtil.getUserIdFromToken(authHeader);
                    String userName = jwtUtil.getUsernameFromToken(authHeader);
                    String roles = jwtUtil.getRolesFromToken(authHeader); // nếu có phân quyền
                    // kiểm tra admin
                    String path = request.getURI().getPath();
                    String method = request.getMethod().name();

                    // Xác định các đường dẫn chỉ dành cho Admin (POST, PUT, DELETE vào products hoặc categories)
                    boolean isAdminOnlyPath = (path.contains("/api/products") || path.contains("/api/categories"))
                            && !method.equalsIgnoreCase("GET");

                    if (isAdminOnlyPath) {
                        if (roles == null || !roles.contains("ROLE_ADMIN")) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "truy cập bị từ chối: chỉ admin mới có quyền thực hiện thao tác này");
                        }
                    }
                    // đính thêm thông tin này vào Header gửi đi tiếp
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Name", userName)
                            .header("X-User-Roles", roles)
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                }
                catch (Exception e) {
                    logger.error("xác thực token thất bại", e);
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());}
            }
            return chain.filter(exchange);
        };
    }
}