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

    private double doneAggregate = 0.0;

    private double inprogressAggregate = 0.0;

    private double notstartedAggregate = 0.0;
}
