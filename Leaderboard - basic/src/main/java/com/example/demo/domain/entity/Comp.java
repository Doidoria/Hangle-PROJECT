package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="comp")
@Data
@Builder
public class Comp {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long compId;
    @Column(length = 100)
    private String compName;


}
