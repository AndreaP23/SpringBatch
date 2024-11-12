package org.si2001.batch_rentalcar.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VeicoloDTO {
    private Long veicoloId;
    private String marca;
    private String modello;
    private int anno;
    private String targa;
    private int disponibilita;

}
