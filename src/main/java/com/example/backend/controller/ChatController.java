package com.example.backend.controller;

import java.util.List; // <-- SỬA LỖI: Import còn thiếu

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
// <-- SỬA LỖI: Xóa import @RequestBody sai của Swagger
import org.springframework.web.bind.annotation.RequestBody; // <-- SỬA LỖI: Import đúng @RequestBody của Spring
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.dto.chat.ConversationDto;
import com.example.backend.dto.chat.ConversationSummaryDto;
import com.example.backend.dto.chat.MessageDto;
import com.example.backend.dto.chat.ReadReceiptRequest;
import com.example.backend.dto.chat.SendMessageRequest;
import com.example.backend.dto.chat.TypingEventRequest;
import com.example.backend.domain.entity.Message;
import com.example.backend.service.ChatService; // <-- SỬA LỖI: Import
import com.example.backend.service.FileStorageService; // <-- SỬA LỖI: Import
// import com.example.backend.service.UserService; // (Không cần thiết nếu chỉ dùng resolveUserId)

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor; // <-- SỬA LỖI: Thêm import

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor // <-- SỬA LỖI: Thêm để tự động inject các service (constructor)
public class ChatController {

    // private final UserService userService; // (Không cần nếu dùng resolveUserId)
    
    // <-- SỬA LỖI: Khai báo và inject các service bị thiếu
    private final ChatService chatService;
    private final FileStorageService fileStorageService;
    private final UserService userService;


    @PostMapping("/send")
    public ResponseEntity<MessageDto> sendMessage(@Valid @RequestBody SendMessageRequest body, HttpServletRequest httpRequest) { // <-- SỬA LỖI: Đổi tên DTO và inject HttpServletRequest
        Integer senderId = resolveUserId(httpRequest); // <-- SỬA LỖI: Lấy senderId từ httpRequest
        MessageDto messageDto = chatService.sendMessage(senderId, body); // <-- SỬA LỖI: Dùng DTO 'body'
        return ResponseEntity.ok(messageDto);
    }

    @PostMapping("/upload_image")
    public ResponseEntity<MessageDto> uploadImage(
            @RequestParam("receiverId") Integer receiverId,
            @RequestParam("image") MultipartFile imageFile,
            HttpServletRequest httpRequest) { // <-- SỬA LỖI: Inject HttpServletRequest

        Integer senderId = resolveUserId(httpRequest); // <-- SỬA LỖI: Thống nhất dùng resolveUserId
        String fileUrl = fileStorageService.storeFile(imageFile);

        SendMessageRequest request = new SendMessageRequest();
        request.setReceiverId(receiverId);
        request.setMessageType(Message.MessageType.IMAGE);
        request.setContent(fileUrl);

        MessageDto messageDto = chatService.sendMessage(senderId, request);
        return ResponseEntity.ok(messageDto);
    }

    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<MessageDto>> getHistory(@PathVariable("conversationId") Integer conversationId, HttpServletRequest httpRequest) {
        Integer currentUserId = resolveUserId(httpRequest);
        List<MessageDto> history = chatService.getMessageHistory(currentUserId, conversationId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/typing")
    public ResponseEntity<Void> sendTypingEvent(@Valid @RequestBody TypingEventRequest body, HttpServletRequest httpRequest) { // <-- SỬA LỖI: Inject HttpServletRequest
        Integer senderId = resolveUserId(httpRequest); // <-- SỬA LỖI: Thống nhất dùng resolveUserId
        chatService.handleTypingEvent(senderId, body);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read")
    public ResponseEntity<Void> sendReadReceipt(@Valid @RequestBody ReadReceiptRequest body, HttpServletRequest httpRequest) { // <-- SỬA LỖI: Inject HttpServletRequest
        Integer senderId = resolveUserId(httpRequest); // <-- SỬA LỖI: Thống nhất dùng resolveUserId
        
        // TODO: Logic check quyền. 'senderId' (người gửi request) phải là NGƯỜI NHẬN tin nhắn này.
        // Logic hiện tại chỉ check 'message.getReadAt() == null'
        chatService.handleReadReceipt(senderId, body);
        return ResponseEntity.ok().build();
    }

    /**
     * Helper để lấy User ID từ nhiều nguồn (Header, SecurityContext)
     * Đây là implementation bạn đã cung cấp.
     */
    private int resolveUserId(HttpServletRequest request) {
        // 1) Dev fallback: header X-USER-ID
        String header = request.getHeader("X-USER-ID");
        // System.out.println(request); // (Nên xóa dòng này trong production)
        if (header != null && !header.isBlank()) {
            return Integer.parseInt(header);
        }

        // 2) Lấy từ SecurityContext do JwtFilter set
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            // a) JwtFilter set principal = userId dạng String
            if (principal instanceof String s) {
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException ignored) {
                }
            }

            // b) JwtFilter set principal = UserDetails
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
                // giả định username là userId (ví dụ "1")
                try {
                    return Integer.parseInt(ud.getUsername());
                } catch (NumberFormatException ignored) {
                }
            }

            // c) JwtFilter set principal = CustomUserPrincipal có getId()
            try {
                var m = principal.getClass().getMethod("getId");
                Object id = m.invoke(principal);
                if (id != null)
                    return Integer.parseInt(String.valueOf(id));
            } catch (Exception ignored) {
            }

            // d) JwtFilter nhét userId vào details (Map hoặc object khác)
            Object details = auth.getDetails();
            if (details instanceof java.util.Map<?, ?> map) {
                Object id = map.get("userId");
                if (id != null)
                    return Integer.parseInt(String.valueOf(id));
            }
        }

        // 3) JwtFilter đặt attribute trên request (phòng hờ)
        Object attr = request.getAttribute("userId");
        if (attr != null) {
            return Integer.parseInt(String.valueOf(attr));
        }

        // 4) Không có gì -> 401
        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Unauthenticated");
    }

