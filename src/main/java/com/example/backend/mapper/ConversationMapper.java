package com.example.backend.mapper;

import com.example.backend.domain.entity.Conversation;
import com.example.backend.dto.chat.ConversationSummaryDto;
import com.example.backend.dto.chat.MessageDto;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ConversationMapper {

    /**
     * Main mapping method from Conversation -> ConversationSummaryDto
     * This implementation tries to be tolerant with various entity getter names by using reflection.
     *
     * NOTE: unreadCount is left null here. It's better to compute unreadCount in the service layer
     * where you can query Message repository (e.g. count unread messages for customer/conversation).
     */
    public ConversationSummaryDto toSummaryDto(Conversation c) {
        if (c == null) return null;

        ConversationSummaryDto dto = ConversationSummaryDto.builder().build();

        // conversationId
        Object idObj = safeInvoke(c, "getId");
        if (idObj != null) {
            dto.setConversationId(asInteger(idObj));
        }

        // participantIds
        Set<?> participants = asSet(safeInvoke(c, "getParticipants"));
        List<Integer> participantIds = participants.stream()
                .map(p -> resolveUserIdFromParticipant(p))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        dto.setParticipantIds(participantIds);

        // lastMessage and lastMessageAt
        Object lastMessageObj = safeInvoke(c, "getLastMessage"); // common pattern
        if (lastMessageObj == null) {
            // maybe conversation has getMessages() and lastMessageAt field only
            lastMessageObj = tryGetLastFromMessagesCollection(c);
        }
        if (lastMessageObj != null) {
            MessageDto mDto = mapToMessageDto(lastMessageObj);
            dto.setLastMessage(mDto);
            // lastMessageAt from conversation field first, else from message
            Object lastMsgAtObj = safeInvoke(c, "getLastMessageAt");
            if (lastMsgAtObj == null && mDto != null && mDto.getCreatedAt() != null) {
                dto.setLastMessageAt(mDto.getCreatedAt());
            } else if (lastMsgAtObj != null) {
                dto.setLastMessageAt(asInstantToEpochMillis(lastMsgAtObj));
            }
        } else {
            // fallback: conversation may have lastMessageAt only
            Object lastMsgAtObj = safeInvoke(c, "getLastMessageAt");
            if (lastMsgAtObj != null) {
                dto.setLastMessageAt(asInstantToEpochMillis(lastMsgAtObj));
            }
        }

        // Identify one "customer" participant (heuristic)
        Object customerParticipant = findCustomerParticipant(participants);
        if (customerParticipant != null) {
            Object userObj = safeInvoke(customerParticipant, "getUser", "getAccount", "getMember");
            if (userObj == null) {
                // sometimes participant itself *is* user-like
                userObj = customerParticipant;
            }
            if (userObj != null) {
                dto.setCustomerId(asInteger(safeInvoke(userObj, "getId", "getUserId")));
                dto.setCustomerName(asString(safeInvoke(userObj, "getFullName", "getName", "getUsername")));
                dto.setCustomerEmail(asString(safeInvoke(userObj, "getEmail")));
            }
        }

        // unreadCount: cannot compute reliably here (needs DB query). Leave null for service to fill.
        dto.setUnreadCount(null);

        return dto;
    }

    // ----------------------
    // Helper methods
    // ----------------------

    /** Try invoking first available method names on target, return result or null */
    private Object safeInvoke(Object target, String... methodNames) {
        if (target == null) return null;
        for (String mName : methodNames) {
            try {
                Method m = target.getClass().getMethod(mName);
                return m.invoke(target);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /** Convert possible collection (Set, List) to Set<?> */
    @SuppressWarnings("unchecked")
    private Set<?> asSet(Object obj) {
        if (obj == null) return Collections.emptySet();
        if (obj instanceof Set) return (Set<?>) obj;
        if (obj instanceof Collection) return new LinkedHashSet<>((Collection<?>) obj);
        // maybe single participant object:
        return Collections.singleton(obj);
    }

    /** Resolve user id from a ConversationParticipant object (tries several getter names) */
    private Integer resolveUserIdFromParticipant(Object participant) {
        if (participant == null) return null;
        Object userObj = safeInvoke(participant, "getUser", "getAccount", "getMember");
        if (userObj == null) {
            // maybe participant itself carries id
            Object idDirect = safeInvoke(participant, "getUserId", "getId");
            if (idDirect != null) return asInteger(idDirect);
            return null;
        }
        Object idObj = safeInvoke(userObj, "getId", "getUserId");
        return idObj != null ? asInteger(idObj) : null;
    }

    /** Map a Message entity object into your MessageDto using reflection on common fields/setters */
    private MessageDto mapToMessageDto(Object messageObj) {
        if (messageObj == null) return null;
        MessageDto dto = new MessageDto();
        // Try common getters and corresponding setters
        trySetIfPresent(dto, "setId", safeInvoke(messageObj, "getId"));
        trySetIfPresent(dto, "setSenderId", safeInvoke(messageObj, "getSenderId", "getFromId", "getSender"));
        trySetIfPresent(dto, "setReceiverId", safeInvoke(messageObj, "getReceiverId", "getToId", "getReceiver"));
        trySetIfPresent(dto, "setContent", safeInvoke(messageObj, "getContent", "getBody"));
        trySetIfPresent(dto, "setMessageType", safeInvoke(messageObj, "getMessageType", "getType"));
        // createdAt: convert Instant/Date/Long -> Long epoch millis if MessageDto expects Long
        Object createdAtObj = safeInvoke(messageObj, "getCreatedAt", "getCreatedDate", "getCreated");
        if (createdAtObj != null) {
            Long epoch = asInstantToEpochMillis(createdAtObj);
            trySetIfPresent(dto, "setCreatedAt", epoch);
        }
        Object readAtObj = safeInvoke(messageObj, "getReadAt");
        if (readAtObj != null) {
            Long epoch = asInstantToEpochMillis(readAtObj);
            trySetIfPresent(dto, "setReadAt", epoch);
        }
        return dto;
    }

    /** If target has setterName and value non-null, invoke it (reflection) */
    private void trySetIfPresent(Object target, String setterName, Object value) {
        if (target == null || value == null) return;
        try {
            Method[] methods = target.getClass().getMethods();
            for (Method m : methods) {
                if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                    Class<?> param = m.getParameterTypes()[0];
                    Object casted = castValueToType(value, param);
                    if (casted != null) {
                        m.invoke(target, casted);
                        return;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    /** Cast common types to expected param type (String, Integer, Long, Enum, etc.) */
    private Object castValueToType(Object value, Class<?> param) {
        if (value == null) return null;
        if (param.isInstance(value)) return value;
        String s = String.valueOf(value);
        try {
            if (param == Integer.class || param == int.class) return Integer.parseInt(s);
            if (param == Long.class || param == long.class) return Long.parseLong(s);
            if (param == String.class) return s;
            if (param.isEnum()) {
                // try to match enum by name
                @SuppressWarnings("rawtypes")
                Class<? extends Enum> eClass = (Class<? extends Enum>) param;
                for (Object enumConst : eClass.getEnumConstants()) {
                    if (enumConst.toString().equalsIgnoreCase(s) || enumConst.toString().equals(s)) {
                        return enumConst;
                    }
                }
            }
            // try Instant -> Long
            if (param == Long.class || param == long.class) {
                if (value instanceof Instant) return ((Instant) value).toEpochMilli();
                if (value instanceof Date) return ((Date) value).getTime();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Convert Instant/Date/Number -> epoch milli */
    private Long asInstantToEpochMillis(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Instant) return ((Instant) obj).toEpochMilli();
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof Date) return ((Date) obj).getTime();
        // try parse string
        try {
            return Long.parseLong(String.valueOf(obj));
        } catch (Exception ignored) {
        }
        return null;
    }

    private Integer asInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String asString(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    /** Heuristic: pick a "customer" participant if role matches CUSTOMER/USER, else first participant */
    private Object findCustomerParticipant(Set<?> participants) {
        if (participants == null || participants.isEmpty()) return null;
        // first try to find participant with role marker
        for (Object p : participants) {
            Object roleVal = safeInvoke(p, "getRole", "getParticipantType", "getType");
            if (roleVal != null) {
                String r = String.valueOf(roleVal).toUpperCase(Locale.ROOT);
                if (r.contains("CUSTOMER") || r.contains("USER") || r.contains("CLIENT")) {
                    return p;
                }
            }
        }
        // fallback: return first
        return participants.iterator().next();
    }

    /** If Conversation has getMessages() return last element by trying to find max createdAt */
    private Object tryGetLastFromMessagesCollection(Conversation c) {
        Object messagesObj = safeInvoke(c, "getMessages", "getMessageSet", "getMessageList");
        if (messagesObj == null) return null;
        Collection<?> messages;
        if (messagesObj instanceof Collection) {
            messages = (Collection<?>) messagesObj;
        } else {
            messages = Collections.singletonList(messagesObj);
        }
        // choose message with max createdAt if possible
        Object best = null;
        Long bestTs = Long.MIN_VALUE;
        for (Object m : messages) {
            Object created = safeInvoke(m, "getCreatedAt", "getCreatedDate", "getCreated");
            Long ts = asInstantToEpochMillis(created);
            if (ts == null) ts = Long.MIN_VALUE;
            if (best == null || ts > bestTs) {
                best = m;
                bestTs = ts;
            }
        }
        return best;
    }
}
