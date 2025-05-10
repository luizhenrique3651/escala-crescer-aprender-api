package com.crescer_aprender.escala.entity;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Escala {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@Column(name = "MES_ESCALA")
	Integer mes;

	@Column(name = "ANO_ESCALA")
	Long ano;

	@ElementCollection
	@Column(name = "DATAS_ESCALA")
	List<LocalDate> datas;

	@ManyToMany
	@JoinTable(
			name = "escala_voluntario",
			joinColumns = @JoinColumn(name = "escala_id"),
			inverseJoinColumns = @JoinColumn(name = "voluntario_id")
	)
	List<Voluntario> voluntarios;
}