    /**
     * Customer: Lấy conversation với admin
     */
    @GetMapping("/conversation")
    public ResponseEntity<ConversationDto> getCustomerConversation(HttpServletRequest httpRequest) {
        Integer customerId = resolveUserId(httpRequest);
        ConversationDto conversation = chatService.getCustomerConversationWithAdmin(customerId);
        return ResponseEntity.ok(conversation);
    }

    /**
     * Customer: Lấy số tin nhắn chưa đọc
     */
    @GetMapping("/unread-count/{conversationId}")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable Integer conversationId, HttpServletRequest httpRequest) {
        Integer customerId = resolveUserId(httpRequest);
        Integer unreadCount = chatService.getUnreadCountForCustomer(customerId, conversationId);
        return ResponseEntity.ok(unreadCount);
    }

    /**
     * Admin: Lấy danh sách tất cả conversations
     */
    @GetMapping("/admin/conversations")
    public ResponseEntity<Page<ConversationSummaryDto>> getAllConversations(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size,
        @RequestParam(name = "sort", defaultValue = "createdAt,desc") String[] sort // simple support
) {
    Sort sortObj = Sort.by(Sort.Order.desc("createdAt")); // default
    try {
        if (sort != null && sort.length > 0) {
            String[] parts = sort[0].split(",");
            if (parts.length == 2) {
                sortObj = Sort.by("asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC, parts[0]);
            } else {
                sortObj = Sort.by(parts[0]);
            }
        }
    } catch (Exception e) {
        // fallback to default
    }
    Pageable pageable = PageRequest.of(page, size, sortObj);
    Page<ConversationSummaryDto> resp = chatService.getAllConversationsForAdmin(pageable);
    return ResponseEntity.ok(resp);
}

    @GetMapping("/search")
public ResponseEntity<Page<ConversationSummaryDto>> searchConversations(
        @RequestParam("customerName") String customerName,
        Pageable pageable
) {
    // Lấy adminId (bạn cần tự implement logic này, ví dụ: lấy từ User Service)
    Integer adminId = userService.getAdminUserId(); 

    Page<ConversationSummaryDto> results = chatService.searchConversationsByCustomerNameForAdmin(
            customerName,
            pageable,
            adminId
    );
    return ResponseEntity.ok(results);
}
}

