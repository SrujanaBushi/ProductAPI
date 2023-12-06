package com.test.productApi;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.sql.Date;

@Entity // This tells Hibernate to make a table out of this class
@Data
public class Product {
    @Id
    private Integer id;

    @NotNull
    @Column(nullable = false)
    private String name;
// Max value allows only the price if it is less than 1000
    @NotNull
    @Max(value = 10000, message = "Price cannot be 10000 or more")
    @Column(nullable = false)
    private double price;
    @Column(nullable = false)
    private Date postedDate;
    @Column(nullable = false)
    private String status;

}