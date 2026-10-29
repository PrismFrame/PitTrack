package com.pittrack.pittrack.item;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity 
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @PositiveOrZero(message = "Quantity cannot be negative")
    @Column(nullable = false)
    private int quantity;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String location;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public Item() {}

    /** Get the ID of the item.
     * 
     * @return The ID of the item.
     */
    public Long getId() {
        return id;
    }

    /** Get the SKU of the item.
     * 
     * @return The SKU of the item.
     */
    public String getSku() {
        return sku;
    }

    /** Set the SKU of the item.
     * 
     * @param sku The SKU of the item.
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /** Get the name of the item.
     * 
     * @return The name of the item.
     */
    public String getName() {
        return name;
    }

    /** Set the name of the item.
     * 
     * @param name The name of the item.
     */
    public void setName(String name) {
        this.name = name;
    }

    /** Get the location of the item.
     * 
     * @return The location of the item.
     */
    public String getLocation() {
        return location;
    }

    /** Set the location of the item.
     * 
     * @param location The location of the item.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /** Get the description of the item.
     * 
     * @return The description of the item.
     */
    public String getDescription() {
        return description;
    }

    /** Set the description of the item.
     * 
     * @param description The description of the item.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Get the quantity of the item.
     * 
     * @return The quantity of the item.
     */
    public int getQuantity() {
        return quantity;
    }

    /** Set the quantity of the item.
     * 
     * @param quantity The quantity of the item.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /** Get the price of the item.
     * 
     * @return The price of the item.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /** Set the price of the item.
     * 
     * @param price The price of the item.
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
