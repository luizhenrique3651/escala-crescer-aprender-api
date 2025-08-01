package com.crescer_aprender.escala.entity;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Voluntario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@Column(name = "NOM_USUARIO")
	String nome;

	@Column(name = "EMAIL_USUARIO")
	String email;

	@Column(name = "SENHA_USUARIO")
	String senha;

	@ElementCollection
	@Column(name = "DATAS_DISPONIVEIS")
	List<LocalDate> datasDisponiveis;

	@Column(name = "MENSAGEM_ERRO")
	String errorMessage;
}
