package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.mapper.UserMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    @GetMapping("/tasks")
    public void exportTasks(@RequestParam(defaultValue = "csv") String format,
                            HttpServletResponse response) throws IOException {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>();
        if (!isAdmin) {
            wrapper.eq(Task::getUserId, currentUser.getId());
        }
        wrapper.orderByAsc(Task::getDeadline);
        List<Task> tasks = taskMapper.selectList(wrapper);

        Map<Long, String> usernameMap = new HashMap<>();
        for (Task t : tasks) {
            if (!usernameMap.containsKey(t.getUserId())) {
                User u = userMapper.selectById(t.getUserId());
                usernameMap.put(t.getUserId(), u != null ? u.getUsername() : "N/A");
            }
        }

        if ("xlsx".equalsIgnoreCase(format)) {
            exportXlsx(tasks, usernameMap, response);
        } else {
            exportCsv(tasks, usernameMap, response);
        }
    }

    private void exportCsv(List<Task> tasks, Map<Long, String> usernameMap,
                           HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.csv");

        try (CSVPrinter printer = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT
                .withHeader("ID", "Title", "Description", "Status", "Priority", "Deadline", "Assignee", "Created At"))) {
            for (Task task : tasks) {
                printer.printRecord(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getPriority(),
                        task.getDeadline(),
                        usernameMap.getOrDefault(task.getUserId(), "N/A"),
                        task.getCreatedAt()
                );
            }
        }
    }

    private void exportXlsx(List<Task> tasks, Map<Long, String> usernameMap,
                            HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tasks");
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "Title", "Description", "Status", "Priority", "Deadline", "Assignee", "Created At"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Task task : tasks) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(task.getId());
                row.createCell(1).setCellValue(task.getTitle());
                row.createCell(2).setCellValue(task.getDescription() != null ? task.getDescription() : "");
                row.createCell(3).setCellValue(task.getStatus());
                row.createCell(4).setCellValue(task.getPriority());
                row.createCell(5).setCellValue(task.getDeadline() != null ? task.getDeadline().toString() : "");
                row.createCell(6).setCellValue(usernameMap.getOrDefault(task.getUserId(), "N/A"));
                row.createCell(7).setCellValue(task.getCreatedAt() != null ? task.getCreatedAt().toString() : "");
            }
            workbook.write(response.getOutputStream());
        }
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
