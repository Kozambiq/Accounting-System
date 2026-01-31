/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.raven.main;

/**
 *
 * @author k2
 */
public class Account {
    private String name;
    private String type;
    private double value;
    
    Account(String name, String type, double value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getType() {
        return this.type;
    }
    
    public double getValue() {
        return this.value;
    }
}
