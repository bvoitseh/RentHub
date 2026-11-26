package com.bohakka.renthub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** FR-18: the target's single public reply, up to 1000 characters. */
public record ReplyRequest(@NotBlank @Size(max = 1000) String text) {
}
