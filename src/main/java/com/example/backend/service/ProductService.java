package com.example.backend.service;

import com.example.backend.domain.entity.*;
import com.example.backend.dto.product.*;
import com.example.backend.exception.custom.ProductAlreadyExistsException;
import com.example.backend.exception.custom.PublisherAlreadyExistsException;
import com.example.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
