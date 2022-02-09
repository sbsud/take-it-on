package com.takeiton.models;

import java.util.Date;

public interface IHistory {
    String getEvent();
    String getValue();
    Date getTime();
}
