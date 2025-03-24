package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to add additional stops to a trip")
public class AdditionalStopsRequest {

    @Schema(description = "List of additional stops to add", example = "[\"Stop 1\", \"Stop 2\"]")
    private List<String> stops;
}