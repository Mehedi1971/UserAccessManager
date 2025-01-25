package com.mahedi.useraccessmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response {

    private Long timeStamp;

    private int statusCode;

    private String status;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int numberOfElement;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long rowCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ErrorResponseDto> errors;
}