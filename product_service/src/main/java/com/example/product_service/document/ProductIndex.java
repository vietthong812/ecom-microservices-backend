package com.example.product_service.document;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "products") // Tên Index trong Elasticsearch
@Setting(shards = 1, replicas = 0) // Cấu hình cơ bản cho môi trường dev
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductIndex {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Integer)
    private Integer stockQuantity;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Double)
    private Double averageRating;

    @Field(type = FieldType.Keyword, index = false)
    private String thumbnail;
}