package com.appsdeveloperblog.products.web.graphql;

import com.appsdeveloperblog.core.dto.Product;
import com.appsdeveloperblog.products.service.ProductService;
import com.appsdeveloperblog.products.web.graphql.input.ProductInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class ProductsGraphQLController {

    private final ProductService productService;

    public ProductsGraphQLController(ProductService productService) {
        this.productService = productService;
    }

    @QueryMapping
    public List<Product> products() {
        return productService.findAll();
    }

    @QueryMapping
    public Product product(@Argument UUID id) {
        return productService.findAll().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @MutationMapping
    public Product createProduct(@Argument ProductInput input) {
        var product = new Product();
        product.setName(input.name());
        product.setPrice(input.price());
        product.setQuantity(input.quantity());
        return productService.save(product);
    }

    @MutationMapping
    public Product updateProduct(@Argument UUID id, @Argument ProductInput input) {
        var product = new Product();
        product.setName(input.name());
        product.setPrice(input.price());
        product.setQuantity(input.quantity());
        return productService.update(id, product);
    }

    @MutationMapping
    public boolean deleteProduct(@Argument UUID id) {
        productService.delete(id);
        return true;
    }
}
