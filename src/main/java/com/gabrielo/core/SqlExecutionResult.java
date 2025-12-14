package com.gabrielo.core;

import com.gabrielo.storage.Record;

import java.util.List;

public record SqlExecutionResult(Boolean isSuccess, String message, List<Record> queryResult) {
}
