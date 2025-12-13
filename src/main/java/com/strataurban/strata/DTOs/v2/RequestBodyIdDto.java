package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body to get Id")
public class RequestBodyIdDto {

    @Schema(description = "Primary Id of the Entity", example = "1,2,23,9")
    private Long id;

    @Schema(description = "Primary Id of the Entity", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private String idString;
}
