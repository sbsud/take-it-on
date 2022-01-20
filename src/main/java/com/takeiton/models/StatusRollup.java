package com.takeiton.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StatusRollup {

    @JsonInclude
    long inProgress = 0;

    @JsonInclude
    long notStarted = 0;

    @JsonInclude
    long done = 0;
}
