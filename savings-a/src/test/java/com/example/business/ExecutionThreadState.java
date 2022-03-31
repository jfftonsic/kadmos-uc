package com.example.business;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExecutionThreadState {
    MultiThreadedBalanceService balanceService;
}
