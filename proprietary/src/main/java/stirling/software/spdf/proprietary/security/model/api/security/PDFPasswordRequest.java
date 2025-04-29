package stirling.software.spdf.proprietary.security.model.api.security;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;

import stirling.software.spdf.proprietary.security.model.api.PDFFile;

@Data
@EqualsAndHashCode(callSuper = true)
public class PDFPasswordRequest extends PDFFile {

    @Schema(
            description = "The password of the PDF file",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
