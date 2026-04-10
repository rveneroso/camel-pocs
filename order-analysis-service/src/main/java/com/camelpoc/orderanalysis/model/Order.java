package com.camelpoc.orderanalysis.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {

    @JsonProperty("order_id")
    private String orderId;

    private double total;

    private Payment payment;

    private Customer customer;

    // getters e setters

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}