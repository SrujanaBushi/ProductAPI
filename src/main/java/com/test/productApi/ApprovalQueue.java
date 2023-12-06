package com.test.productApi;

import jakarta.persistence.*;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.cache.spi.entry.StructuredCacheEntry;

import java.sql.Date;
@Entity
@Data
public class ApprovalQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer approvalid;
    @NotNull
    @Column(nullable = false)
    private Integer id;
    @NotNull
    @Column(nullable = false)
    private String name;
    @NotNull
    @Column(nullable = false)
    private double price;
    @Column(nullable = false)
    private Date postedDate;
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String ApprovalStatus;

    @Column(nullable = false)
    private Date ApprovalRequestDate;

    @Column(nullable = false)
    private String RequestType;

}
