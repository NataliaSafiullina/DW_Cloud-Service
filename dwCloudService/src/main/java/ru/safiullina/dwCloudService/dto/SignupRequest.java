package ru.safiullina.dwCloudService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SignupRequest {

    @NotBlank
    private String login;
    @NotBlank
    private String password;

}
