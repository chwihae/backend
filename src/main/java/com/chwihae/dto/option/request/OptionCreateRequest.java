package com.chwihae.dto.option.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OptionCreateRequest {

    @Size(max = 100)
    @NotBlank
    private String name;
}
