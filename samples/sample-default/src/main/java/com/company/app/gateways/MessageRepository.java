package com.company.app.gateways;

import io.soffa.foundation.annotations.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, String> {
}
