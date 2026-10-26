package C:.Users.Asus.Desktop.school-management-restructured.src.test.com.schoolmanagement.school.service.impl;

import com.schoolmanagement.admin.service.EmailService;
import com.schoolmanagement.school.dto.TicketRequestDTO;
import com.schoolmanagement.school.entity.*;
import com.schoolmanagement.school.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TicketNotificationTest {

    @Autowired
    private TicketServiceImpl ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private AddStudentRepository studentRepository;

    @Autowired
    private AddTeacherRepository teacherRepository;

    @MockBean
    private JavaMailSender mailSender;

    private School testSchool;
    private AddStudent testStudent;
    private AddTeacher testTeacher;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();
        Mockito.reset(mailSender);

        testSchool = new School();
        testSchool.setSchoolName("Test School");
        testSchool.setUdiseNo("UDISE12345");
        testSchool.setEmail("school@test.com");
        testSchool.setPhoneNo("1234567890");
        testSchool.setRegistrationStatus("APPROVED");
        testSchool = schoolRepository.save(testSchool);

        testStudent = new AddStudent();
        testStudent.setFirstName("John");
        testStudent.setLastName("Doe");
        testStudent.setEmailId("student@test.com");
        testStudent.setMobileNumber("9876543210");
        testStudent.setSchool(testSchool);
        testStudent.setStatus("ACTIVE");
        testStudent = studentRepository.save(testStudent);

        testTeacher = new AddTeacher();
        testTeacher.setTeacherName("Jane Smith");
        testTeacher.setEmailId("teacher@test.com");
        testTeacher.setMobileNumber("8765432109");
        testTeacher.setSchool(testSchool);
        testTeacher.setStatus("ACTIVE");
        testTeacher = teacherRepository.save(testTeacher);
    }

    private void setSecurityContext(String username, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, null, Collections.singletonList(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testTicketCreationSendsEmail() {
        setSecurityContext("student@test.com", "ROLE_STUDENT");

        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setTitle("Internet Issue");
        dto.setDescription("Wi-Fi is not working in standard 10 classroom");

        ticketService.createTicket(dto);

        // Verify that creation email was triggered asynchronously
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(3000).times(1)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertNotNull(message);
        assertEquals("student@test.com", message.getTo()[0]);
        assertTrue(message.getSubject().contains("Ticket Created Successfully"));
        String body = message.getText();
        assertTrue(body.contains("Dear John Doe"));
        assertTrue(body.contains("Your support ticket has been successfully created."));
        assertTrue(body.contains("Ticket ID:"));
        
        // Assert no category/priority/status text is in the body
        assertFalse(body.contains("Category:"));
        assertFalse(body.contains("Priority:"));
        assertFalse(body.contains("Status:"));
    }

    @Test
    public void testTicketResolutionSendsEmail() {
        setSecurityContext("student@test.com", "ROLE_STUDENT");

        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setTitle("Printer Issue");
        dto.setDescription("Library printer is out of ink");

        ticketService.createTicket(dto);

        // Capture and clear creation email mock interaction
        verify(mailSender, timeout(3000).times(1)).send(any(SimpleMailMessage.class));
        Mockito.reset(mailSender);

        // Find the created ticket
        Ticket ticket = ticketRepository.findAll().stream()
                .filter(t -> t.getTitle().equals("Printer Issue"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Ticket not created"));

        // Change status to IN_PROGRESS (Intermediate - no email)
        ticketService.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);
        verifyNoInteractions(mailSender);

        // Change status to RESOLVED (Triggers resolution email)
        ticketService.updateStatusAndNotes(ticket.getId(), TicketStatus.RESOLVED, "Replaced ink cartridges");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(3000).times(1)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertNotNull(message);
        assertEquals("student@test.com", message.getTo()[0]);
        assertTrue(message.getSubject().contains("Ticket Closed"));
        String body = message.getText();
        assertTrue(body.contains("Dear John Doe"));
        assertTrue(body.contains("Your support ticket has been successfully closed."));
        assertTrue(body.contains("Replaced ink cartridges"));
        
        // Assert no intermediate info
        assertFalse(body.contains("Status:"));
    }

    @Test
    public void testTicketResolutionThenClosureSendsOnlyOneEmail() {
        setSecurityContext("student@test.com", "ROLE_STUDENT");

        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setTitle("Lights Out");
        dto.setDescription("Classroom light is broken");

        ticketService.createTicket(dto);

        // Capture and clear creation email mock interaction
        verify(mailSender, timeout(3000).times(1)).send(any(SimpleMailMessage.class));
        Mockito.reset(mailSender);

        Ticket ticket = ticketRepository.findAll().stream()
                .filter(t -> t.getTitle().equals("Lights Out"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Ticket not created"));

        // Move to IN_PROGRESS (no email)
        ticketService.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);

        // Move to RESOLVED (sends 1st email)
        ticketService.updateStatusAndNotes(ticket.getId(), TicketStatus.RESOLVED, "Replaced lightbulb");
        verify(mailSender, timeout(3000).times(1)).send(any(SimpleMailMessage.class));
        Mockito.reset(mailSender);

        // Move to CLOSED (should NOT send a second email)
        ticketService.updateStatus(ticket.getId(), TicketStatus.CLOSED);
        
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        verifyNoInteractions(mailSender);
    }

    @Test
    public void testIntermediateStatusChangesNoEmail() {
        setSecurityContext("student@test.com", "ROLE_STUDENT");

        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setTitle("Login Issue");
        dto.setDescription("Cannot login to portal");

        ticketService.createTicket(dto);

        // Capture creation email
        verify(mailSender, timeout(3000).times(1)).send(any(SimpleMailMessage.class));
        Mockito.reset(mailSender);

        Ticket ticket = ticketRepository.findAll().stream()
                .filter(t -> t.getTitle().equals("Login Issue"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Ticket not created"));

        // Move to IN_PROGRESS - should NOT send any email
        ticketService.updateStatus(ticket.getId(), TicketStatus.IN_PROGRESS);

        // Wait a bit to ensure no async calls happen
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        verifyNoInteractions(mailSender);
    }

    @Test
    public void testEmailFailureDoesNotAffectOperations() {
        // Mock email sender to throw exception on send
        doThrow(new RuntimeException("Mail server down")).when(mailSender).send(any(SimpleMailMessage.class));

        setSecurityContext("student@test.com", "ROLE_STUDENT");

        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setTitle("Network Outage");
        dto.setDescription("Whole building has no network");

        // The method should complete successfully despite email failure
        assertDoesNotThrow(() -> ticketService.createTicket(dto));

        // Verify ticket was successfully saved in DB
        Optional<Ticket> ticketOpt = ticketRepository.findAll().stream()
                .filter(t -> t.getTitle().equals("Network Outage"))
                .findFirst();
        assertTrue(ticketOpt.isPresent());
        assertEquals(TicketStatus.OPEN, ticketOpt.get().getStatus());
    }
}


