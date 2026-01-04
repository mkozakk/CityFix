package org.example.userservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.example.userservice.entity.User;
import org.example.userservice.event.ReportCreatedEvent;
import org.example.userservice.repository.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
public class ReportEventListener {
    private final UserRepository userRepository;

    @Autowired
    public ReportEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.user-counter:user.reports.counter.queue}")
    @Transactional
    public void handleReportCreated(
            ReportCreatedEvent event,
            @Header(value = "amqp_messageId", required = false) String messageId) {

        log.info("Received ReportCreatedEvent for user counter: userId={}, reportId={}, messageId={}",
                event.getUserId(), event.getReportId(), messageId);

        try {
            Optional<User> userOpt = userRepository.findById(event.getUserId());

            if (userOpt.isEmpty()) {
                log.warn("User not found with id={}, cannot update counter", event.getUserId());
                return;
            }

            User user = userOpt.get();
            Integer currentCount = user.getReportsCount();
            if (currentCount == null) {
                currentCount = 0;
            }

            user.setReportsCount(currentCount + 1);
            userRepository.save(user);

            log.info("Updated reports counter for userId={}: {} -> {}",
                    event.getUserId(), currentCount, currentCount + 1);

        } catch (Exception e) {
            log.error("Failed to update reports counter for userId={}: {}",
                    event.getUserId(), e.getMessage(), e);
            throw e; // Rzuć wyjątek aby RabbitMQ mógł zrobić retry
        }
    }
}

