package com.example.backend.service;

import com.example.backend.domain.entity.*;
import com.example.backend.dto.product.*;
import com.example.backend.exception.custom.ProductAlreadyExistsException;
import com.example.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ArtistRepository artistRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;

    // CREATE
    public Response create(Request req) {
        // Kiểm tra xem product name đã tồn tại chưa
        if (productRepository.findByNameContainingIgnoreCase(req.getName()).stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(req.getName()))) {
            throw new ProductAlreadyExistsException(req.getName());
        }

        Product product = new Product();
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setQuantity(req.getQuantity());
        product.setImageUrl(req.getImageUrl());
        product.setReleaseDate(req.getReleaseDate());

        if (req.getArtistId() != null)
            product.setArtist(artistRepository.findById(req.getArtistId())
                    .orElseThrow(() -> new EntityNotFoundException("Artist not found")));

        if (req.getPublisherId() != null)
            product.setPublisher(publisherRepository.findById(req.getPublisherId())
                    .orElseThrow(() -> new EntityNotFoundException("Publisher not found")));

        if (req.getCategoryId() != null)
            product.setCategory(categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found")));

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    // READ ALL
    public List<Response> getAll() {
        return productRepository.findAll()
                .stream().map(this::mapToResponse).toList();
    }

    // FILTER
    public List<Response>  filter(String name,
                                 Integer categoryId,
                                 Integer publisherId,
                                 Integer artistId,
                                 String priceSort,
                                 Integer releaseYearFrom,
                                 Integer releaseYearTo,
                                 BigDecimal priceMin,
                                 BigDecimal priceMax) {

        Specification<Product> spec = (root, query, cb) -> cb.conjunction();
        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }


        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("category").get("id"), categoryId));
        }
        
        if (publisherId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("publisher").get("id"), publisherId));
        }

        if (artistId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("artist").get("id"), artistId));
        }

        if (priceMin != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), priceMin));
        }
        if (priceMax != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), priceMax));
        }

        if (releaseYearFrom != null || releaseYearTo != null) {
            final int fromYear = releaseYearFrom != null ? releaseYearFrom : 0;
            final int toYear = releaseYearTo != null ? releaseYearTo : 9999;
            final LocalDate fromDate = LocalDate.of(Math.max(0, fromYear), 1, 1);
            final LocalDate toDate = LocalDate.of(Math.min(9999, toYear), 12, 31);
            spec = spec.and((root, query, cb) -> cb.between(root.get("releaseDate"), fromDate, toDate));
        }

        Sort sort = Sort.unsorted();
        if (priceSort != null) {
            String normalized = priceSort.trim().toLowerCase();
            if (normalized.equals("asc") || normalized.equals("low") || normalized.equals("low_to_high") || normalized.equals("up")) {
                sort = Sort.by(Sort.Direction.ASC, "price");
            } else if (normalized.equals("desc") || normalized.equals("high") || normalized.equals("high_to_low") || normalized.equals("down")) {
                sort = Sort.by(Sort.Direction.DESC, "price");
            }
        }

        return productRepository.findAll(spec, sort).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // READ ONE
    public Response getById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return mapToResponse(product);
    }

    // UPDATE
    public Response update(Integer id, Request req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Chỉ update name nếu không rỗng và khác null
        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            // Kiểm tra xem tên mới có trùng với publisher khác không
            List<Product> existingProducts = productRepository.findByNameContainingIgnoreCase(req.getName());
            boolean nameExists = existingProducts.stream()
                    .anyMatch(p -> !p.getId().equals(id) && p.getName().equalsIgnoreCase(req.getName()));

            if (nameExists) {
                throw new ProductAlreadyExistsException(req.getName());
            }

            product.setName(req.getName().trim());
        }

        if (req.getDescription() != null) {
            product.setDescription(req.getDescription());
        }

        if (req.getPrice() != null) {
            product.setPrice(req.getPrice());
        }

        if (req.getQuantity() != null) {
            product.setQuantity(req.getQuantity());
        }

        if (req.getImageUrl() != null) {
            product.setImageUrl(req.getImageUrl());
        }

        if (req.getReleaseDate() != null) {
            product.setReleaseDate(req.getReleaseDate());
        }
        
        if (req.getArtistId() != null)
            product.setArtist(artistRepository.findById(req.getArtistId())
                    .orElseThrow(() -> new EntityNotFoundException("Artist not found")));

        if (req.getPublisherId() != null)
            product.setPublisher(publisherRepository.findById(req.getPublisherId())
                    .orElseThrow(() -> new EntityNotFoundException("Publisher not found")));

        if (req.getCategoryId() != null)
            product.setCategory(categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found")));

        return mapToResponse(productRepository.save(product));
    }

    // DELETE
    public void delete(Integer id) {
        if (!productRepository.existsById(id))
            throw new EntityNotFoundException("Product not found");
        productRepository.deleteById(id);
    }

    // MAPPER
    private Response mapToResponse(Product p) {
        return Response.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .quantity(p.getQuantity())
                .imageUrl(p.getImageUrl())
                .artistName(p.getArtist() != null ? p.getArtist().getArtistName() : null)
                .publisherName(p.getPublisher() != null ? p.getPublisher().getName() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .releaseDate(p.getReleaseDate())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
