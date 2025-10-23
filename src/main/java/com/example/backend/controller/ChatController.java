package com.example.backend.controller;

import java.util.List; // <-- SỬA LỖI: Import còn thiếu

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
// <-- SỬA LỖI: Xóa import @RequestBody sai của Swagger
import org.springframework.web.bind.annotation.RequestBody; // <-- SỬA LỖI: Import đúng @RequestBody của Spring
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
import lombok.RequiredArgsConstructor; // <-- SỬA LỖI: Thêm import

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor // <-- SỬA LỖI: Thêm để tự động inject các service (constructor)
public class ChatController {

    // private final UserService userService; // (Không cần nếu dùng resolveUserId)
    
    // <-- SỬA LỖI: Khai báo và inject các service bị thiếu
    private final ChatService chatService;
    private final FileStorageService fileStorageService;


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
    public ResponseEntity<List<MessageDto>> getHistory(@PathVariable Integer conversationId) {
        // TODO: Nên check quyền xem user hiện tại có trong conversationId này không
        // Integer currentUserId = resolveUserId(httpRequest); // (Thêm httpRequest nếu cần check)
        return ResponseEntity.ok(chatService.getMessageHistory(conversationId));
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
}