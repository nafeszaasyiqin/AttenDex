package com.example.Attendex;

import com.example.Attendex.model.*;
import com.example.Attendex.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.opencsv.CSVWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.security.SecureRandom;

@Controller
@RequestMapping("/lecturer")
public class LecturerController {

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private ClassSessionRepo classSessionRepo;

    @Autowired
    private AttendanceRepo attendanceRepo;

    @Autowired
    private UserRepo userRepo;

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        UserEntity lecturer = userRepo.findByUsername(authentication.getName()).orElseThrow();
        
        // Fetch courses with eager loading of related entities
        List<CourseEntity> courses = courseRepo.findByLecturer(lecturer);
        
        // Calculate additional dashboard statistics
        int totalStudents = calculateTotalStudents(lecturer);
        int totalCourses = courses.size();
        int todaySessions = calculateTodaySessions(lecturer);
        
        // Add attributes to the model
        model.addAttribute("courses", courses);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("todaySessions", todaySessions);
        
        return "/lecturer/lecturer";
    }

    // Helper method to calculate total students across lecturer's courses
    private int calculateTotalStudents(UserEntity lecturer) {
        List<CourseEntity> courses = courseRepo.findByLecturer(lecturer);
        Set<UserEntity> uniqueStudents = new HashSet<>();
        
        for (CourseEntity course : courses) {
            // Assuming you have a method to find students enrolled in a course
            List<UserEntity> courseStudents = userRepo.findStudentsByCourse(course);
            uniqueStudents.addAll(courseStudents);
        }
        
        return uniqueStudents.size();
    }

    // Helper method to calculate today's sessions
    private int calculateTodaySessions(UserEntity lecturer) {
        LocalDate today = LocalDate.now();
        List<CourseEntity> courses = courseRepo.findByLecturer(lecturer);
        
        return (int) courses.stream()
            .filter(course -> {
                DayOfWeek courseDay = DayOfWeek.valueOf(course.getDayOfWeek().toUpperCase());
                return courseDay == today.getDayOfWeek();
            })
            .count();
    }

    @PostMapping("/generate-code/{courseId}")
    @ResponseBody
    public Map<String, String> generateClassCode(@PathVariable Long courseId) {
        CourseEntity course = courseRepo.findById(courseId).orElseThrow();

        // Generate random code
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        // Create new class session
        ClassSessionEntity session = new ClassSessionEntity();
        session.setCourse(course);
        session.setClassCode(code.toString());
        session.setSessionDateTime(LocalDateTime.now());
        session.setCodeExpiryDateTime(LocalDateTime.now().plusMinutes(10)); // Set expiration time
        classSessionRepo.save(session);

        Map<String, String> response = new HashMap<>();
        response.put("code", code.toString());
        response.put("expiresAt", session.getCodeExpiryDateTime().toString()); // Optional: Send expiration info
        return response;
    }

    @GetMapping("/view-attendance")
    public String viewAttendance(Model model, Authentication authentication) {
        UserEntity lecturer = userRepo.findByUsername(authentication.getName()).orElseThrow();
        List<CourseEntity> courses = courseRepo.findByLecturer(lecturer);
        model.addAttribute("courses", courses);
        return "/lecturer/view-attendance";
    }

    @GetMapping("/attendance-data")
    @ResponseBody
    public Map<String, Object> getAttendanceData(
            @RequestParam Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime sessionDate) {
        
        CourseEntity course = courseRepo.findById(courseId).orElseThrow();
        List<ClassSessionEntity> sessions = classSessionRepo.findByCourseAndSessionDateTimeBetween(
            course, 
            sessionDate.toLocalDate().atStartOfDay(),
            sessionDate.toLocalDate().plusDays(1).atStartOfDay()
        );

        List<Map<String, Object>> attendanceData = new ArrayList<>();
        if (!sessions.isEmpty()) {
            ClassSessionEntity session = sessions.get(0);
            List<AttendanceEntity> attendances = attendanceRepo.findByClassSession(session);
            
            for (AttendanceEntity attendance : attendances) {
                Map<String, Object> record = new HashMap<>();
                record.put("studentName", attendance.getStudent().getUsername());
                record.put("status", attendance.isPresent() ? "Present" : "Absent");
                attendanceData.add(record);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", attendanceData);
        return response;
    }

    @GetMapping("/generate-report")
    public String showReportForm(Model model, Authentication authentication) {
        UserEntity lecturer = userRepo.findByUsername(authentication.getName()).orElseThrow();
        List<CourseEntity> courses = courseRepo.findByLecturer(lecturer);
        model.addAttribute("courses", courses);
        return "/lecturer/generate-report";
    }

    @GetMapping("/download-report")
    public void downloadReport(
            @RequestParam Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam String format,
            HttpServletResponse response) throws IOException {
        
        CourseEntity course = courseRepo.findById(courseId).orElseThrow();
        List<ClassSessionEntity> sessions = classSessionRepo.findByCourseAndSessionDateTimeBetween(course, startDate, endDate);

        if ("csv".equals(format)) {
            generateCsvReport(sessions, response);
        } else if ("pdf".equals(format)) {
            generatePdfReport(sessions, response);
        }
    }

    private void generateCsvReport(List<ClassSessionEntity> sessions, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_report.csv\"");

        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);

        // Write header
        csvWriter.writeNext(new String[]{"Student", "Date", "Status"});

        // Write data
        for (ClassSessionEntity session : sessions) {
            List<AttendanceEntity> attendances = attendanceRepo.findByClassSession(session);
            for (AttendanceEntity attendance : attendances) {
                csvWriter.writeNext(new String[]{
                    attendance.getStudent().getUsername(),
                    session.getSessionDateTime().toString(),
                    attendance.isPresent() ? "Present" : "Absent"
                });
            }
        }

        response.getWriter().write(stringWriter.toString());
    }

    private void generatePdfReport(List<ClassSessionEntity> sessions, HttpServletResponse response) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
    
        PDPageContentStream contentStream = new PDPageContentStream(document, page); // Initialize contentStream once
        try {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;
            float tableHeight = 20f;
            final int rows = sessions.size() + 1;
            final int cols = 3;
            float rowHeight = 20f;
            float tableYBottom = yPosition - (rows * rowHeight);
            float cellMargin = 5f;
    
            // Draw header
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yStart);
            contentStream.showText("Attendance Report");
            contentStream.endText();
            
            yPosition -= 30;
    
            // Draw table headers
            float[] columnWidths = {tableWidth / 3, tableWidth / 3, tableWidth / 3};
            
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Student");
            contentStream.newLineAtOffset(columnWidths[0], 0);
            contentStream.showText("Date");
            contentStream.newLineAtOffset(columnWidths[1], 0);
            contentStream.showText("Status");
            contentStream.endText();
            
            yPosition -= rowHeight;
    
            // Draw table content
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            
            for (ClassSessionEntity session : sessions) {
                List<AttendanceEntity> attendances = attendanceRepo.findByClassSession(session);
                for (AttendanceEntity attendance : attendances) {
                    if (yPosition < margin + rowHeight) {
                        // Create a new page if we're running out of space
                        PDPage newPage = new PDPage();
                        document.addPage(newPage);
                        contentStream.close();  // Close the previous contentStream
                        contentStream = new PDPageContentStream(document, newPage);  // Reinitialize the contentStream for the new page
                        contentStream.setFont(PDType1Font.HELVETICA, 12);  // Set font again
                        yPosition = yStart - rowHeight; // Reset the Y position
                    }
    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(attendance.getStudent().getUsername());
                    contentStream.newLineAtOffset(columnWidths[0], 0);
                    contentStream.showText(session.getSessionDateTime().toString());
                    contentStream.newLineAtOffset(columnWidths[1], 0);
                    contentStream.showText(attendance.isPresent() ? "Present" : "Absent");
                    contentStream.endText();
                    
                    yPosition -= rowHeight;
                }
            }
        } finally {
            contentStream.close(); // Close the contentStream after the document is written
        }
    
        // Set response headers and write document
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_report.pdf\"");
        document.save(response.getOutputStream());
        document.close();
    }    
}
