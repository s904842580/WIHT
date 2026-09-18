package com.waht.platform.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditWriter {
    private final AuditMapper mapper;

    public AuditWriter(AuditMapper mapper) { this.mapper = mapper; }

    // Separate bean/proxy: business rollback must not roll back audit records.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void append(AuditEvent event) {
        if (mapper.insert(event) != 1) {
            throw new IllegalStateException("Audit insert did not write one row");
        }
    }
}
