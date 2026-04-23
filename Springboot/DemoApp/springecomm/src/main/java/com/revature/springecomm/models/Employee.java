package com.revature.springecomm.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employee entity")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    @Column(name = "email", unique = true, length = 100)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "d_id")
//    @JsonIgnore
//    @JsonBackReference
    private Department department;

    private double salary;
    @Transient
    private int tax;
    @Lob
    private String description;
//    @Temporal(TemporalType.DATE)
    private Date dob;//

    public enum Status{
        ACTIVE, INACTIVE, EXPIRED
    }
    @Enumerated(EnumType.STRING)
    private Status status;


}
