package com.lattice.core.repository.inbox;

import com.lattice.core.domain.inbox.InboxItem;
import com.lattice.core.domain.inbox.InboxItem.InboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InboxRepository extends JpaRepository<InboxItem, UUID> {

    List<InboxItem> findByStatus(InboxStatus status);
}
