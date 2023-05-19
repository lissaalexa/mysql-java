package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

//this class is a menu driven application that accepts user input from the console. it then performs CRUD operations on the project tables

public class ProjectsApp {
	private Scanner scanner = new Scanner(System.in);
	private ProjectService projectService = new ProjectService();
	private Project curProject;
	
//above represents the operation list that a user can choose within the program
	private List<String> operations = List.of(
		"1) Add a project",
		"2) List projects",
		"3) Select a project",
		"4) Update project details",
		"5) Delete a project"
		);

//entry point for Java application
public static void main(String[] args) {
	new ProjectsApp().processUserSelections();
	}

//this method prints the operations, gets a user menu selection, and performs the requested operation
//it repeats until the user requests that the application terminate.
private void processUserSelections() {
	boolean done = false;
	
	while(!done) {
		try {
			//Get user's selection
			int selection = getUserSelection();
			
			switch(selection) {
			// if selection -1, call the exitMenu method and assign its return value to done
			case -1:
				done = exitMenu();
				break;
			
			//if selection 1, call the createProject method
			case 1:
				createProject();
				break;
			
			//if selection 2, call the listProjects method	
			case 2:
				listProjects();
				break;
			
			//if selection 3, call the selectProject method	
			case 3:
				selectProject();
				break;
			
			//if selection 4, call the updateProjectDetails method
			case 4:
				updateProjectDetails();
				break;
			
			//if selection 5, call the deleteProject method
			case 5:
				deleteProject();
				break;
				
			default:
				System.out.println("\n" + selection + " is not a valid selection. Try again.");
				break;
			}
		} catch(Exception e) {
			// catch any exception that happens during the processing and display of an error message
			System.out.println("\nError: " + e + " Try again.");
			}
	  	}
	}

//gather user input for a project row and then call the project service to create the row
private void deleteProject() {
	//display list of projects
	listProjects();
	
	//get project ID to delete from the user
	Integer projectId = getIntInput("Enter the ID of the project to delete");
	
	projectService.deleteProject(projectId);
	System.out.println("Project " + projectId + " was deleted successfully.");
	if(Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
		curProject = null;
	}
}

private void updateProjectDetails() {
	// check if there is a currently selected project
	if(Objects.isNull(curProject)) {
		System.out.println("\nPlease select a project.");
		return;
	}
	
	//get updated project details from the user
	String projectName =
			getStringInput("Enter the project name [" + curProject.getProjectName() + "]");
	
	BigDecimal estimatedHours =
			getDecimalInput("Enter the estimated hours [" + curProject.getEstimatedHours() + "]");
	
	BigDecimal actualHours =
			getDecimalInput("Enter the actual hours + [" + curProject.getActualHours() + "]");
	
	Integer difficulty =
			getIntInput("Enter the project difficulty (1-5) [" + curProject.getDifficulty() + "]");
	
	String notes = getStringInput("Enter the project notes [" + curProject.getNotes() + "]");
	
	Project project = new Project();
	
	project.setProjectId(curProject.getProjectId());
	project.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
	
	project.setEstimatedHours(
			Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
	project.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
	project.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
	project.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);
	
	projectService.modifyProjectDetails(project);
	
	curProject = projectService.fetchProjectById(curProject.getProjectId());
}

private void selectProject() {
	//display list of projects
	listProjects();
	
	Integer projectId = getIntInput("Enter a project ID to select a project");
	curProject = null;
	curProject = projectService.fetchProjectById(projectId);
}

private void listProjects() {
	// get all projects from the projectService
	List<Project> projects = projectService.fetchAllProjects();
	//display list of projects
	System.out.println("\nProjects:");
	
	projects.forEach(project -> System.out.println("   " + project.getProjectId() + ": " + project.getProjectName())); 
}

//gather user input for a project row then call the project service to create the row
private void createProject() {
	//get project details from the user
	String projectName = getStringInput("Enter the project name");
	BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
	BigDecimal actualHours = getDecimalInput("Enter the actual hours");
	Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
	String notes = getStringInput("Enter the project notes");
	
	//create a new project object
	Project project = new Project();
	//set values
	project.setProjectName(projectName);
	project.setEstimatedHours(estimatedHours);
	project.setActualHours(actualHours);
	project.setDifficulty(difficulty);
	project.setNotes(notes);
	
	Project dbProject = projectService.addProject(project);
	System.out.println("You have successfully created project: " + dbProject);
	
	curProject = projectService.fetchProjectById(dbProject.getProjectId());
}

private BigDecimal getDecimalInput(String prompt) {
	//get decimal input from the user
	String input = getStringInput(prompt);
	
	if(Objects.isNull(input)) {
		return null;
	} try {
		return new BigDecimal(input).setScale(2);
	} catch(NumberFormatException e) {
		//throw an exception if the input is not a valid decimal number
		throw new DbException(input + " is not a valid decimal number.");
	}
}

//called when the user wants to exit the application
//it prints a message and returns to terminate the app
private boolean exitMenu() {
	//displays exit menu
	System.out.println("Exiting the menu.");
	return true;
}

//this method prints the available menu selections
//it then gets the user's menu selection from the console and converts it to an int
private int getUserSelection() {
	printOperations();
	Integer input = getIntInput("Enter a menu selection");
	return Objects.isNull(input) ? -1 : input;
	}

//print the menu selections, one per line
private void printOperations() {
	//print available selections and the current project
	System.out.println("\nThese are the available selections. Press the Enter key to quit:");
	operations.forEach(line -> System.out.println("  " + line));
		if(Objects.isNull(curProject)) {
			System.out.println("\nYou are not working with a project.");
		} else {
			System.out.println("\nYou are working with project: " + curProject);
		}
	}

//prints a prompt on the console and then gets the user's input from the console
//it then converts the input to an integer
private Integer getIntInput(String prompt) {
	String input = getStringInput(prompt);
	
	if(Objects.isNull(input)) {
		return null;
	} try {
		return Integer.valueOf(input);
	} catch(NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
			//throw an exception is the input is not a valid number
		}
	}

//prints a prompt on the console and then gets the user's input from the console
//if the user enters nothing, (@code null) is returned, otherwise the trimmed input is returned
private String getStringInput(String prompt) {
	//user input and get a string input
	System.out.print(prompt + ": ");
	String input = scanner.nextLine();
	
	return input.isBlank() ? null : input.trim();
	}
}