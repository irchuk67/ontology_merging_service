package ua.kpi.ipze.ontology.service.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.kpi.ipze.ontology.dto.ClassConflictMessage;
import ua.kpi.ipze.ontology.dto.ClassRelation;
import ua.kpi.ipze.ontology.service.MessageCollectorService;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class WebSocketIOService implements IOService {

    private final String sessionId;
    private final WebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public ClassRelation askForRelation(String class1, String class2) {
        try {
            ClassConflictMessage classConflictMessage = new ClassConflictMessage(class1, class2, Arrays.stream(ClassRelation.values()).toList());
            String message = objectMapper.writeValueAsString(classConflictMessage);
            webSocketHandler.sendMessageToClient(sessionId, message);
            String clientAnswer = webSocketHandler.waitForClientMessage(sessionId);
            log.info("ClientAnswer: {}", clientAnswer);
            return ClassRelation.fromName(clientAnswer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPerformedActions(MessageCollectorService messageCollectorService) {
        try {
           String message = objectMapper.writeValueAsString(messageCollectorService.getPerformedActions());
           webSocketHandler.sendMessageToClient(sessionId, message);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
