package com.tasks.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tasks.model.Task;
import com.tasks.repository.TaskRepository;
import com.tasks.services.FileStorageService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TaskController {

	@Autowired
	TaskRepository taskRepo;
	
	@Autowired
	FileStorageService fileService;
	
	//Get all tasks
	@GetMapping("/tasks")
	public List<Task> getAllTasks() {
		return taskRepo.findAll();
	}
	
	//Create a new task
	@PostMapping("/tasks")
	public List<Task> createTask(@Valid @RequestBody Task task) {
		taskRepo.save(task);
		return taskRepo.findAll();
	}
	
	//Update a task
	@PutMapping("/tasks/{id}")
	public List<Task> updateTask(@PathVariable(value="id") Long taskId, @RequestParam("file") MultipartFile file) {
		Optional<Task> optionalEntity = taskRepo.findById(taskId);
		Task task = optionalEntity.get();
		
		String fileName = fileService.storeFile(file);
		String fileDownloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/downloadFile/")
                .path(fileName)
                .toUriString();
		
		task.setIsCompleted(true);
		task.setFileName(fileName);
		task.setFileDownloadUrl(fileDownloadUrl);
		taskRepo.save(task);
				
		return taskRepo.findAll();
	}
	
	
	//Get task image
	@GetMapping("downloadFile/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request){
		Resource resource = fileService.loadFile(fileName);
		
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
        	ex.printStackTrace();
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
	}
	
	
	@DeleteMapping("/tasks/{id}")
	public List<Task> deleteTask(@PathVariable(value="id") Long taskId){
		Optional<Task> optionalEntity = taskRepo.findById(taskId);
		Task task = optionalEntity.get();
				
		taskRepo.delete(task);
		return taskRepo.findAll();
	}
}
