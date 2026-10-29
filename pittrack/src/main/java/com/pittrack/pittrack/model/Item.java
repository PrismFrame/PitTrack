package com.pittrack.pittrack.model;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity 
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sku;
    private String name;
    private int quantity;
    private String location;
    private String description;
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

    public void addAttribute(String string, List<Item> allItems) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addAttribute'");
    }
}
