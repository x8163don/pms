package com.pms.adapter.repository.dto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parking_lot")
public class ParkingLotDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fab_id", nullable = false)
    private Long fabId;

    @Column(nullable = false)
    private String name;

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

    /**
     * Zone is a child entity of this aggregate: cascading + orphanRemoval means
     * saving/deleting a ParkingLot saves/deletes its Zones too, with no separate
     * ZoneRepository. @OrderColumn keeps list order stable across save/reload,
     * which ParkingLotUseCaseImpl.addZone relies on to find the newly added Zone.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "lot_id", nullable = false)
    @OrderColumn(name = "zone_order")
    private List<ZoneDto> zones = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFabId() {
        return fabId;
    }

    public void setFabId(Long fabId) {
        this.fabId = fabId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
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

    public List<ZoneDto> getZones() {
        return zones;
    }

    public void setZones(List<ZoneDto> zones) {
        this.zones = zones;
    }
}
