package ru.safiullina.dwCloudService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * +--------------+--------------+------+-----+---------+----------------+
 * | Field        | Type         | Null | Key | Default | Extra          |
 * +--------------+--------------+------+-----+---------+----------------+
 * | id           | bigint       | NO   | PRI | NULL    | auto_increment |
 * | file_content | longblob     | YES  |     | NULL    |                |
 * | file_name    | varchar(255) | YES  |     | NULL    |                |
 * | user_id      | bigint       | NO   | MUL | NULL    |                |
 * | hash         | varchar(255) | YES  |     | NULL    |                |
 * +--------------+--------------+------+-----+---------+----------------+
 */

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

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private User user;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "hash")
    private String hash;

    @Lob
    @Column(name = "file_content", columnDefinition = "longblob")
    private byte[] fileContent;

    public File(User user, String fileName, String hash, byte[] fileContent) {
        this.user = user;
        this.fileName = fileName;
        this.hash = hash;
        this.fileContent = fileContent;
    }
}
