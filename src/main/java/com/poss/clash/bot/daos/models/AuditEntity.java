package com.poss.clash.bot.daos.models;

import org.springframework.data.annotation.*;

import java.time.Instant;

public class AuditEntity {

    @CreatedDate
    Instant createdDate;

    @CreatedBy
    String createdBy;

    @LastModifiedDate
    Instant lastModifiedDate;

    @LastModifiedBy
    String lastModifiedBy;

    @Version
    Integer version;

}
