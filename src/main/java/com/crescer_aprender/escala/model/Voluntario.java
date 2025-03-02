package com.crescer_aprender.escala.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Voluntario {
	
	@Column(name = "ID_USUARIO")
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@Column(name = "NOM_USUARIO")
	String nome;
	
	@Column(name = "EMAIL_USUARIO")
	String email;
	
	@Column(name = "SENHA_USUARIO")
	String senha;
	
	@Column(name = "DATAS_DISPONIVEIS")
	List<LocalDate> datasDisponiveis;
	
	
	
}
