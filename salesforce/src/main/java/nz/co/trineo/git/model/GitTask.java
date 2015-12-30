package nz.co.trineo.git.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "task")
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

	public GitTask(String title, int totalWork) {
		this.title = title;
		this.totalWork = totalWork;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTotalWork() {
		return totalWork;
	}

	public void setTotalWork(int totalWork) {
		this.totalWork = totalWork;
	}

	public int getCurrentWork() {
		return currentWork;
	}

	public void setCurrentWork(int currentWork) {
		this.currentWork = currentWork;
	}
}
