package com.ialedocarmo.pauta_votacao_api.voto.domain;

import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "voto", uniqueConstraints = {
        @UniqueConstraint(name = "uq_voto_pauta_associado", columnNames = {"pauta_id", "associado_id"})
})
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pauta_id", nullable = false)
    private Pauta pauta;

    @Column(name = "associado_id", nullable = false)
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpcaoVoto voto;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public void setPauta(Pauta pauta) {
        this.pauta = pauta;
    }

    public String getAssociadoId() {
        return associadoId;
    }

    public void setAssociadoId(String associadoId) {
        this.associadoId = associadoId;
    }

    public OpcaoVoto getVoto() {
        return voto;
    }

    public void setVoto(OpcaoVoto voto) {
        this.voto = voto;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
