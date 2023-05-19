package projects.service;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {
	private ProjectDao projectDao = new ProjectDao();
	//this method simply calls the DAO class to insert a project row
	
	
	//method to add project to database
	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}
	
	//method to fetch all projects from within the database
	public List<Project> fetchAllProjects() {
		return projectDao.fetchAllProjects();
	}
	
	//method to fetch a project by its ID from database
	public Project fetchProjectById(Integer projectId) {
		return projectDao.fetchProjectById(projectId).orElseThrow(() -> new NoSuchElementException("Project with project ID=" + projectId + " does not exist."));
	}
	
	//method to modify project details from database
	public void modifyProjectDetails(Project project) {
			if(!projectDao.modifyProjectDetails(project)) {
				throw new DbException("Project with ID=" + project.getProjectId() + " does not exist.");
			}
	}

	//method to delete a project from database
	public void deleteProject(Integer projectId) {
		if(!projectDao.deleteProject(projectId)) {
			throw new DbException("Project with ID=" + projectId + " does not exist.");
		}
	}
}