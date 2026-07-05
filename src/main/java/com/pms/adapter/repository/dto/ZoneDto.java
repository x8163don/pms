package com.pms.adapter.repository.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Persisted only as a child of ParkingLotDto's cascaded @OneToMany —
 * the "lot_id" FK column is managed by ParkingLotDto's @JoinColumn.
 */
@Entity
@Table(name = "zone")
public class ZoneDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String floor;

    @Column(name = "name", nullable = false)
    private int name;

    @Column(name = "regular_total", nullable = false)
    private int regularTotal;

    @Column(name = "regular_remain", nullable = false)
    private int regularRemain;

    @Column(name = "flexible_total", nullable = false)
    private int flexibleTotal;

    @Column(name = "flexible_remain", nullable = false)
    private int flexibleRemain;

    @Column(name = "all_total", nullable = false)
    private int allTotal;

    @Column(name = "all_remain", nullable = false)
    private int allRemain;

    @Column(nullable = false)
    private int color;

    @Column(nullable = false)
    private int note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getRegularTotal() {
        return regularTotal;
    }

    public void setRegularTotal(int regularTotal) {
        this.regularTotal = regularTotal;
    }

    public int getRegularRemain() {
        return regularRemain;
    }

    public void setRegularRemain(int regularRemain) {
        this.regularRemain = regularRemain;
    }

    public int getFlexibleTotal() {
        return flexibleTotal;
    }

    public void setFlexibleTotal(int flexibleTotal) {
        this.flexibleTotal = flexibleTotal;
    }

    public int getFlexibleRemain() {
        return flexibleRemain;
    }

    public void setFlexibleRemain(int flexibleRemain) {
        this.flexibleRemain = flexibleRemain;
    }

    public int getAllTotal() {
        return allTotal;
    }

    public void setAllTotal(int allTotal) {
        this.allTotal = allTotal;
    }

    public int getAllRemain() {
        return allRemain;
    }

    public void setAllRemain(int allRemain) {
        this.allRemain = allRemain;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }
}
