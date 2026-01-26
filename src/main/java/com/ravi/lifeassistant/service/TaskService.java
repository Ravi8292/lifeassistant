package com.ravi.lifeassistant.service;

import java.util.List;
import java.util.Optional;
import com.ravi.lifeassistant.enums.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.ravi.lifeassistant.model.Task;
import com.ravi.lifeassistant.repository.TaskRepository;
import com.ravi.lifeassistant.enums.TaskStatus;
import com.ravi.lifeassistant.exception.TaskNotFoundException;


@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // Business logic: save task
    public Task saveTask(Task task) {

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }

        return taskRepository.save(task);
    }
   /* public Task markTaskAsDone(Long id) {

        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty()) {
            throw new RuntimeException("Task not found with id: " + id);
        }

        Task task = optionalTask.get();
        task.setStatus(TaskStatus.DONE);

        return taskRepository.save(task);
    }*/
    
    public Task markTaskAsDone(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(TaskStatus.DONE);
        return taskRepository.save(task); // Returns the task with the new status
    }
    
    public Task updateTask(Long id, Task updatedTask) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setDueDate(updatedTask.getDueDate());
        task.setStatus(updatedTask.getStatus());

        return taskRepository.save(task);
    }
//serach things on mine project
    public Page<Task> searchTasks(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        keyword, keyword, pageable
                );
    }

    
    
//    public void deleteTask(Long id) {
//        if (!taskRepository.existsById(id)) {
//            throw new RuntimeException("Task not found with id: " + id);
//        }
//        taskRepository.deleteById(id);
//    }
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    // Business logic: fetch all tasks
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    public Page<Task> getTasksPaginated(int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return taskRepository.findAll(pageable);
    }

    public Page<Task> getTasksByStatusPaginated(
            TaskStatus status,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return taskRepository.findByStatus(status, pageable);
    }
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }
}
