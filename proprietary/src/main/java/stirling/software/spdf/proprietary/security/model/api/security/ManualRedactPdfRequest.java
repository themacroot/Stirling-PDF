package stirling.software.spdf.proprietary.security.model.api.security;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;

import stirling.software.spdf.proprietary.security.model.api.PDFWithPageNums;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManualRedactPdfRequest extends PDFWithPageNums {
    @Schema(description = "A list of areas that should be redacted")
    private List<RedactionArea> redactions;

    @Schema(description = "Convert the redacted PDF to an image", defaultValue = "false")
    private boolean convertPDFToImage;

    @Schema(description = "The color used to fully redact certain pages")
    private String pageRedactionColor;
}
