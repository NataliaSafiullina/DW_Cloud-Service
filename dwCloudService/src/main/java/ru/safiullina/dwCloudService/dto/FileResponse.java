package ru.safiullina.dwCloudService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class FileResponse {
    private String hash;
    private String file;

}
