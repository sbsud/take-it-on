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

    private double doneAggregate;

    private double inprogressAggregate;

    private double notstartedAggregate;
}
