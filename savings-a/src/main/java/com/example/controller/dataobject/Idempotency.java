package com.example.controller.dataobject;
import static com.example.util.GeneralConstants.IDEMPOTENCY_CODE_MAX_SIZE;

import javax.validation.constraints.Size;

public record Idempotency(@Size(max = IDEMPOTENCY_CODE_MAX_SIZE) String code) {
}
