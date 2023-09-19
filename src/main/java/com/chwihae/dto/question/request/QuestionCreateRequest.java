package com.chwihae.dto.question.request;

import com.chwihae.domain.question.QuestionEntity;
import com.chwihae.domain.question.QuestionType;
import com.chwihae.domain.user.UserEntity;
import com.chwihae.dto.option.request.OptionCreateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class QuestionCreateRequest {

    @Size(max = 50)
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private QuestionType type;

    @NotNull
    private LocalDateTime closeAt;

    @Size(max = 20)
    @NotEmpty
    private List<OptionCreateRequest> options;

    public QuestionEntity toEntity(UserEntity userEntity) {
        return QuestionEntity.builder()
                .userEntity(userEntity)
                .title(this.title)
                .content(this.content)
                .type(this.type)
                .closeAt(this.closeAt)
                .build();
    }

    @Builder
    private QuestionCreateRequest(String title, String content, QuestionType type, LocalDateTime closeAt) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.closeAt = closeAt;
    }
}