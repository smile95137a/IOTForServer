package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "special_date")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate date;


    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime openTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime closeTime;

    private Double regularRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    @JsonBackReference("specialDateReference")
    private Store store;

    @OneToMany(mappedBy = "specialDate", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SpecialTimeSlot> timeSlots = new ArrayList<>();

    // Getters and setters
}
