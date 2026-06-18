package com.pack.entity;

import com.pack.enums.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "budgets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_budget_user_category_month_year",
                        columnNames = {
                                "user_id",
                                "category",
                                "budget_month",
                                "budget_year"
                        }
                )
        },
        indexes = {
                @Index(name = "idx_budget_user", columnList = "user_id"),
                @Index(name = "idx_budget_month_year", columnList = "budget_month,budget_year")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_budget_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @NotNull
    @Min(1)
    @Max(12)
    @Column(name = "budget_month", nullable = false)
    private Integer budgetMonth;

    @NotNull
    @Column(name = "budget_year", nullable = false)
    private Integer budgetYear;

    @NotNull
    @DecimalMin("0.01")
    @Column(
            name = "amount_limit",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal amountLimit;
}