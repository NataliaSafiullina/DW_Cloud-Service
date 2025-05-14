package ru.safiullina.dwCloudService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class FileListResponse {

    private String filename;
    private Integer size;
}
