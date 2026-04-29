package com.arthurberwanger.microbio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_cliente", length = 50)
    private String tipoCliente;

    @Column(name = "cpf_cnpj", unique = true, length = 20)
    private String cpfCnpj;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro = LocalDateTime.now();

    /*
     * CORREÇÃO: era cascade = CascadeType.PERSIST apenas.
     * Com só PERSIST, ao editar um endereço já existente o Hibernate
     * tentava inserir novamente e lançava erro.
     * Com {PERSIST, MERGE} ele salva tanto na criação quanto na atualização.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;
}