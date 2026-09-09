package com.schoolmanagement.app_admin_Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "global_announcements")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalAnnouncement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String targetAudience; // ALL, ADMINS, OWNERS

    private String sentBy;

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
