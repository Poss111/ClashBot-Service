package com.poss.clash.bot.daos.models;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

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

}
