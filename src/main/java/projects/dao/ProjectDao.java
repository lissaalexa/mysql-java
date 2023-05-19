package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {
	public static final String CATEGORY_TABLE = "category";
	public static final String MATERIAL_TABLE = "material";
	public static final String PROJECT_TABLE = "project";
	public static final String PROJECT_CATEGORY_TABLE = "project_category";
	public static final String STEP_TABLE = "step";
	
	//method to input a new project into the database
	public Project insertProject(Project project) {
		String sql = ""
				+ "INSERT INTO " + PROJECT_TABLE + " "
				+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
				+ "VALUES "
				+ "(?, ?, ?, ?, ?)";
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);

				stmt.executeUpdate();
				
				//last inserted ID from database
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				commitTransaction(conn);
				
				//set projectID to the project object
				project.setProjectId(projectId);
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			} 
			
			
		} catch (SQLException e) {
			throw new DbException(e);
		}
		
		return project;
	}
	
	//fetches project by its ID in the database
	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";
			try(Connection conn = DbConnection.getConnection()) {
				startTransaction(conn);
			try {
				Project project = null;
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, projectId, Integer.class);
			try(ResultSet rs = stmt.executeQuery()) {
				if(rs.next()) {
					project = extract(rs, Project.class);
				}
			}
		}
			if(Objects.nonNull(project)) {
				project.getMaterials().addAll(fetchMaterialForProject(conn, projectId));
				project.getSteps().addAll(fetchStepsForProject(conn, projectId));
				project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
			}
			commitTransaction(conn);
			return Optional.ofNullable(project);
		}
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
			catch (SQLException e) {
				throw new DbException(e);
			}
		}

	//method to fetch all projects within the database
	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				List<Project> projects = new LinkedList<>();
				try(ResultSet rs = stmt.executeQuery()) {
					
					while(rs.next()) {
						projects.add(extract(rs, Project.class));
					}
				} catch (Exception e) {
					rollbackTransaction(conn);
					throw new DbException(e);
				}
				return projects;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}
	
	//method to fetch all categories for a project within the database
	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = ""
				+ "SELECT c.* FROM " + CATEGORY_TABLE + " c "
				+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
				+ "WHERE project_id = ?";
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			
		try(ResultSet rs = stmt.executeQuery()) {
			List<Category> categories = new LinkedList<>();
			
			while(rs.next()) {
				categories.add(extract(rs, Category.class));
			}
			return categories;
			}
		}
	}
	
	//method to fetch steps for a project from the database
	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
		try(ResultSet rs = stmt.executeQuery()) {
			List<Step> steps = new LinkedList<>();
			
			while(rs.next()) {
				steps.add(extract(rs, Step.class));
			}
			return steps;
			}
		}
	}
	
	//method to fetch materials for a project from within the database
	private List<Material> fetchMaterialForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1,projectId, Integer.class);
		try(ResultSet rs = stmt.executeQuery()) {
			List<Material> materials = new LinkedList<>();
			
			while(rs.next()) {
				materials.add(extract(rs, Material.class));
			}
		return materials;
			}
		}
	}
	
	//method to modify project details from within the database
	public boolean modifyProjectDetails(Project project) {
		String sql = ""
				+ "UPDATE " + PROJECT_TABLE + " SET "
				+ "project_name = ?, "
				+ "estimated_hours = ?, "
				+ "actual_hours = ?, "
				+ "difficulty = ?, "
				+ "notes = ? "
				+ "WHERE project_id = ?";
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);
				
				boolean modified = stmt.executeUpdate() == 1;
				commitTransaction(conn);
				
				return modified;
			}
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}
	
	//method to delete a project from the database
	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			
			boolean deleted = stmt.executeUpdate() == 1;
			
			commitTransaction(conn);
			return deleted;
			}
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException e) {
			throw new DbException(e);
			}
		}
}