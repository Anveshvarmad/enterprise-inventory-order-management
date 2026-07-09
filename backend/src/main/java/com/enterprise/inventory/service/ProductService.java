package com.enterprise.inventory.service;

import com.enterprise.inventory.dto.ProductCreateRequest;
import com.enterprise.inventory.dto.ProductResponse;
import com.enterprise.inventory.dto.ProductUpdateRequest;
import com.enterprise.inventory.entity.Category;
import com.enterprise.inventory.entity.Product;
import com.enterprise.inventory.entity.Supplier;
import com.enterprise.inventory.enums.ProductStatus;
import com.enterprise.inventory.exception.DuplicateResourceException;
import com.enterprise.inventory.exception.ResourceNotFoundException;
import com.enterprise.inventory.repository.CategoryRepository;
import com.enterprise.inventory.repository.ProductRepository;
import com.enterprise.inventory.repository.SupplierRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Locale;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            String search,
            Long categoryId,
            Long supplierId,
            ProductStatus status,
            Pageable pageable
    ) {
        Specification<Product> specification = buildSpecification(search, categoryId, supplierId, status);

        return productRepository.findAll(specification, pageable)
                .map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findProduct(id);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        String normalizedSku = normalizeSku(request.sku());

        if (productRepository.existsBySkuIgnoreCase(normalizedSku)) {
            throw new DuplicateResourceException("Product SKU already exists: " + normalizedSku);
        }

        Category category = findCategory(request.categoryId());
        Supplier supplier = findSupplier(request.supplierId());

        Product product = new Product();
        product.setSku(normalizedSku);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setUnitPrice(request.unitPrice());
        product.setReorderLevel(request.reorderLevel());
        product.setStatus(ProductStatus.ACTIVE);

        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = findProduct(id);

        if (request.sku() != null && !request.sku().isBlank()) {
            String normalizedSku = normalizeSku(request.sku());

            if (productRepository.existsBySkuIgnoreCaseAndIdNot(normalizedSku, id)) {
                throw new DuplicateResourceException("Product SKU already exists: " + normalizedSku);
            }

            product.setSku(normalizedSku);
        }

        if (request.name() != null && !request.name().isBlank()) {
            product.setName(request.name().trim());
        }

        if (request.description() != null) {
            product.setDescription(request.description());
        }

        if (request.categoryId() != null) {
            product.setCategory(findCategory(request.categoryId()));
        }

        if (request.supplierId() != null) {
            product.setSupplier(findSupplier(request.supplierId()));
        }

        if (request.unitPrice() != null) {
            product.setUnitPrice(request.unitPrice());
        }

        if (request.reorderLevel() != null) {
            product.setReorderLevel(request.reorderLevel());
        }

        if (request.status() != null) {
            product.setStatus(request.status());
        }

        Product updatedProduct = productRepository.save(product);
        return ProductResponse.from(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private Supplier findSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }

    private Specification<Product> buildSpecification(
            String search,
            Long categoryId,
            Long supplierId,
            ProductStatus status
    ) {
        return (root, query, criteriaBuilder) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";

                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
                Predicate skuMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), pattern);
                Predicate descriptionMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);

                predicates.add(criteriaBuilder.or(nameMatch, skuMatch, descriptionMatch));
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (supplierId != null) {
                predicates.add(criteriaBuilder.equal(root.get("supplier").get("id"), supplierId));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
