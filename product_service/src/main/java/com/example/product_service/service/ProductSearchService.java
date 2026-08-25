package com.example.product_service.service;

import com.example.product_service.document.ProductIndex;
import com.example.product_service.mapper.ProductMapper;
import com.example.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductMapper productMapper;

    public List<ProductResponse> searchProducts(String keyword) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .fields("name^3", "description")
                                .query(keyword)
                                .fuzziness("AUTO")
                        )
                )
                .build();

        SearchHits<ProductIndex> searchHits = elasticsearchOperations.search(query, ProductIndex.class);

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }
}