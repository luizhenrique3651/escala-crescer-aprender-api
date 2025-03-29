package com.crescer_aprender.escala.entity;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Escala {

	@Column(name = "ID_USUARIO")
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@Column(name = "MES_ESCALA")
	Month mes;
	
	@Column(name = "ANO_ESCALA")
	Long ano;
	
	@Column(name = "DATAS_ESCALA")
	List<LocalDate> datas;
	
	@Column(name = "VOLUNTARIO")
	List<Voluntario> voluntarios;
	
	
}
