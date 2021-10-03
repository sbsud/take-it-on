package com.takeiton.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusAggregate {

    @Builder.Default
    private double doneAggregate = 0.0;

    @Builder.Default
    private double inprogressAggregate = 0.0;

    @Builder.Default
    private double notstartedAggregate = 0.0;
}
