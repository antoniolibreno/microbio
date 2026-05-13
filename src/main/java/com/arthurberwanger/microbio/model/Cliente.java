package com.arthurberwanger.microbio.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
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

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;

    public Cliente() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }
}
