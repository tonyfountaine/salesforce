package nz.co.trineo.git.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity(name = "process")
@JsonInclude(Include.NON_DEFAULT)
public class GitProcess {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Column
	private int totalTasks;
	@OneToOne
	private GitTask task;
	@Column
	private int completedTasks;

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public int getTotalTasks() {
		return totalTasks;
	}

	public void setTotalTasks(final int totalTasks) {
		this.totalTasks = totalTasks;
	}

	public GitTask getTask() {
		return task;
	}

	public void setTask(final GitTask task) {
		this.task = task;
	}

	public int getCompletedTasks() {
		return completedTasks;
	}

	public void setCompletedTasks(final int completedTasks) {
		this.completedTasks = completedTasks;
	}
}
