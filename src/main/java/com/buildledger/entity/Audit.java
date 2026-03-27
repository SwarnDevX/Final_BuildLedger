package com.buildledger.entity;

import com.buildledger.enums.AuditStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "audits")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compliance_officer_id", nullable = false)
    private User complianceOfficer;

    @Column(name = "scope", nullable = false, columnDefinition = "TEXT")
    private String scope;

    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "date", nullable = false)
    private LocalDate date; // audit scheduleDate

    @Column(name = "audit_date" ,nullable= true)
    private LocalDate auditDate; // audit startDate (when audit is IN_PROGRESS)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AuditStatus status = AuditStatus.SCHEDULED;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
