package nz.co.trineo.git.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity(name = "task")
@JsonInclude(Include.NON_DEFAULT)
public class GitTask {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Column
	private String title;
	@Column
	private int totalWork;
	@Column
	private int currentWork;

	public GitTask(final String title, final int totalWork) {
		this.title = title;
		this.totalWork = totalWork;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public int getTotalWork() {
		return totalWork;
	}

	public void setTotalWork(final int totalWork) {
		this.totalWork = totalWork;
	}

	public int getCurrentWork() {
		return currentWork;
	}

	public void setCurrentWork(final int currentWork) {
		this.currentWork = currentWork;
	}
}
