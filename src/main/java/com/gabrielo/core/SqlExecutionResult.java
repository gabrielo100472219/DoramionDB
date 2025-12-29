package com.gabrielo.core;

import com.gabrielo.backend.Record;

import java.util.List;

public record SqlExecutionResult(Boolean isSuccess, String message, List<Record> queryResult) {
}
