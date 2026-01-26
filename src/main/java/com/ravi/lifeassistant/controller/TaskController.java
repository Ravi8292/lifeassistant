package com.ravi.lifeassistant.controller;
import org.springframework.http.ResponseEntity;
import com.ravi.lifeassistant.dto.AiTaskRequest;
import com.ravi.lifeassistant.dto.AiTaskResponse;
import com.ravi.lifeassistant.enums.TaskStatus;
import com.ravi.lifeassistant.model.Task;
import com.ravi.lifeassistant.service.AiService;
import com.ravi.lifeassistant.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
@CrossOrigin(origins = "http://localhost:4200") // ADD THIS if missing
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AiService aiService;

    // ✅ NORMAL TASK CREATION
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.saveTask(task);
    }

    // 🤖 AI TASK CREATION (THIS FIXES 405 ERROR)
    @PostMapping("/ai")
    public Task createTaskFromAi(@RequestBody AiTaskRequest request) {

        AiTaskResponse aiResponse =
                aiService.parseTextToTask(request.getText());

        Task task = new Task();
        task.setTitle(aiResponse.getTitle());
        task.setDescription(aiResponse.getDescription());
        task.setDueDate(aiResponse.getDueDate());

        task.setStatus(aiResponse.getStatus());

        return taskService.saveTask(task);
    }
    //search task by tile, discription
    @GetMapping("/search")
    public Page<Task> searchTasks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return taskService.searchTasks(keyword, page, size);
    }

    
    //Edit task details 
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        return taskService.updateTask(id, updatedTask);
    }


    // ✅ MARK TASK AS DONE
  /*  @PutMapping("/{id}/done")
    public String markTaskDone(@PathVariable Long id) {
        taskService.markTaskAsDone(id);
        return "Task marked as DONE";
    }
    */
    @PutMapping("/{id}/done")
    public ResponseEntity<Task> markTaskDone(@PathVariable Long id) {
        // 1. Perform the update logic
        Task updatedTask = taskService.markTaskAsDone(id);
        
        // 2. Return the actual updated object as JSON
        return ResponseEntity.ok(updatedTask);
    }
    

    // ✅ DELETE TASK
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "Task deleted successfully";
    }

    // ✅ GET TASKS WITH PAGINATION & FILTER
    @GetMapping
    public Page<Task> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        if (status != null) {
            return taskService.getTasksByStatusPaginated(
                    status, page, size, sortBy, direction);
        }

        return taskService.getTasksPaginated(page, size, sortBy, direction);
    }
}
