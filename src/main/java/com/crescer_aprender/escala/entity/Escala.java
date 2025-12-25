package com.crescer_aprender.escala.entity;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Escala {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@Column(name = "MES_ESCALA")
	Integer mes;

	@Column(name = "ANO_ESCALA")
	Long ano;
    //@TODO verificar necessidade de usar ainda esse campo
	@ElementCollection
	@Column(name = "DATAS_ESCALA")
	List<LocalDate> datas;

	// representa os voluntários escalados por cada data (1..n dias)
	@JsonManagedReference
	@OneToMany(mappedBy = "escala", cascade = CascadeType.ALL, orphanRemoval = true)
	List<EscalaDia> dias;

	@Transient
	String errorMessage;
}
