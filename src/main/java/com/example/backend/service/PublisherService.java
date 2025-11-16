package com.example.backend.service;

import com.example.backend.domain.entity.Publisher;
import com.example.backend.dto.publisher.CreatePublisherRequest;
import com.example.backend.dto.publisher.PublisherResponse;
import com.example.backend.exception.custom.PublisherNotFoundException;
import com.example.backend.exception.custom.PublisherAlreadyExistsException;
import com.example.backend.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PublisherService {

    private final PublisherRepository publisherRepository;

    /**
     * Tạo mới một publisher
     */
    public PublisherResponse createPublisher(CreatePublisherRequest request) {
        // Kiểm tra xem publisher name đã tồn tại chưa
        if (publisherRepository.findByNameContainingIgnoreCase(request.getName()).stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(request.getName()))) {
            throw new PublisherAlreadyExistsException(request.getName());
        }

        Publisher publisher = Publisher.builder()
                .name(request.getName())
                .foundedYear(request.getFoundedYear())
                .build();

        try {
            Publisher savedPublisher = publisherRepository.save(publisher);
            return convertToResponse(savedPublisher);
        } catch (DataIntegrityViolationException e) {
            throw new PublisherAlreadyExistsException(request.getName(), e);
        }
    }

    /**
     * Lấy tất cả publishers
     */
    @Transactional(readOnly = true)
    public List<PublisherResponse> getAllPublishers() {
        return publisherRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy publisher theo ID
     */
    @Transactional(readOnly = true)
    public PublisherResponse getPublisherById(Integer id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new PublisherNotFoundException(id));
        return convertToResponse(publisher);
    }

    /**
     * Cập nhật publisher
     */
    public PublisherResponse updatePublisher(Integer id, CreatePublisherRequest request) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new PublisherNotFoundException(id));
        
        // Chỉ update name nếu không rỗng và khác null
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            // Kiểm tra xem tên mới có trùng với publisher khác không
            List<Publisher> existingPublishers = publisherRepository.findByNameContainingIgnoreCase(request.getName());
            boolean nameExists = existingPublishers.stream()
                    .anyMatch(p -> !p.getId().equals(id) && p.getName().equalsIgnoreCase(request.getName()));
            
            if (nameExists) {
                throw new PublisherAlreadyExistsException(request.getName());
            }
            
            publisher.setName(request.getName().trim());
        }
        
        // Chỉ update foundedYear nếu không null
        if (request.getFoundedYear() != null) {
            publisher.setFoundedYear(request.getFoundedYear());
        }
        
        try {
            Publisher updatedPublisher = publisherRepository.save(publisher);
            return convertToResponse(updatedPublisher);
        } catch (DataIntegrityViolationException e) {
            throw new PublisherAlreadyExistsException(request.getName(), e);
        }
    }

    /**
     * Xóa publisher theo ID
     */
    public void deletePublisher(Integer id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new PublisherNotFoundException(id));
        publisherRepository.delete(publisher);
    }

    /**
     * Tìm kiếm publishers theo tên
     */
    @Transactional(readOnly = true)
    public List<PublisherResponse> searchPublishersByName(String name) {
        return publisherRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra publisher có tồn tại không
     */
    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return publisherRepository.existsById(id);
    }

    /**
     * Chuyển đổi từ Entity sang Response DTO
     */
    private PublisherResponse convertToResponse(Publisher publisher) {
        return PublisherResponse.builder()
                .id(publisher.getId())
                .name(publisher.getName())
                .foundedYear(publisher.getFoundedYear())
                .build();
    }
}
