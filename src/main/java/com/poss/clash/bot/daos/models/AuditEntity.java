package com.poss.clash.bot.daos.models;

import lombok.Data;
import org.springframework.data.annotation.*;

import java.io.Serializable;
import java.time.Instant;

@Data
public class AuditEntity implements Serializable {

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
