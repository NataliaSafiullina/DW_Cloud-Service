package ru.safiullina.dwCloudService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Blob;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "files")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User userId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_content")
    private Blob fileContent;
}
