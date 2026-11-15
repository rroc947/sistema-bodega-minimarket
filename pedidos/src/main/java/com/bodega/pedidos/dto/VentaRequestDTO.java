package com.bodega.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class VentaRequestDTO {
    @NotEmpty(message = "La venta debe contener al menos un producto")
    @Valid
    private List<ItemVentaDTO> items;
}
