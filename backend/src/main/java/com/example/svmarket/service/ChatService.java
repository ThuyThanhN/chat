package com.example.svmarket.service;

import com.example.svmarket.dto.ChatConversationResponse;
import com.example.svmarket.dto.ChatMessageResponse;
import com.example.svmarket.dto.ChatSendMessageRequest;
import com.example.svmarket.entity.Conversation;
import com.example.svmarket.entity.Image;
import com.example.svmarket.entity.Listing;
import com.example.svmarket.entity.Message;
import com.example.svmarket.entity.User;
import com.example.svmarket.repository.ConversationRepository;
import com.example.svmarket.repository.ListingRepository;
import com.example.svmarket.repository.MessageRepository;
import com.example.svmarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    // Tao moi hoac lay lai hoi thoai giua buyer va seller theo bai dang.
    @Transactional
    public ChatConversationResponse startConversation(String email, Integer listingId) {
        User buyer = getUserByEmail(email);
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài đăng"));

        User seller = listing.getSeller();
        if (seller == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài đăng chưa có người bán");
        }

        if (buyer.getId().equals(seller.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bạn không thể tự nhắn tin cho chính mình");
        }

        Conversation conversation = conversationRepository
                .findByBuyerIdAndSellerIdAndListingId(buyer.getId(), seller.getId(), listing.getId())
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder()
                                .buyer(buyer)
                                .seller(seller)
                                .listing(listing)
                                .updatedAt(LocalDateTime.now())
                                .build()));

        long unreadCount = messageRepository.countUnreadMessages(conversation.getId(), buyer.getId());
        return toConversationResponse(conversation, buyer.getId(), unreadCount);
    }

    // Lay danh sach hoi thoai cua nguoi dang nhap.
    @Transactional(readOnly = true)
    public List<ChatConversationResponse> getMyConversations(String email) {
        User currentUser = getUserByEmail(email);

        return conversationRepository.findByParticipantIdOrderByUpdatedAtDesc(currentUser.getId())
                .stream()
                .map(conversation -> {
                    long unreadCount = messageRepository.countUnreadMessages(conversation.getId(), currentUser.getId());
                    return toConversationResponse(conversation, currentUser.getId(), unreadCount);
                })
                .toList();
    }

    // Lay tin nhan cua 1 hoi thoai va danh dau da doc cho phia nguoi dang nhap.
    @Transactional
    public List<ChatMessageResponse> getConversationMessages(String email, Integer conversationId) {
        User currentUser = getUserByEmail(email);
        Conversation conversation = getConversationAndValidateMember(conversationId, currentUser.getId());

        messageRepository.markConversationAsRead(conversation.getId(), currentUser.getId());

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(message -> toMessageResponse(message, currentUser.getId()))
                .toList();
    }

    // Danh dau tat ca tin nhan trong hoi thoai la da doc.
    @Transactional
    public void markConversationAsRead(String email, Integer conversationId) {
        User currentUser = getUserByEmail(email);
        Conversation conversation = getConversationAndValidateMember(conversationId, currentUser.getId());
        messageRepository.markConversationAsRead(conversation.getId(), currentUser.getId());
    }

    // Luu tin nhan vao DB va day realtime den 2 thanh vien trong hoi thoai.
    @Transactional
    public ChatMessageResponse sendMessage(String email, ChatSendMessageRequest request) {
        User sender = getUserByEmail(email);
        Conversation conversation = getConversationAndValidateMember(request.getConversationId(), sender.getId());

        String content = request.getContent() != null ? request.getContent().trim() : "";
        if (content.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung tin nhắn không được để trống");
        }

        Message savedMessage = messageRepository.save(
                Message.builder()
                        .conversation(conversation)
                        .sender(sender)
                        .content(content)
                        .isRead(false)
                        .build());

        conversation.setLastMessage(content);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatMessageResponse payload = toMessageResponse(savedMessage, sender.getId());

        // Gửi về kênh cá nhân của buyer và seller để đồng bộ realtime.
        simpMessagingTemplate.convertAndSendToUser(conversation.getBuyer().getEmail(), "/queue/messages", payload);
        simpMessagingTemplate.convertAndSendToUser(conversation.getSeller().getEmail(), "/queue/messages", payload);

        return payload;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));
    }

    private Conversation getConversationAndValidateMember(Integer conversationId, Integer userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hội thoại"));

        boolean isBuyer = conversation.getBuyer() != null && conversation.getBuyer().getId().equals(userId);
        boolean isSeller = conversation.getSeller() != null && conversation.getSeller().getId().equals(userId);

        if (!isBuyer && !isSeller) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập hội thoại này");
        }

        return conversation;
    }

    private ChatConversationResponse toConversationResponse(Conversation conversation, Integer currentUserId, long unreadCount) {
        boolean amBuyer = conversation.getBuyer() != null && conversation.getBuyer().getId().equals(currentUserId);
        User partner = amBuyer ? conversation.getSeller() : conversation.getBuyer();

        Listing listing = conversation.getListing();
        String listingThumbnail = null;
        if (listing != null && listing.getImages() != null && !listing.getImages().isEmpty()) {
            Image firstImage = listing.getImages().get(0);
            listingThumbnail = firstImage != null ? firstImage.getUrl() : null;
        }

        return ChatConversationResponse.builder()
                .conversationId(conversation.getId())
                .listingId(listing != null ? listing.getId() : null)
                .listingTitle(listing != null ? listing.getTitle() : "")
                .listingThumbnail(listingThumbnail)
                .listingPrice(listing != null ? listing.getPrice() : null)
                .partnerId(partner != null ? partner.getId() : null)
                .partnerName(partner != null ? partner.getFullName() : "")
                .partnerAvatar(partner != null ? partner.getAvatar() : null)
                .partnerUniversity(partner != null ? partner.getUniversity() : null)
                .lastMessage(conversation.getLastMessage())
                .updatedAt(conversation.getUpdatedAt())
                .unreadCount(unreadCount)
                .build();
    }

    private ChatMessageResponse toMessageResponse(Message message, Integer currentUserId) {
        User sender = message.getSender();

        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(sender != null ? sender.getId() : null)
                .senderName(sender != null ? sender.getFullName() : "")
                .senderAvatar(sender != null ? sender.getAvatar() : null)
                .content(message.getContent())
                .isRead(message.getIsRead())
                .isMine(sender != null && sender.getId().equals(currentUserId))
                .createdAt(message.getCreatedAt())
                .build();
    }
}
