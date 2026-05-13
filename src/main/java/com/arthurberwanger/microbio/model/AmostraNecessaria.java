package com.arthurberwanger.microbio.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "amostra_necessaria")
@Data
@NoArgsConstructor
public class AmostraNecessaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String tipo;

    @Column(length = 60)
    private String quantidade;

    @Column(length = 120)
    private String conservacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analise_id")
    @JsonIgnore
    private Analise analise;
}
