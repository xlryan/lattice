package com.lattice.core.event;

import com.lattice.core.domain.DomainType;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.UUID;

public class LatticeNodeIngestedEvent extends ApplicationEvent {

    private final UUID nodeId;
    private final DomainType domain;
    private final String content;
    private final List<String> tags;

    public LatticeNodeIngestedEvent(Object source, UUID nodeId, DomainType domain, String content, List<String> tags) {
        super(source);
        this.nodeId = nodeId;
        this.domain = domain;
        this.content = content;
        this.tags = tags;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public DomainType getDomain() {
        return domain;
    }

    public String getContent() {
        return content;
    }

    public List<String> getTags() {
        return tags;
    }
}
