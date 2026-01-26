package com.ravi.lifeassistant.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ravi.lifeassistant.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ravi.lifeassistant.enums.TaskStatus;
import com.ravi.lifeassistant.model.Task;
	public interface TaskRepository extends JpaRepository<Task, Long> {

	    List<Task> findByStatus(TaskStatus status);
	    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
	    Page<Task> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
	            String title,
	            String description,
	            Pageable pageable
	    );
	    
	}

