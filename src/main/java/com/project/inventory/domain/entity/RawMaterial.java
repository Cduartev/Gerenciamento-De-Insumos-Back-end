package com.project.inventory.domain.entity;

import com.project.inventory.domain.enumtype.UnitOfMeasurement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "raw_materials", uniqueConstraints ={@UniqueConstraint(name = "uk_raw_material_code", columnNames = "code")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "stock_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measurement", nullable = false, length = 20)
    private UnitOfMeasurement unitOfMeasurement;
}
