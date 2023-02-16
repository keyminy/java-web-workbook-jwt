package org.zerock.api01.domain;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Table(name = "tbl_todo_api")
public class Todo {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long tno;
	private String title;
	private LocalDate dueDate;
	private String writer;
	private boolean complete;

	public void changeComplete(boolean complete) {
		this.complete = complete;
	}

	public void changeDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public void changeTitle(String title) {
		this.title = title;
	}
}
